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

private val light = Toggle(false,
        turnedOnMessage = "Your lamp is now turned on.",
        turnedOffMessage = "Your lamp is now turned off.",
        alreadyOnMessage = "Your lamp is already turned on.",
        alreadyOffMessage = "Your lamp is already turned off.")

/**
 * Most of the game objects are defined in ColossalCave, but the lantern
 * is special because it determines the player's ability to see in dark
 * rooms, so it's in fact part of the player.
 *
 * The lantern is a good example of using a Toggle to track its lit/unlit
 * state, producing appropriate messages as the state changes. It is also
 * an example of an item with dynamic descriptions dependent on the state.
 *
 * A lantern defines a local action which turns it on or off it the player
 * is holding the lantern, in which case the action is included in the action
 * lookup sequence. The action is selected for execution when the command
 * subjects (words following the first) include one of the item's names.
 * The action also checks the subjects to see whether to turn the lamp on
 * or off.
 *
 * @author Vassili Bykov
 */
class Lantern : Item("lamp", "lantern",
        owned = { if (light.isOn) "A lit brass lantern" else "A brass lantern" },
        dropped = { if (light.isOn) "There is a brass lamp nearby." else "There is a brass lamp shining nearby." })
{
    val isOn get() = light.isOn

    init {
        action("turn", "switch") {
            when {
                "on" in subjects -> {
                    val wasOn = light.isOn
                    light.turnOn()
                    if (!wasOn) {
                        player.room.printFullDescription()
                    }
                }
                "off" in subjects -> {
                    light.turnOff()
                    if (player.room is DarkRoom && !light.isOn) {
                        say("It is pitch black. You are likely to be eaten by... ummm, nevermind, wrong game.")
                    }
                }
                else -> say("Do you want the lamp on or off?")
            }
        }
    }
}