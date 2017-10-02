package com.github.vassilibykov.adventkt

/**
 *
 * @author Vassili Bykov
 */
class Player : ItemOwner {

    /**
     * The items currently owned by the player.
     */
    override val items = mutableListOf<Item>()

    /**
     * The room the player is in.
     */
    var room = Room.NOWHERE
        private set

    /**
     * Relocate the player to a different room. Both rooms involved are asked
     * for approval. Either room can veto the move, possibly generating some
     * game output. If the move is approved, the player is relocated and then
     * both rooms are sent a move notification.
     */
    fun moveTo(newRoom: Room) {
        val oldRoom = room
        if (room.approvePlayerMoveTo(newRoom) && newRoom.approvePlayerMoveFrom(room)) {
            room = newRoom
            oldRoom.noticePlayerMoveTo(newRoom)
            newRoom.noticePlayerMoveFrom(oldRoom)
        }
    }

    override fun noticeItemMoveFrom(oldOwner: ItemOwner, item: Item) {
        if (oldOwner is Room) {
            say("You are now carrying the $item.")
        }
    }

    override fun noticeItemMoveTo(newOwner: ItemOwner, item: Item) {
        if (newOwner is Room) {
            say("You dropped the $item.")
        }
    }

    override fun primitiveAddItem(item: Item) {
        items.add(item)
    }

    override fun primitiveRemoveItem(item: Item) {
        items.remove(item)
    }
}