package com.github.vassilibykov.adventkt

/**
 * An action bound to a room or an item. Its effect is typically specified
 * using a lambda within a `.action()` configuration message.
 */
open class LocalAction(words: Collection<String>, private var effect: LocalAction.()->Unit) : Action(*(words.toTypedArray())) {

    /**
     * The subjects of the currently executing command,
     * made available for the effect block.
     */
    internal var subjects = listOf<String>()
        private set

    fun guardedBy(guard: LocalAction.()->Boolean, failMessage: String): LocalAction {
        val originalAction = effect
        effect = {
            if (guard()) {
                originalAction()
            } else {
                say(failMessage)
            }
        }
        return this
    }

    /**
     * Indicate whether any of the current subject words refer to the [item].
     */
    fun referringTo(item: Item) = subjects.any { it in item.names }

    override fun act(subjects: List<String>) {
        this.subjects = subjects
        effect()
    }
}