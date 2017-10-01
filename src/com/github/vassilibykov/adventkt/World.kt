package com.github.vassilibykov.adventkt

import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

/**
 *
 */
abstract class World {
    private val vocabulary = globalVocabulary()

    abstract val player: Player

    abstract val lantern: Lantern

    fun findVerb(word: String): Verb? = vocabulary[word]

    fun runObjectInitializers() {
        for (property in this::class.memberProperties) {
            val type = property.returnType
            if (type.isSubtypeOf(Object::class.starProjectedType)) {
                @Suppress("UNCHECKED_CAST")
                val p = property as KProperty1<World, Object>
                p.get(this).initialize()
            }
        }
    }

    interface Object {
        fun initialize()
    }

    class VetoException : RuntimeException()
}