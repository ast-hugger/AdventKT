package com.github.vassilibykov.adventkt

/**
 *
 * @author vassili
 */

var cave = ColossalCave.create()
    private set
var player = cave.player
    private set
var lantern = cave.lantern
    private set

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

// for tests only
fun reset() {
    cave = ColossalCave.create()
    player = cave.player
    lantern = cave.lantern
}

class QuitException: RuntimeException()