package com.github.vassilibykov.adventkt

/**
 *
 * @author Vassili Bykov
 */

var cave = ColossalCave.create()
    private set
var player = cave.player
    private set
var lantern = cave.lantern
    private set

fun main(args: Array<String>) {
   cave.play()
}

// for tests only
fun reset() {
    cave = ColossalCave.create()
    player = cave.player
    lantern = cave.lantern
}
