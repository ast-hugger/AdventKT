package com.github.vassilibykov.adventkt.framework

/**
 * An object owned by the room for each possible exit direction
 * as properties with names such as `north`.
 * Implements DSL statements like
 *
 *     north to mistHall unless { snake isIn this }
 */
class RoomExit(val owner: Room, direction: Direction) {

    var target: Room? = null
        private set

    init {
        owner.exits[direction] = this
    }

    infix fun to(target: Room): RoomExit {
        this.target = target
        return this
    }

    infix fun noEntry(message: String): RoomExit {
        this.target = UnenterableRoom(message)
        return this
    }

    infix fun unless(predicate: ()->Boolean): Guard {
        val guard = Guard(predicate)
        owner.allowPlayerExit { guard.test(it) }
        return guard
    }

    infix fun and(another: RoomExit) = Group(this, another)

    inner class Group(vararg _exits: RoomExit) {
        private val exits = mutableSetOf(*_exits)

        infix fun and(another: RoomExit): Group {
            exits.add(another)
            return this
        }

        infix fun to(target: Room): Group {
            exits.forEach { it.to(target) }
            return this
        }

        infix fun noEntry(message: String): Group {
            to(UnenterableRoom(message))
            return this
        }

        infix fun unless(predicate: ()->Boolean): Guard {
            val guard = Guard(predicate)
            exits.forEach { it.owner.allowPlayerExit { guard.test(it) } }
            return guard
        }
    }

    inner class Guard(private val predicate: () -> Boolean) {
        private var message = "You can't go that way."

        infix fun thenSay(message: String): Guard {
            this.message = message
            return this
        }

        fun test(newRoom: Room): Boolean {
            return if (newRoom == target && predicate()) {
                say(message)
                false
            } else {
                true
            }
        }
    }
}