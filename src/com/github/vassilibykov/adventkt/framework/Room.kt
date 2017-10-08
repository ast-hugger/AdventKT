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

package com.github.vassilibykov.adventkt.framework

import com.github.vassilibykov.adventkt.cave.DirectionAction

typealias PlayerApprover = Room.(Room) -> Boolean
typealias PlayerReactor = Room.(Room) -> Unit
typealias ItemApprover = Room.(ItemOwner, Item) -> Boolean
typealias ItemReactor = Room.(ItemOwner, Item) -> Unit

/**
 * A game location. Not intended to be instantiated directly or subclassed,
 * instead typically created and configured using the `room()` or the
 * `darkRoom()` DSL clause.
 *
 * @see [World.room]
 * @see [World.darkRoom]
 *
 * @author Vassili Bykov
 */
open class Room(private val _shortDescription: String, _description: String) : World.Configurable, ItemOwner {

    open val description =_description.lines().map{ it.trim() }.joinToString("\n")
    open val shortDescription
        get() = _shortDescription
    override val items = mutableListOf<Item>()
    internal val exits = mutableMapOf<Direction, RoomExit>()
    private val vocabulary = mutableListOf<Action>()
    var visited = false

    // Exits to standard directions, introducing DSL statements
    internal val north = RoomExit(this, StandardDirection.NORTH)
    internal val northeast = RoomExit(this, StandardDirection.NORTHEAST)
    internal val east = RoomExit(this, StandardDirection.EAST)
    internal val southeast = RoomExit(this, StandardDirection.SOUTHEAST)
    internal val south = RoomExit(this, StandardDirection.SOUTH)
    internal val southwest = RoomExit(this, StandardDirection.SOUTHWEST)
    internal val west = RoomExit(this, StandardDirection.WEST)
    internal val northwest = RoomExit(this, StandardDirection.NORTHWEST)
    internal val up = RoomExit(this, StandardDirection.UP)
    internal val down = RoomExit(this, StandardDirection.DOWN)
    internal val `in` = RoomExit(this, StandardDirection.IN)
    internal val out = RoomExit(this, StandardDirection.OUT)

    internal val detail = RoomDetailBuilder(this)

    internal inner class CustomExitBuilder {
        infix fun named(name: String): RoomExit {
            val direction = CustomDirection(name)
            vocabulary.add(DirectionAction(name))
            return RoomExit(this@Room, direction)
        }
    }
    internal val exit = CustomExitBuilder()

    internal var configurator: (Room.()->Unit)? = null
    private val playerMoveInApprovers = mutableListOf<PlayerApprover>()
    private val playerMoveOutApprovers = mutableListOf<PlayerApprover>()
    private val playerMoveInReactors = mutableListOf<PlayerReactor>()
    private val playerMoveOutReactors = mutableListOf<PlayerReactor>()
    private val itemMoveInApprovers = mutableListOf<ItemApprover>()
    private val itemMoveOutApprovers = mutableListOf<ItemApprover>()
    private val itemMoveInReactors = mutableListOf<ItemReactor>()
    private val itemMoveOutReactors = mutableListOf<ItemReactor>()
    private var turnEndAction = {}

    /*
        DSLish stuff

        Internal methods intended to be used in configuration blocks of Rooms.
     */

    /**
     * Declare an item as belonging to this room. The room becomes the owner
     * of the item, unless the item already has a (non-LIMBO) owner.
     */
    internal operator fun Item.unaryMinus() {
        if (this isIn Item.LIMBO) {
            primitiveMoveTo(this@Room)
        } else {
            this@Room.items.add(this)
        }
    }

    /**
     * Declare a room-specific action. The action is only considered by the
     * parser when the player is in this room.
     */
    internal fun action(vararg words: String, effect: LocalAction.()->Unit): LocalAction {
        val verb = LocalAction(listOf(*words), effect = effect)
        vocabulary.add(verb)
        return verb
    }

    /**
     * Declare a predicate evaluated before a player is moved to this room. If
     * the predicate returns false, the move is vetoed. The room the player is
     * about to move from is passed as an argument to the predicate.
     *
     * @see Player.moveTo
     */
    internal fun allowPlayerEntry(a: PlayerApprover) = playerMoveInApprovers.add(a)

