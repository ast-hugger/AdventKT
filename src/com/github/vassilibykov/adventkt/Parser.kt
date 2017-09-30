package com.github.vassilibykov.adventkt

/**
 * A simple input parser. Far from Infocom grade, but better
 * than the original.
 *
 * @author Vassili Bykov
 */
class Parser {

    val IGNORED = setOf("a", "the", "in", "at")

    fun process(command: String) {
        val tokens = command.split(' ', '\t', '\n', '\r')
        if (!tokens.isEmpty()) {
            val first = tokens[0]
            val rest = tokens.drop(1).filter { it !in IGNORED }
            val allVerbs = allMatchingVerbs(first)
            if (allVerbs.isEmpty()) {
                if (first in Verb.knownWords) {
                    say("There is nothing here to $first.")
                } else {
                    say("I don't understand \"$command\".")
                }
            } else {
                for (verb in allVerbs) {
                    try {
                        verb.act(rest)
                        return
                    } catch (e: Verb.RejectedException) {
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

    private fun allMatchingVerbs(word: String): List<Verb> {
        val inventoryVerbs = player.inventory.mapNotNull { it.findVerb(word) }
        val vicinityItemVerbs = player.room.items.mapNotNull { it.findVicinityVerb(word) }
        val roomVerbs = listOfNotNull(player.room.findVerb(word))
        val globalVerbs = listOfNotNull(cave.findVerb(word))
        return inventoryVerbs + vicinityItemVerbs + roomVerbs + globalVerbs
    }
}

