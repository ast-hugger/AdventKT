package com.github.vassilibykov.adventkt

/**
 * A verb bound to a room or an item. Its action is typically specified
 * using a lambda within a `.verb()` configuration message.
 */
open class LocalVerb(words: Collection<String>, private var action: LocalVerb.()->Unit) : Verb(*(words.toTypedArray())) {

    /**
     * The subjects of the currently executing command,
     * made available for the action block.
     */
    internal var subjects = listOf<String>()
        private set

    fun guardedBy(guard: LocalVerb.()->Boolean, failMessage: String) {
        val originalAction = action
        action = {
            if (guard()) {
                originalAction()
            } else {
                say(failMessage)
            }
        }
    }

    /**
     * Indicate whether any of the current subject words refer to the [item].
     */
    fun referringTo(item: Item) = subjects.any { it in item.names }

    override fun act(subjects: List<String>) {
        this.subjects = subjects
        action()
    }
}