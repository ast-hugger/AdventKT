package com.github.vassilibykov.adventkt

/**
 * A special verb bound to a movement direction such as "north",
 * so a direction by itself may be used as a command.
 */
abstract class MovementVerb(word: String) : Verb(word) {
    internal fun movePlayer(where: String, world: World) {
        val direction = Direction.lookup(where)
        if (direction == null) {
            println("You can't go to '$where'")
            return
        }
        val destination = world.player.room.exits[direction]
        if (destination == null) {
            println("You can't go $direction.")
            return
        }
        world.player.moveTo(destination)
    }
}