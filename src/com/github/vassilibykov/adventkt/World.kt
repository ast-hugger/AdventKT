package com.github.vassilibykov.adventkt

import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

/**
 * The superclass of a game definition. Uses reflection to implement a second
 * initialization pass for its properties which implement the [Object]
 * interface, thus allowing mutually recursive references which are the bane of
 * regular initializers.
 */
abstract class World internal constructor() {
    private val vocabulary = globalVocabulary()

    abstract val player: Player

    fun findVerb(word: String): Verb? = vocabulary[word]

    /**
     * Run [Object.setup] methods of all properties of this instance which
     * implement the [Object] interface. It is the responsibility of a [World]
     * instance creator, typically a factory method in the companion object of a
     * subclass, to invoke this method on a freshly created instance.
     *
     * @see ColossalCave.create
     */
    internal fun runObjectSetup() {
        for (property in this::class.memberProperties) {
            val type = property.returnType
            if (type.isSubtypeOf(Object::class.starProjectedType)) {
                @Suppress("UNCHECKED_CAST")
                val p = property as KProperty1<World, Object>
                p.get(this).setup()
            }
        }
    }

    /**
     * An object which, when a property of a [World] subclass instance defining
     * the game world, will have its [setup] method invoked after all properties
     * have been created.
     */
    interface Object {
        fun setup()
    }

    class VetoException : RuntimeException()
}