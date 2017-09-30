package com.github.vassilibykov.adventkt

/**
 *
 * @author vassili
 */
abstract class Verb(vararg val words: String) {

    init {
        knownWords.addAll(words)
    }

    abstract fun act(subjects: List<String>)

    fun addTo(map: MutableMap<String, Verb>) {
        words.forEach { map[it] = this }
    }

    internal fun pass() {
        throw RejectedException()
    }

    companion object {
        val knownWords = mutableSetOf<String>()
    }

    class RejectedException : RuntimeException()
}