    /**
     * Declare a reaction block evaluated after the player has been moved into
     * this room. The room the player has moved from is passed as an argument to
     * the block.
     *
     * @see Player.moveTo
     */
    internal fun onPlayerEntry(r: PlayerReactor) = playerMoveInReactors.add(r)

    /**
     * Declare a predicate evaluated before a player is moved out of this room.
     * If the predicate returns false, the move is vetoed. The room the player
     * is about to to from is passed as an argument to the predicate.
     *
     * @see Player.moveTo
     */
    internal fun allowPlayerExit(a: PlayerApprover) = playerMoveOutApprovers.add(a)

    /**
     * Declare a reaction block evaluated after the player has been moved out of
     * this room. The room the player has move to is passed as an argument to
     * the block.
     *
     * @see Player.moveTo
     */
    internal fun onPlayerExit(r: PlayerReactor) = playerMoveOutReactors.add(r)

    /**
     * Declare a predicate evaluated before any item is moved into this room.
     * The current owner and the item are passed as arguments to the predicate.
     * If the predicate returns false, the move is vetoed.
     *
     * @see Item.moveTo
     */
    internal fun allowItemMoveIn(approver: ItemApprover) = itemMoveInApprovers.add(approver)

    /**
     * Declare a predicate evaluated before the specified item is moved into this room.
     * The current owner is passed as an argument to the predicate.
     * If the predicate returns false, the move is vetoed.
     *
     * @see Item.moveTo
     */
    internal fun allowItemMoveIn(item: Item, approver: Room.(ItemOwner)->Boolean) {
        allowItemMoveIn { oldRoom, movedItem ->
            if (movedItem == item) approver(this, oldRoom) else true
        }
    }

    /**
     * Declare a predicate evaluated before any item is moved out of this room.
     * The future owner and the item are passed as arguments to the predicate.
     * If the predicate returns false, the move is vetoed.
     *
     * @see Item.moveTo
     */
    internal fun allowItemMoveOut(approver: ItemApprover) = itemMoveOutApprovers.add(approver)

    /**
     * Declare a predicate evaluated before the specified item is moved into this room.
     * The future owner is passed as an argument to the predicate.
     * If the predicate returns false, the move is vetoed.
     *
     * @see Item.moveTo
     */
    internal fun allowItemMoveOut(item: Item, approver: Room.(ItemOwner)->Boolean) {
        allowItemMoveOut { newRoom, movedItem ->
            if (movedItem == item) approver(this, newRoom) else true
        }
    }

    /**
     * Declare a reaction block evaluated after any item is moved into this room.
     * The item's old owner and the item itself are passed as arguments to the block.
     *
     * @see Item.moveTo
     */
    internal fun onItemMoveIn(reactor: ItemReactor) = itemMoveInReactors.add(reactor)

    // The following and its equivalent onItemMoveOut cause overload resolution ambiguity.
    // The ambiguity seems fishy but what can we do.

//    internal fun onItemMoveIn(item: Item, reactor: Room.(ItemOwner)->Unit) {
//        onItemMoveIn { oldOwner, movedItem -> if (movedItem == item) reactor(this, oldOwner) }
//    }

    /**
     * Declare a reaction block evaluated after the specified item is moved into
     * this room.
     *
     * @see Item.moveTo
     */
    internal fun onItemMoveIn(item: Item, reactor: Room.()->Unit) {
        onItemMoveIn { _, movedItem -> if (movedItem == item) reactor(this) }
    }

    /**
     * Declare a reaction block evaluated after any item is moved out of this room.
     * The item's new owner and the item itself are passed as arguments to the block.
     *
     * @see Item.moveTo
     */
    internal fun onItemMoveOut(reactor: ItemReactor) = itemMoveOutReactors.add(reactor)

    /**
     * Declare a reaction block evaluated after the specified item is moved out
     * of this room.
     *
     * @see Item.moveTo
     */
    internal fun onItemMoveOut(item: Item, reactor: Room.()->Unit) {
        onItemMoveOut { _, movedItem -> if (movedItem == item) reactor(this) }
    }

    /**
     * Declare a reaction block evaluated after every player command (which may
     * be a non-movement command such as "turn on lamp") at the end of which the
     * player is in this room.
     */
    internal fun onTurnEnd(action: ()->Unit) {
        turnEndAction = action
    }

    val deferredOutput = mutableListOf<String>()

    internal fun sayLater(message: String) = deferredOutput.add(message)

    internal operator fun String.unaryPlus() {
        say(this)
    }

