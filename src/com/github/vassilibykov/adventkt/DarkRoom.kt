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

package com.github.vassilibykov.adventkt

/**
 * The superclass of rooms that have no natural lights and require the player to
 * carry a lantern, or the lantern to be in the room, for the player to see.
 *
 * @author Vassili Bykov
 */
class DarkRoom(shortDescription: String, description: String) : Room(shortDescription, description) {
    private val DARK_MESSAGE = "It is now pitch dark. If you proceed you will likely fall into a pit."

    override fun printDescription() {
        if ((player has lantern || player.room has lantern) && lantern.light.isOn) {
            super.printDescription()
        } else {
            say(DARK_MESSAGE)
        }
    }

    override fun printFullDescription() {
        if ((player has lantern || player.room has lantern) && lantern.light.isOn) {
            super.printFullDescription()
        } else {
            say(DARK_MESSAGE)
        }
    }
}