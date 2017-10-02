package com.github.vassilibykov.adventkt

/**
 *
 * @author Vassili Bykov
 */
enum class Direction(val shortcut: String) {

    NORTH("n"),
    NORTHEAST("ne"),
    EAST("e"),
    SOUTHEAST("se"),
    SOUTH("s"),
    SOUTHWEST("sw"),
    WEST("w"),
    NORTHWEST("nw"),
    UP("u"),
    DOWN("d"),
    IN("in"),
    OUT("out");

    fun opposite() = opposites[this] ?: throw IllegalStateException()

    override fun toString(): String {
        return this.name.toLowerCase()
    }

    companion object {
        private val directions = mutableMapOf<String, Direction>()
        private val opposites = mutableMapOf<Direction, Direction>()

        init {
            Direction.values().forEach {
                directions[it.toString()] = it
                directions[it.shortcut] = it
            }
            defineOpposites(NORTH, SOUTH)
            defineOpposites(NORTHWEST, SOUTHEAST)
            defineOpposites(WEST, EAST)
            defineOpposites(SOUTHWEST, NORTHEAST)
            defineOpposites(UP, DOWN)
            defineOpposites(IN, OUT)
        }

        val directionNames get() = directions.keys

        val compassDirections = setOf(NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST)

        private fun defineOpposites(dir1: Direction, dir2: Direction) {
            opposites[dir1] = dir2
            opposites[dir2]= dir1
        }

        fun named(name: String) = directions[name]
    }
}



