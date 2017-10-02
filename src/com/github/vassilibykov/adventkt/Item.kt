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
    val primaryName
        get() = _names[0]

    /**
     * The description of the item displayed when the item is NOT owned by the
     * player.
     */
    open val description = dropped

    /**
     * The description of the item to display when it's in the player's inventory.
     */
    open val inventoryDescription = owned

    /**
     * Whether to skip the item when printing a full room description.
     */
    open val isHidden = false

    private val vocabulary = emptyVocabulary()
    private val vicinityVocabulary = emptyVocabulary()

    override fun configure() = Unit

    fun action(vararg words: String, effect: ItemAction.()->Unit): ItemAction {
        @Suppress("UNCHECKED_CAST")
        val verb = ItemAction(this, listOf(*words), action = effect as LocalAction.() -> Unit)
        verb.addTo(vocabulary)
        return verb
    }

    fun vicinityAction(vararg words: String, effect: ItemAction.()->Unit): ItemAction {
        @Suppress("UNCHECKED_CAST")
        val verb = ItemAction(this, listOf(*words), action = effect as LocalAction.() -> Unit)
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

    open fun approveMoveTo(newOwner: ItemOwner) = true

    open fun noticeMove(newOwner: ItemOwner, oldOwner: ItemOwner) = Unit

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
