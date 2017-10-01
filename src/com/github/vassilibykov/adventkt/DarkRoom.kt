package com.github.vassilibykov.adventkt

/**
 * The superclass of rooms that have no lights and require the player
 * to have a lantern to see things.
 */
abstract class DarkRoom(description: String, shortDescription: String) : Room(shortDescription, description) {
    val MESSAGE = "It is now pitch dark. If you proceed you will likely fall into a pit."

    override fun printDescription() {
        if ((player has lantern || player.room has lantern) && lantern.light.isOn) {
            super.printDescription()
        } else {
            say(MESSAGE)
        }
    }

    override fun printFullDescription() {
        if ((player has lantern || player.room has lantern) && lantern.light.isOn) {
            super.printFullDescription()
        } else {
            say(MESSAGE)
        }
    }
}