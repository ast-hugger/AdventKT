package com.github.vassilibykov.adventkt

/**
 * A boolean property of a room or an item which can be turned on and off
 * by player actions. Each state change, as well as an attempt to change
 * the state when the toggle is already in that state, produces a player
 * message.
 */
class Toggle(private var state: Boolean,
             val turnOnMessage: String,
             val turnOffMessage: String,
             val alreadyOnMessage: String,
             val alreadyOffMessage: String)
{
    val isOn
        get() = state

    fun turnOn() {
        say(if (state) alreadyOnMessage else turnOnMessage)
        state = true
    }

    fun turnOff() {
        say(if (state) turnOffMessage else alreadyOffMessage)
        state = false
    }
}