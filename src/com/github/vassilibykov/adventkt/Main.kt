package com.github.vassilibykov.adventkt

/**
 *
 * @author vassili
 */

val cave = ColossalCave()
val player = cave.player
val lantern = cave.lantern

fun main(args: Array<String>) {
    val commandProcessor = Parser()
    var priorRoom: Room? = null
    try {
        while (true) {
            val room = player.room
            if (room != priorRoom) {
                room.printDescription()
                room.visited = true
                priorRoom = room
            }
            print("> ")
            val command = readLine() ?: "quit"
            commandProcessor.process(command)
        }
    } catch (e: QuitException) {
        println("Leaving.")
    }
}

class QuitException: RuntimeException()