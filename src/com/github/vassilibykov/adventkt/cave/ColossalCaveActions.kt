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

package com.github.vassilibykov.adventkt.cave

import com.github.vassilibykov.adventkt.framework.*


/*
    Global vocabulary actions, alphabetic order.

    @author Vassili Bykov
 */

fun globalVocabulary(): Map<String, Action> {
    val vocabulary = mutableMapOf<String, Action>()
    fun add(action: Action) = action.words.forEach { vocabulary.put(it, action) }
    add(Drop())
    add(Go())
    add(Inventory())
    add(Look())
    add(Quit())
    add(Say())
    add(Take())
    add(Xyzzy())
    add(Summon())
    add(Teleport())
    add(AvadaKedavra())
    StandardDirection.directionNames.forEach {vocabulary.put(it, DirectionAction(it))}
    return vocabulary
}

class DirectionAction(word: String) : MovementAction(word) {
    override fun act(subjects: List<String>) {
        movePlayer(words[0])
    }
}

class Drop: Action("drop") {
    override fun act(subjects: List<String>) {
        val item = player.findItem(subjects)
        if (item == null) {
            say("You don't have that.")
            return
        }
        item.moveTo(player.room)
    }
}

class Go: MovementAction("go") {
    override fun act(subjects: List<String>) {
        if (subjects.isEmpty()) {
            println("Go where?")
        } else {
            movePlayer(subjects[0])
        }
    }
}

class Inventory: Action("inventory", "i", "inv") {
    override fun act(subjects: List<String>) {
        val items = player.items
        if (items.isEmpty()) {
            say("You are carrying nothing.")
        } else {
            say("You are currently holding the following:")
            items.forEach { say(it.inventoryDescription) }
        }
    }
}

class Look: Action("look") {
    override fun act(subjects: List<String>) {
        if (subjects.isEmpty()) {
            player.room.printFullDescription()
        } else {
            say("You don't see any such thing.")
        }
    }
}

class Say: Action("say", "speak") {
    override fun act(subjects: List<String>) {
        say("Just say it.")
    }
}

class Quit: Action("quit", "bye") {
    override fun act(subjects: List<String>) {
        throw World.QuitException()
    }
}

class Take: Action("take", "get", "pick") {
    override fun act(subjects: List<String>) {
        if ("all" in subjects) {
            takeAll()
            return
        }
        val item = subjects.map { player.room.findItem(it) }.firstOrNull()
        if (item == null) {
            say("There is no such thing here.")
        } else {
            item.moveTo(player)
        }
    }

    private fun takeAll() {
        // ownItem modifies the item list, so make sure to iterate over a copy
        player.room.items.toList().forEach {
            if (!it.isHidden) {
                it.moveTo(player)
            }
        }
    }
}

class Xyzzy : Action("xyzzy") {
    override fun act(subjects: List<String>) {
        say("Nothing happens.")
    }
}

/*
    Let's have a few generic magic spells for testing and giggles.
 */

/**
 * `vocare <name>`: teleport to the player's room any non-fixture item.
 * `vocare fortis <name>`: teleport to player's room any item, even if it's a fixture.
 *
 * The name is the name of the property which declares the item in the [ColossalCave] class.
 */
class Summon : Action("vocare") {
    override fun act(subjects: List<String>) {
        for (itemName in subjects) {
            val item = cave.findItem(itemName)
            if (item != null) {
                if (player has item) {
                    say("You already own the $item.")
                } else {
                    val anItem = if (item.isPlural) item.toString() else "${item.indefiniteArticle} $item"
                    if (item is Fixture  && "fortis" !in subjects) {
                        say("For a brief moment, you see a shimmering outline of $anItem\n" +
                                "floating in the air. It disappears with a loud \"pop!\"")
                    } else {
                        say("You see a shimmering outline of $anItem floating in the air.\n" +
                                "It quickly solidifies, and the $item ${if (item.isPlural) "drop" else "drops"} to the ground.")
                        item.primitiveMoveTo(player.room)
                    }
                }
                return // breaking out of the for loop
            }
        }
        say("A gust of wind blows, but nothing happens.")
    }
}


/**
 * `teleportio <name>`: teleport the player to any room. The room must be declared
 * as a property in the world definition class. Name match is case-insensitive.
 */
class Teleport : Action("teleportio") {
    override fun act(subjects: List<String>) {
        for (roomName in subjects) {
            val room = cave.findRoom(roomName)
            if (room != null) {
                say(">>Foof!<<")
                player.internalMoveTo(room)
                return
            }
        }
        say("A gust of wind blows, but nothing happens.")
    }
}

private var firstAKuse = true

class AvadaKedavra : Action("avada") {
    override fun act(subjects: List<String>) {
        if ("kedavra" in subjects) {
            val item = subjects.mapNotNull { player.room.findItem(it) }.firstOrNull()
            if (item != null) {
                say("""There is a flash of green lightning and a rushing sound.
                    |The $item disappears in a puff of green smoke.""")
                item.moveTo(Item.LIMBO)
                if (firstAKuse) {
                    firstAKuse = false
                    say("\nA hollow voice says, \"Been doing some reading, have we?\"")
                }
            } else {
                say("""There is a flash of green lightning and a rushing sound.
                    |You disappear in a puff of green smoke.""")
                throw World.QuitException()
            }
        } else {
            say("I don't like the sound of this.")
        }
    }
}
