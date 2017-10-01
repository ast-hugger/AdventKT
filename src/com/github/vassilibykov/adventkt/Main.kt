package com.github.vassilibykov.adventkt

/**
 *
 * @author vassili
 */

var cave = ColossalCave()
    private set
var player = cave.player
    private set
var lantern = cave.lantern
    private set

fun main(args: Array<String>) {
    cave.runObjectInitializers()
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
    cave = ColossalCave()
    cave.runObjectInitializers()
    player = cave.player
    lantern = cave.lantern
}

class QuitException: RuntimeException()