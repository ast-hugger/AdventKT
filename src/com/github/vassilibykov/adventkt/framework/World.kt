/*
 * Copyright (c) 2017 Vassili Bykov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vassilibykov.adventkt.framework

import com.github.vassilibykov.adventkt.cave.globalVocabulary
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

/**
 * The superclass of a game definition. Defines the top-level DSL constructs.
 *
 * Uses reflection to implement a second initialization pass for its properties
 * which implement the [Configurable] interface, thus allowing mutually
 * recursive references which are the bane of regular initializers.
 *
 * @author Vassili Bykov
 */
abstract class World internal constructor() {
    private val vocabulary = globalVocabulary()

    val player = Player()

    abstract val start: Room

    val parser = Parser()

    private fun defaultReader(): String {
        print("> ")
        return readLine() ?: "quit"
    }

    /**
     * Read and process the user input until a [QuitException] is thrown.
     *
     * Here is a high level overview of how input is processed.
     *
     *   1. The input string is analyzed by the parser. The parser selects
     *   and invokes applicable actions as detailed in [Parser.process].
     *   2. Invoked actions move, or attempt to move, game objects (items
     *   and the player), producing the bulk of the game output. (For example,
     *   if the player has moved to a new room, the new room prints its
     *   description when it's notified about the player moving into it).
     *   For more details, see [Player.moveTo] and [Item.moveTo].
     *   3. Once all actions have run, items in the player's vicinity,
     *   which are items held by the player and items in the player's room,
     *   as well as the player's room itself, are notified about the
     *   turn end. That lets them generate additional game events which
     *   appear to be independent from the player's actions (for example,
     *   in certain rooms a dwarf shows up and throws an axe).
     *   See [Item.noticeTurnEnd] and [Room.noticeTurnEnd].
     */
    fun play(lineReader: ()-> String = this::defaultReader) {
        try {
            player.moveTo(start)
            while (true) {
                val command = lineReader()
                parser.process(command)
            }
        } catch (e: QuitException) {
            println("Leaving.")
        }
    }

    fun findAction(word: String): Action? = vocabulary[word]

    /*
        DSL constructs
     */

    /**
     * Declare a room with natural light. The player can always see in this room.
     */
    internal fun litRoom(caption: String, description: String, configurator: Room.()->Unit): Room {
        val room = Room(caption, description)
        room.configurator = configurator
        return room
    }

    /**
     * Declare a room with no natural light. The player can see only when the
     * lantern in the room, and the lantern is lit. The lantern may be dropped
     * in the room or carried by the player.
     */
    internal fun darkRoom(caption: String, description: String, configurator: Room.()->Unit): DarkRoom {
        val room = DarkRoom(caption, description)
        room.configurator = configurator
        return room
    }

    /**
     * Declare a general purpose item.
     */
    internal fun item(vararg names: String, owned: ()->String, dropped: ()->String, configurator: Item.() -> Unit): Item {
        val item = Item(*names, owned = owned, dropped = dropped)
        item.configurator = configurator
        return item
    }

    /**
     * Declare a general purpose item.
     */
    internal fun item(vararg names: String, owned: String, dropped: String, configurator: Item.() -> Unit): Item {
        val item = Item(*names, owned = owned, dropped = dropped)
        item.configurator = configurator
        return item
    }

    /**
     * Declare a general purpose item.
     */
    internal fun item(vararg names: String, configurator: Item.() -> Unit): Item {
        val item = Item(*names)
        item.configurator = configurator
        return item
    }

    /**
     * Declare an item which is present in the room but can't be picked up by the player.
     */
    internal fun fixture(vararg names: String, message: ()->String, configurator: Fixture.() -> Unit): Fixture {
        val item = Fixture(*names, message = message)
        @Suppress("UNCHECKED_CAST")
        item.configurator = configurator as Item.() -> Unit
        return item
    }

    /**
     * Declare an item which is present in the room but can't be picked up by the player.
     */
    internal fun fixture(vararg names: String, message: String, configurator: Fixture.() -> Unit): Fixture {
        val item = Fixture(*names, message = message)
        @Suppress("UNCHECKED_CAST")
        item.configurator = configurator as Item.() -> Unit
        return item
    }

    /**
     * Declare an item which is present in the room but can't be picked up by the player.
     */
    internal fun fixture(vararg names: String, configurator: Fixture.() -> Unit): Fixture {
        val item = Fixture(*names)
        @Suppress("UNCHECKED_CAST")
        item.configurator = configurator as Item.() -> Unit
        return item
    }

    /**
     * Run [Configurable.configure] methods of all properties of this instance which
     * implement the [Configurable] interface. It is the responsibility of a [World]
     * instance creator, typically a factory method in the companion object of a
     * subclass, to invoke this method on a freshly created instance.
     *
     * @see ColossalCave.create
     */
    internal fun runObjectSetup() {
        val configurationContext = ConfigurationContext()
        for (property in this::class.memberProperties) {
            val type = property.returnType
            if (type.isSubtypeOf(Configurable::class.starProjectedType)) {
                @Suppress("UNCHECKED_CAST")
                val p = property as KProperty1<World, Configurable>
                val configurable = p.get(this)
                configurationContext.configure(configurable)
            }
        }
    }

    /**
     * Reflectively examine this world's properties, looking for one with a
     * matching name and of type [Room]. Return the property value or null if
     * not found. This [name] is expected to come from the parser, and therefore
     * be converted to lowercase. Thus, the match against the property name is
     * case-insensitive.
     */
    fun findRoom(name: String): Room? = findDeclaredObject(Room::class, name)

    /**
     * Reflectively examine this world's properties, looking for one with a
     * matching name and of type [Item]. Return the property value or null if
     * not found. This [name] is expected to come from the parser, and therefore
     * be converted to lowercase. Thus, the match against the property name is
     * case-insensitive.
     */
    fun findItem(name: String): Item? = findDeclaredObject(Item::class, name)

    private fun <T : Any> findDeclaredObject(typeToken: KClass<T>, name: String): T? {
        for (property in this::class.memberProperties) {
            val type = property.returnType
            if (type.isSubtypeOf(typeToken.starProjectedType)) {
                @Suppress("UNCHECKED_CAST")
                if (property.name.toLowerCase() == name) {
                    val p = property as KProperty1<World, T>
                    return p.get(this)
                }
            }
        }
        return null
    }

    /**
     * Keeps track of configurable objects configured so far, thus avoiding
     * multiple and infinitely recursive configuration calls.
     */
    class ConfigurationContext {
        private val alreadyConfigured = mutableSetOf<Configurable>()

        fun configure(c: Configurable) {
            if (c !in alreadyConfigured) {
                alreadyConfigured.add(c)
                c.configure(this)
            }
        }
    }

    /**
     * An object which, when a property of a [World] subclass instance defining
     * the game world, will have its [configure] method invoked after all the
     * world properties have been created.
     */
    interface Configurable {
        fun configure(context: ConfigurationContext)
    }

    class QuitException: RuntimeException()
}

