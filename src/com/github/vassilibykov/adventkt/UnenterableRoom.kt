package com.github.vassilibykov.adventkt

/**
 * A room that refuses any player's attempt to enter,
 * printing the specified explanatory message.
 *
 * @author Vassili Bykov
 */
class UnenterableRoom(private val entryRefusedMessage: String) : Room("", "") {

    override fun configure() = Unit

    override fun approvePlayerMoveFrom(oldRoom: Room): Boolean {
        say(entryRefusedMessage)
        return false
    }
}