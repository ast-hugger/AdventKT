/*
 * Copyright (c) 2017 Vassili Bykov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        if (room.approvePlayerMoveOut(newRoom) && newRoom.approvePlayerMoveIn(room)) {
            room = newRoom
            oldRoom.noticePlayerMoveTo(newRoom)
            newRoom.noticePlayerMoveFrom(oldRoom)
        }
    }

    fun internalMoveTo(newRoom: Room) {
        room = newRoom
        newRoom.noticePlayerMoveFrom(room)
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