    /*
        World object mechanics
     */

    override fun configure(context: World.ConfigurationContext) {
        configurator?.invoke(this)
        // Some of the items may be declared directly in the room declaration
        // and not seen by the world's reflective configuration code, so we
        // need to make sure they are configured.
        items.forEach { context.configure(it) }
    }

    /**
     * Invoked when the player is about to be moved to this room from another.
     * The method may return false to veto the move. In this case, it typically
     * should print a game message explaining why the move hasn't happened.
     *
     * Specific rooms should typically use the `allow` set of DSL declarations
     * instead of overriding this.
     */
    open fun approvePlayerMoveIn(oldRoom: Room): Boolean {
        return playerMoveInApprovers.fold(true, {b, approver -> b && approver(this, oldRoom)})
    }

    /**
     * Invoked when the player is about to be moved to another room from this
     * one. The method may return false to veto the move. In this case, it
     * typically should print a game message explaining why the move hasn't
     * happened.
     */
    open fun approvePlayerMoveOut(newRoom: Room): Boolean {
        return playerMoveOutApprovers.fold(true, {b, approver -> b && approver(this, newRoom)})
    }

    /**
     * Invoked after the player leaves this room for another room. Room
     * definitions should typically use the [onPlayerExit] DSL method instead
     * of overriding this.
     */
    open fun noticePlayerMoveTo(newRoom: Room) {
        playerMoveOutReactors.forEach { it(this, newRoom) }
    }

    /**
     * Invoked after the player enters this room. Room definitions should
     * typically use the [onPlayerEntry] DSL method instead of overriding this.
     *
     * If overridden, the overriding method must call this implementation.
     */
    open fun noticePlayerMoveFrom(oldRoom: Room) {
        deferredOutput.clear()
        playerMoveInReactors.forEach { it(this, oldRoom) }
        printDescription()
        visited = true
        deferredOutput.forEach { say(it) }
        deferredOutput.clear()
    }

    /**
     * Invoked after every game command processed by the game for the room
     * the player is in at the end of the move.
     */
    open fun noticeTurnEnd() = turnEndAction()

    override fun approveItemMoveTo(newOwner: ItemOwner, item: Item): Boolean {
        return itemMoveOutApprovers.fold(true, {b, it -> b && it(this, newOwner, item)})
    }

    override fun approveItemMoveFrom(oldOwner: ItemOwner, item: Item): Boolean {
        return itemMoveInApprovers.fold(true, {b, it -> b && it(this, oldOwner, item)})
    }

    override fun noticeItemMoveTo(newOwner: ItemOwner, item: Item) {
        itemMoveOutReactors.forEach { it(this, newOwner, item) }
    }

    override fun noticeItemMoveFrom(oldOwner: ItemOwner, item: Item) {
        itemMoveInReactors.forEach { it(this, oldOwner, item) }
    }

    /**
     * Print the description of the room and all the items in it.
     * This is what prints the blurb before every input prompt.
     */
    open fun printDescription() {
        say(if (visited) shortDescription else description)
        val visibleItems = items.filter { !it.isHidden }
        if (!visibleItems.isEmpty()) {
            println()
            visibleItems.forEach { say(it.description) }
        }
    }

    open fun printFullDescription() {
        say(description)
        val visibleItems = items.filter { !it.isHidden }
        if (!visibleItems.isEmpty()) {
            println()
            visibleItems.forEach { say(it.description) }
        }
    }

    open fun exitTo(direction: Direction): Room? = exits[direction]?.target

    open fun exitTo(directionName: String): Room? {
        val dir = exits.keys.firstOrNull {
            it.name.equals(directionName, ignoreCase = true) || it.shortcut.equals(directionName, ignoreCase = true)
        }
        return dir?.let { exitTo(it) }
    }

    fun findAction(word: String): Action? = vocabulary.find { word in it.words }

    override fun primitiveAddItem(item: Item) {
        items.add(item)
    }

    override fun primitiveRemoveItem(item: Item) {
        items.remove(item)
    }

    override fun toString() = "Room \"$shortDescription\""

    companion object {
        /**
         * The player is created in this "room" and moved into the starting
         * room when the game begins, thus producing the opening room description.
         */
        val NOWHERE = object : Room("You're nowhere.", "This is exactly what the middle of nowhere looks like.") {
            override fun configure(context: World.ConfigurationContext) {
            }
        }
    }
}
