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

import com.github.vassilibykov.adventkt.cave.player

/**
 * A specialized action identified by a movement direction such as "north", so a
 * direction by itself can be used as a command.
 *
 * @author Vassili Bykov
 */
abstract class MovementAction(word: String) : Action(word) {
    internal fun movePlayer(where: String) {
        val direction = Direction.named(where)
        if (direction == null) {
            println("You can't go to '$where'")
        } else {
            val destination = player.room.exitTo(direction)
            if (destination == null) {
                println("You can't go $direction.")
            } else
                player.moveTo(destination)
        }
    }
}