package com.github.vassilibykov.adventkt

/**
 * A simple input parser. Far from Infocom grade, but better than the
 * classic one.
 *
 * @author Vassili Bykov
 */
class Parser {

    private val IGNORED_WORDS = setOf("a", "the", "in", "at")

    fun process(command: String) {
        val tokens = command.split(' ', '\t', '\n', '\r')
        if (!tokens.isEmpty()) {
            val first = tokens[0]
            val rest = tokens.drop(1).filter { it !in IGNORED_WORDS }
            val allVerbs = allMatchingActions(first)
            if (allVerbs.isEmpty()) {
                if (first in Action.knownWords) {
                    say("There is nothing here to $first.")
                } else {
                    say("I don't understand \"$command\".")
                }
            } else {
                for (verb in allVerbs) {
                    try {
                        verb.act(rest)
                        return
                    } catch (e: Action.RejectedException) {
                        // continue to the next one
                    }
                }
                if (rest.isEmpty()) {
                    say("What are you trying to $first?")
                } else {
                    say("You can't $first that.")
                }
            }
        }
    }

    private fun allMatchingActions(word: String): List<Action> {
        val inventoryItemActions = player.items.mapNotNull { it.findAction(word) }
        val vicinityItemActions = player.room.items.mapNotNull { it.findVicinityAction(word) }
        val roomActions = listOfNotNull(player.room.findAction(word))
        val globalActions = listOfNotNull(cave.findAction(word))
        return inventoryItemActions + vicinityItemActions + roomActions + globalActions
    }
}

