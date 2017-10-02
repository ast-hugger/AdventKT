package com.github.vassilibykov.adventkt

/**
 *
 * @author Vassili Bykov
 */
abstract class Action(vararg val words: String) {

    init {
        knownWords.addAll(words)
    }

    abstract fun act(subjects: List<String>)

    fun addTo(map: MutableMap<String, Action>) {
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
