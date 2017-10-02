package com.github.vassilibykov.adventkt

import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

/**
 * The superclass of a game definition. Uses reflection to implement a second
 * initialization pass for its properties which implement the [Configurable]
 * interface, thus allowing mutually recursive references which are the bane of
 * regular initializers.
 */
abstract class World internal constructor() {
    private val vocabulary = globalVocabulary()

    val player = Player()

    abstract val start: Room

    val parser = Parser()

    fun play() {
        try {
            player.moveTo(start)
            while (true) {
                print("> ")
                val command = readLine() ?: "quit"
                parser.process(command)
            }
        } catch (e: QuitException) {
            println("Leaving.")
        }
    }

    fun findAction(word: String): Action? = vocabulary[word]

    /**
     * Run [Configurable.configure] methods of all properties of this instance which
     * implement the [Configurable] interface. It is the responsibility of a [World]
     * instance creator, typically a factory method in the companion object of a
     * subclass, to invoke this method on a freshly created instance.
     *
     * @see ColossalCave.create
     */
    internal fun runObjectSetup() {
        // A property may refer to another property value, so we need to protect
        // against calling .configure() more than once on the same object.
        val alreadyConfigured = mutableSetOf<Configurable>()
        for (property in this::class.memberProperties) {
            val type = property.returnType
            if (type.isSubtypeOf(Configurable::class.starProjectedType)) {
                @Suppress("UNCHECKED_CAST")
                val p = property as KProperty1<World, Configurable>
                val configurable = p.get(this)
                if (configurable !in alreadyConfigured) {
                    alreadyConfigured.add(configurable)
                    configurable.configure()
                }
            }
        }
    }

    /**
     * An object which, when a property of a [World] subclass instance defining
     * the game world, will have its [configure] method invoked after all properties
     * have been created.
     */
    interface Configurable {
        fun configure()
    }

    class QuitException: RuntimeException()
}
