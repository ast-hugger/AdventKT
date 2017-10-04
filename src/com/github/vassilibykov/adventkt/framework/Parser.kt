/*
 * Copyright (c) 2017 Vassili Bykov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vassilibykov.adventkt.framework

import com.github.vassilibykov.adventkt.cave.cave
import com.github.vassilibykov.adventkt.cave.player

/**
 * A simple user input parser, selecting and running actions applicable to the
 * given input. Far from Infocom grade, but better than the classic one.
 *
 * To correctly express the game logic, the programmer must clearly understand
 * the protocol of how actions are selected and applied by the parser. The
 * protocol is described in [process].
 *
 * @author Vassili Bykov
 */
class Parser {

    /**
     * These words are filtered out from the input before it's processed.
     */
    private val IGNORED_WORDS = setOf("a", "the", "at", "to")

    /**
     * Select and invoke the action(s) applicable to the specified user input as
     * follows:
     *
     *   1. Split the input into individual words and drop any [IGNORED_WORDS].
     *   We will generally refer to the first remaining word as the _command_
     *   and the words that follow (if any) as _subjects_.
     *   2. Produce a list of _applicable actions_ by selecting actions which
     *   have the command among their identifying words, in the following order
     *   of decreasing specificity:
     *     * actions of items in the player's inventory
     *     * vicinity actions of items in the player's room
     *     * actions of the player's room
     *     * global actions.
     *   3. If the list of applicable actions is empty, the parser does not know
     *   how to handle the user input and prints a corresponding message.
     *   4. Otherwise, the first element on the list (the most specific action) is
     *   activated by calling its [Action.act] method.
     *   5. If the method returns normally, the input is considered processed
     *   and no further actions are activated.
     *   6. If the method throws the [Action.RejectedException] (typically by
     *   calling [Action.pass]), the next element of the applicable list is
     *   selected for execution.
     *   7. This process repeats until an action completes normally or there are
     *   no more applicable actions.
     *
     * An action's applicability is only determined by the match of the current
     * command with one of the action's identifying words. Additionally, when
     * invoked, an action attached to an item will examine the current command's
     * subject words for a match with one of the item's names. If there is no
     * match, the item action will not attempt to run and will throw
     * [Action.RejectedException], setting in motion the step (6) above.
     */
    fun process(input: String) {
        val tokens = input.split(' ', '\t', '\n', '\r')
        if (!tokens.isEmpty()) {
            val command = tokens[0].toLowerCase()
            val subjects = tokens.drop(1).filter { it !in IGNORED_WORDS }.map { it.toLowerCase() }
            val applicableActions = allApplicableActions(command)
            if (applicableActions.isEmpty()) {
                if (command in Action.knownWords) {
                    say("There is nothing here to $command.")
                } else {
                    say("I don't understand \"$input\".")
                }
            } else {
                for (verb in applicableActions) {
                    try {
                        verb.act(subjects)
                        return
                    } catch (e: Action.RejectedException) {
                        // continue to the next applicable action
                    }
                }
                if (subjects.isEmpty()) {
                    say("What are you trying to $command?")
                } else {
                    say("You can't $command that.")
                }
            }
        }
    }

    private fun allApplicableActions(command: String): List<Action> {
        val inventoryItemActions = player.items.mapNotNull { it.findAction(command) }
        val vicinityItemActions = player.room.items.mapNotNull { it.findVicinityAction(command) }
        val roomActions = listOfNotNull(player.room.findAction(command))
        val globalActions = listOfNotNull(cave.findAction(command))
        return inventoryItemActions + vicinityItemActions + roomActions + globalActions
    }
}

