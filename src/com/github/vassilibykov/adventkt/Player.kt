package com.github.vassilibykov.adventkt

/**
 *
 * @author vassili
 */
class Player(private var _room: Room) : ItemOwner {
    val inventory = mutableListOf<Item>()

    val room
        get() = _room

    fun moveTo(newRoom: Room) {
        if (room.approvePlayerMoveTo(newRoom) && newRoom.approvePlayerMoveFrom(room)) {
            quietlyMoveTo(newRoom)
        }
    }

    fun quietlyMoveTo(newRoom: Room) {
        val oldRoom = room
        _room = newRoom
        oldRoom.noticePlayerMoveTo(newRoom)
        newRoom.noticePlayerMoveFrom(oldRoom)
    }

    override fun items(): Collection<Item> {
        return inventory
    }

    override fun privilegedAddItem(item: Item) {
        inventory.add(item)
    }

    override fun privilegedRemoveItem(item: Item) {
        inventory.remove(item)
    }
}