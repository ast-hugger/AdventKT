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
 * An item (or a character). An item can be picked up and carried by the player,
 * unless the item specifically prohibits that as part of the game logic. An
 * item can prohibit that by overriding [approveMoveTo], or by inheriting from
 * [Fixture] instead of this class.
 *
 * @author Vassili Bykov
 */

open class Item (
        private vararg val _names: String,
        private val owned: String,
        private val dropped: String)
    : World.Configurable
{
    internal constructor(vararg names: String)
            : this(*names, owned = "<no inventoryDescription() for $names[0]>", dropped = "no description() for $names[0]")

    var owner = LIMBO
        internal set
    val names = _names.toSet()
    internal var isPlural = false
    internal var dynamicDescription: (() -> String)? = null
    internal var dynamicInventoryDescription: (() -> String)? = null
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
    open val description
        get() = dynamicDescription?.invoke() ?: dropped

    /**
     * The description of the item to display when it's in the player's inventory.
     */
    open val inventoryDescription
        get() = dynamicInventoryDescription?.invoke() ?: owned

    /**
     * Whether to skip the item when printing a full room description.
     */
    open val isHidden = false

    private val vocabulary = emptyVocabulary()
    private val vicinityVocabulary = emptyVocabulary()

    internal var configurator: (Item.()->Unit)? = null
    private val moveApprovers = mutableListOf<Item.(ItemOwner)->Boolean>()

    override fun configure() {
        configurator?.invoke(this)
    }

    internal fun allowMove(approver: Item.(ItemOwner)->Boolean) = moveApprovers.add(approver)

    internal fun allowMoveTo(ownerOfInterest: ItemOwner, approver: Item.()->Boolean) {
        allowMove { newOwner ->
            if (newOwner == ownerOfInterest) approver(this) else true
        }
    }

    internal fun decline(message: String): Boolean {
        say(message)
        return false
    }

    internal fun declineIf(condition: ()->Boolean, message: String): Boolean {
        return if (condition()) {
            say(message)
            false
        } else {
            true
        }
    }

    open fun approveMoveTo(newOwner: ItemOwner): Boolean {
        return moveApprovers.fold(true, {b, it -> b && it(this, newOwner)})
    }

    fun action(vararg words: String, effect: ItemAction.()->Unit): ItemAction {
        @Suppress("UNCHECKED_CAST")
        val verb = ItemAction(this, listOf(*words), effect = effect as LocalAction.() -> Unit)
        verb.addTo(vocabulary)
        return verb
    }

    fun vicinityAction(vararg words: String, effect: ItemAction.()->Unit): ItemAction {
        @Suppress("UNCHECKED_CAST")
        val verb = ItemAction(this, listOf(*words), effect = effect as LocalAction.() -> Unit)
        verb.addTo(vicinityVocabulary)
        return verb
    }

    fun findAction(word: String) = vocabulary[word]

    fun findVicinityAction(word: String) = vicinityVocabulary[word]

    /**
     * Move the item to a new owner. This is the standard method of doing so,
     * invoking approval methods of all parties involved and thus giving them
     * the chance to veto the move. The parties involved are also notified
     * once the move is complete.
     */
    fun moveTo(newOwner: ItemOwner) {
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
