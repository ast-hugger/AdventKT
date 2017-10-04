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

typealias PlayerApprover = Room.(Room) -> Boolean
typealias PlayerReactor = Room.(Room) -> Unit
typealias ItemApprover = Room.(ItemOwner, Item) -> Boolean
typealias ItemReactor = Room.(ItemOwner, Item) -> Unit

/**
 * A game location. Not intended to be instantiated directly or subclassed,
 * instead typically created and configured using the `litRoom()` or the
 * `darkRoom()` DSL clause.
 *
 * @see [World.litRoom]
 * @see [World.darkRoom]
 *
 * @author Vassili Bykov
 */
open class Room(private val _shortDescription: String, _description: String) : World.Configurable, ItemOwner {

    open val description =_description.lines().map{ it.trim() }.joinToString("\n")
    open val shortDescription
        get() = _shortDescription
    override val items = mutableListOf<Item>()
    private val exits = mutableMapOf<Direction, Room>()
    private val vocabulary = mutableListOf<Action>()
    var visited = false

    internal var configurator: (Room.()->Unit)? = null
    private val playerMoveInApprovers = mutableListOf<PlayerApprover>()
    private val playerMoveOutApprovers = mutableListOf<PlayerApprover>()
    private val playerMoveInReactors = mutableListOf<PlayerReactor>()
    private val playerMoveOutReactors = mutableListOf<PlayerReactor>()
    private val itemMoveInApprovers = mutableListOf<ItemApprover>()
    private val itemMoveOutApprovers = mutableListOf<ItemApprover>()
    private val itemMoveInReactors = mutableListOf<ItemReactor>()
    private val itemMoveOutReactors = mutableListOf<ItemReactor>()

    /*
        DSLish stuff

        Internal methods intended to be used in configuration blocks of Rooms.
     */

    /**
     * Declare a two-way passage between this room and [target].
     * The passage leaves this room in the specified direction(s),
     * and the target room in the opposite direction(s).
     */
    internal fun twoWay(target: Room, vararg directions: Direction) {
        for (direction in directions) {
            if (exits.containsKey(direction)) {
                throw IllegalArgumentException("exit to $direction already exists in $_shortDescription")
            }
            val opposite = direction.opposite()
            if (target.exits.containsKey(opposite)) {
                throw IllegalArgumentException("exit to $opposite already exists in $target._shortDescription")
            }
            addExit(direction, target)
            target.addExit(opposite, this)
        }
    }

    /**
     * Declare a one-way passage between this room and [target].
     * The passage leaves this room in the specified direction(s).
     */
    internal fun oneWay(target: Room, vararg directions: Direction) {
        for (direction in directions) {
            if (exits.containsKey(direction)) {
                throw IllegalArgumentException("exit to $direction already exists")
            }
            addExit(direction, target)
        }
    }

    /**
     * Declare an item which belongs to this room.
     */
    internal fun here(item: Item): Item {
        item.primitiveMoveTo(this)
        return item
    }

    /**
     * Declare an item which officially belongs to another location, but is also
     * visible in this room.
     */
    internal fun hereShared(item: Item): Item {
        items.add(item)
        return item
    }

    /**
     * Declare a detail: a hidden item which can be looked at, producing the specified
     * description message.
     */
    internal fun detail(vararg names: String, extraVerbs: Collection<String> = setOf(), message: String): Detail {
        val detail = Detail(*names, extraVerbs = extraVerbs, message = message)
        here(detail)
        return detail
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
    internal fun allowPlayerMoveIn(a: PlayerApprover) = playerMoveInApprovers.add(a)

    /**
     * Declare a reaction block evaluated after the player has been moved into
     * this room. The room the player has moved from is passed as an argument to
     * the block.
     *
     * @see Player.moveTo
     */
    internal fun onPlayerMoveIn(r: PlayerReactor) = playerMoveInReactors.add(r)

    /**
     * Declare a predicate evaluated before a player is moved out of this room.
     * If the predicate returns false, the move is vetoed. The room the player
     * is about to to from is passed as an argument to the predicate.
     *
     * @see Player.moveTo
     */
    internal fun allowPlayerMoveOut(a: PlayerApprover) = playerMoveOutApprovers.add(a)

    /**
     * Declare a reaction block evaluated after the player has been moved out of
     * this room. The room the player has move to is passed as an argument to
     * the block.
     *
     * @see Player.moveTo
     */
    internal fun onPlayerMoveOut(r: PlayerReactor) = playerMoveOutReactors.add(r)

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

//    internal fun onItemMoveOut(item: Item, reactor: Room.(ItemOwner)->Unit) {
//        onItemMoveOut { oldRoom, movedItem -> if (movedItem == item) reactor(this, oldRoom) }
//    }

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
     * Evaluate the [condition]. If true, print the specified message and return false.
     * Otherwise, silently return true. Intended to be used as part of `allow`
     * declarations, for example
     *
     *     allowPlayerMoveOut { _ ->
     *       declineIf({ player has forbiddenItem },
     *         "You are not allowed to take $forbiddenItem out of the room.")
     *     }
     */
    internal fun declineIf(condition: ()->Boolean, message: String): Boolean {
        return if (condition()) {
            say(message)
            false
        } else {
            true
        }
    }

    val deferredOutput = mutableListOf<String>()

    internal fun sayLater(message: String) = deferredOutput.add(message)

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
     * definitions should typically use the [onPlayerMoveOut] DSL method instead
     * of overriding this.
     */
    open fun noticePlayerMoveTo(newRoom: Room) {
        playerMoveOutReactors.forEach { it(this, newRoom) }
    }

    /**
     * Invoked after the player enters this room. Room definitions should
     * typically use the [onPlayerMoveIn] DSL method instead of overriding this.
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

    open fun exitTo(direction: Direction): Room? = exits[direction]

    fun findAction(word: String): Action? = vocabulary.find { word in it.words }

    private fun addExit(direction: Direction, target: Room) = exits.put(direction, target)

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
