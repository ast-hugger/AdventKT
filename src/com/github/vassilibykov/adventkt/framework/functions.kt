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

import java.util.*

val random = Random()

/**
 * Print a game output message. The message is always
 * filtered through `trimMargin`, with a newline appended.
 */
fun say(message: String) = println(message.trimMargin())

fun blankLine() = println()

fun decline(message: String): Boolean {
    say(message)
    return false
}

/**
 * Evaluate the [condition]. If true, print the specified message and return false.
 * Otherwise, silently return true. Intended to be used as part of the `allow`
 * declarations, for example
 *
 *     allowPlayerExit { _ ->
 *         declineIf({ player has forbiddenItem },
 *             "You are not allowed to take $forbiddenItem out of the room.")
 *     }
 */
fun declineIf(condition: ()->Boolean, message: String): Boolean {
    return if (condition()) {
        say(message)
        false
    } else {
        true
    }
}

/**
 * Return one of the call arguments at random.
 */
fun <T> random(vararg args: T): T {
    if (args.isEmpty()) {
        throw IllegalArgumentException()
    }
    return args[random.nextInt(args.size)]
}

/**
 * Execute the [action] with the specified probability.
 */
internal fun withProbability(probability: Double, action: ()->Unit) {
    if (random.nextDouble() < probability) {
        action()
    }
}

fun emptyVocabulary() = mutableMapOf<String, Action>()