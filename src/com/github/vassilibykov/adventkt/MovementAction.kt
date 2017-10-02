package com.github.vassilibykov.adventkt

/**
 * A special action bound to a movement direction such as "north",
 * so a direction by itself may be used as a command.
 */
abstract class MovementAction(word: String) : Action(word) {
    internal fun movePlayer(where: String) {
        val direction = Direction.named(where)
        if (direction == null) {
            println("You can't go to '$where'")
            return
        }
        val destination = player.room.exitTo(direction)
        if (destination == null) {
            println("You can't go $direction.")
            return
        }
        player.moveTo(destination)
    }
}