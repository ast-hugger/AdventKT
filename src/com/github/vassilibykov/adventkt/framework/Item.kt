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

/**
 * An item (or a character). An item can be picked up and carried by the player,
 * unless the item specifically prohibits that as part of the game logic. An
 * item can prohibit that by overriding [approveMoveTo], or by inheriting from
 * [Fixture] instead of this class.
 *
 * Not intended to be instantiated directly or subclassed. Instead, typically
 * created and configured using the `item()` DSL clause.
 *
 * @see World.item
 *
 * @author Vassili Bykov
 */

open class Item (
        private vararg val _names: String,
        private var owned: ()->String,
        private val dropped: ()->String)
    : World.Configurable
{
    constructor(vararg  names: String, owned: String, dropped: String)
            : this(*names, owned = { owned }, dropped = { dropped })

    internal constructor(vararg names: String)
            : this(*names, owned = "<no inventoryDescription() for $names[0]>", dropped = "no description() for $names[0]")

    var owner = LIMBO
        internal set
    val names = _names.toSet()
    internal var isPlural = false
    val primaryName
        get() = _names[0]
    val indefiniteArticle
        get() = when {
            isPlural -> ""
            primaryName[0] in setOf('a', 'o', 'i', 'e') -> "an"
            else -> "a"
        }

    /**
     * The description of the item displayed when the item is NOT owned by the
     * player.
     */
    open val description get() = dropped()

    /**
     * The description of the item to display when it's in the player's inventory.
     */
    open val inventoryDescription get() = owned()

    /**
     * Whether to skip the item when printing a full room description.
     */
    open var isHidden = false

    private val vocabulary = emptyVocabulary()
    private val vicinityVocabulary = emptyVocabulary()

    internal var configurator: (Item.()->Unit)? = null
    private val moveApprovers = mutableListOf<Item.(ItemOwner)->Boolean>()
    private var turnEndAction = {}

    /*
        DSL declarations
     */

    /**
     * Declare a predicate evaluated before the item is moved to a new owner.
     * The predicate receives the proposed owner as the argument.
     * If the predicate returns false, the move is vetoed.
     */
    internal fun allowMove(approver: Item.(ItemOwner)->Boolean) = moveApprovers.add(approver)

    /**
     * Declare a predicate evaluated before the item is moved to the specified owner.
     * If the predicate returns false, the move is vetoed.
     */
    internal fun allowMoveTo(ownerOfInterest: ItemOwner, approver: Item.()->Boolean) {
        allowMove { newOwner ->
            if (newOwner == ownerOfInterest) approver(this) else true
        }
    }

    /**
     * Declare an action which is considered for execution by the parser when
     * the player is holding the item.
     */
    internal fun action(vararg words: String, effect: ItemAction.()->Unit): ItemAction {
        @Suppress("UNCHECKED_CAST")
        val verb = ItemAction(this, listOf(*words), effect = effect as LocalAction.() -> Unit)
        verb.addTo(vocabulary)
        return verb
    }

    /**
     * Declare an action which is considered for execution by the parser when
     * the player is in the same room as the item.
     */
    internal fun vicinityAction(vararg words: String, effect: ItemAction.()->Unit): ItemAction {
        @Suppress("UNCHECKED_CAST")
        val verb = ItemAction(this, listOf(*words), effect = effect as LocalAction.() -> Unit)
        verb.addTo(vicinityVocabulary)
        return verb
    }

    /**
     * Declare a reaction block evaluated after every player command (which may
     * be a non-movement command such as "turn on lamp") at the end of which the
     * item is in the same room as the player, or in the player inventory.
     */

    internal fun onTurnEnd(action: ()->Unit) {
        turnEndAction = action
    }

    internal operator fun String.unaryPlus() {
        say(this)
    }

    /*
        Object model mechanics
     */

    override fun configure(context: World.ConfigurationContext) {
        configurator?.invoke(this)
    }

    open fun approveMoveTo(newOwner: ItemOwner): Boolean {
        return moveApprovers.fold(true, {b, it -> b && it(this, newOwner)})
    }

    private val lookWords = setOf("look", "l")
    private val lookAction = ItemAction(this, lookWords, { say(description) })
    private val lookInventoryAction = ItemAction(this, lookWords, {

        say(inventoryDescription)
    })

    fun findAction(word: String): Action? {
        return vocabulary[word] ?: if (word in lookWords) lookInventoryAction else null
    }

    fun findVicinityAction(word: String): Action? {
        return vicinityVocabulary[word] ?: if (word in lookWords) lookAction else null
    }

    /**
     * Move the item to a new owner. This is the standard method of doing so,
     * invoking approval methods of all parties involved and thus giving them
     * the chance to veto the move. The parties involved are also notified
     * once the move is complete (if it has not been vetoed).
     */
    infix fun moveTo(newOwner: ItemOwner) {
        val oldOwner = owner
        if (approveMoveTo(newOwner)
                && oldOwner.approveItemMoveTo(newOwner, this)
                && newOwner.approveItemMoveFrom(oldOwner, this))
        {
            primitiveMoveTo(newOwner)
            noticeMove(newOwner, oldOwner)
            oldOwner.noticeItemMoveTo(newOwner, this)
            newOwner.noticeItemMoveFrom(oldOwner, this)
        }
    }

    /**
     * Unconditionally relocate this item to a new owner, without getting any
     * associated approvals. Do notify the item and the owners.
     */
    fun primitiveMoveTo(newOwner: ItemOwner) {
        val oldOwner = owner
        oldOwner.primitiveRemoveItem(this)
        owner = newOwner
        newOwner.primitiveAddItem(this)
    }

    open fun noticeMove(newOwner: ItemOwner, oldOwner: ItemOwner) = Unit

    open fun noticeTurnEnd() = turnEndAction()

    /**
     * Return the item's primary name. Some of printing logic relies on this.
     */
    override fun toString(): String = primaryName

    companion object {
        /**
         * An [ItemOwner] an item belongs to when it's not in any of the rooms
         * or the player inventory. Any newly created item is initially in limbo.
         */
        val LIMBO = object : ItemOwner {
            override val items = mutableSetOf<Item>()

            override fun primitiveAddItem(item: Item) {
                items.add(item)
            }

            override fun primitiveRemoveItem(item: Item) {
                items.remove(item)
            }
        }
    }
}
