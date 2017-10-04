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
 * A helper object representing a boolean property that can be changed by the
 * player's actions, with four distinct response messages reported to the player
 * for the four possible combinations of the old and the new state of the
 * property: turnedOn, turnedOff, alreadyOn, and alreadyOff.
 *
 * @author Vassili Bykov
 */
class Toggle(private var state: Boolean,
             val turnedOnMessage: String,
             val turnedOffMessage: String,
             val alreadyOnMessage: String,
             val alreadyOffMessage: String)
{
    val isOn
        get() = state

    fun turnOn() = set(true)

    fun turnOff() = set(false)

    fun set(value: Boolean) {
        if (value == state) {
            say(if (state) alreadyOnMessage else alreadyOffMessage)
        } else {
            say(if (value) turnedOnMessage else turnedOffMessage)
            state = value
        }
    }
}