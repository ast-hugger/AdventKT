package com.github.vassilibykov.adventkt

/**
 *
 */
abstract class World {
    private val vocabulary = globalVocabulary()

    abstract val player: Player

    abstract val lantern: Lantern

    fun findVerb(word: String): Verb? = vocabulary[word]

    class VetoException : RuntimeException()
}