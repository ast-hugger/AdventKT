package com.github.vassilibykov.adventkt

/**
 *
 * @author vassili
 */
class Player(private var _room: Room) : ItemOwner {
    val inventory = mutableListOf<Item>()

    val room
        get() = _room

    fun moveTo(anotherRoom: Room) {
        if (room.approvePlayerMoveTo(anotherRoom) && anotherRoom.approvePlayerMoveFrom(room)) {
            uncheckedMoveTo(anotherRoom)
        }
    }

    fun uncheckedMoveTo(anotherRoom: Room) {
        _room = anotherRoom
    }

    override fun items(): Collection<Item> {
        return inventory
    }

    override fun privateAddItem(item: Item) {
        inventory.add(item)
    }

    override fun privateRemoveItem(item: Item) {
        inventory.remove(item)
    }
}