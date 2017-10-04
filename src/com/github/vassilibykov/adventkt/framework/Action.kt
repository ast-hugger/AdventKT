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

/**
 * A handler of user input, participating in parsing the input and responsible
 * for carrying out the action. An action has a set of identifying words, which
 * are used to determine if an action is applicable to the given user input.
 *
 * An action can be global and always checked against the input, or attached to
 * rooms or items and only involved in input processing when in the player's
 * vicinity or possession.
 *
 * @see Parser.process
 *
 * @author Vassili Bykov
 */
abstract class Action(vararg val words: String) {

    companion object {
        /**
         * A set of identifying words of all known actions. Used to determine
         * whether the verb of the input is not understood only because at the
         * moment there are no applicable actions, or because it is not known
         * to the game.
         */
        val knownWords = mutableSetOf<String>()
    }

    init {
        knownWords.addAll(words)
    }

    /**
     * Carry out the action.
     */
    abstract fun act(subjects: List<String>)

    internal fun addTo(map: MutableMap<String, Action>) {
        words.forEach { map[it] = this }
    }

    /**
     * Stop the execution of this action and pass control to the next less
     * specific action. Intended to be called from the [act] method of an
     * action.
     */
    internal fun pass() {
        throw RejectedException()
    }

    override fun toString(): String {
        return this::class.simpleName + words
    }

    internal class RejectedException : RuntimeException()
}
