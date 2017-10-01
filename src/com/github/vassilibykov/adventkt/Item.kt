package com.github.vassilibykov.adventkt

/**
 *
 * @author vassili
 */

open class Item (
        private vararg val _names: String,
        private val owned: String,
        private val dropped: String)
    : World.Object
{
    internal constructor(vararg names: String)
            : this(*names, owned = "<no inventoryDescription() for $names[0]>", dropped = "no description() for $names[0]")

    val primaryName
        get() = _names[0]
    val names = _names.toSet()
    var owner = LIMBO
        internal set

    private val vocabulary = emptyVocabulary()
    private val vicinityVocabulary = emptyVocabulary()

    override fun setup() = Unit

    /**
     * Return a description of the item to display when it's in the player's inventory.
     */
    open fun inventoryDescription() = owned

    /**
     * Return a description of the item displayed when the item is
     * NOT owned by the player.
     */
    open fun description(): String = dropped

    /**
     * Indicate whether to skip the item when printing a full room description.
     */
    open fun isHidden(): Boolean = false

    /**
     * Indicate whether the item can be taken by the player.
     */
    open fun canBeTaken(): Boolean = true

    fun verb(vararg words: String, action: ItemVerb.()->Unit): ItemVerb {
        @Suppress("UNCHECKED_CAST")
        val verb = ItemVerb(this, listOf(*words), action = action as LocalVerb.() -> Unit)
        verb.addTo(vocabulary)
        return verb
    }

    fun vicinityVerb(vararg words: String, action: ItemVerb.()->Unit): ItemVerb {
        @Suppress("UNCHECKED_CAST")
        val verb = ItemVerb(this, listOf(*words), action = action as LocalVerb.() -> Unit)
        verb.addTo(vicinityVocabulary)
        return verb
    }

    fun findVerb(word: String) = vocabulary[word]

    fun findVicinityVerb(word: String) = vicinityVocabulary[word]

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
            quietlyMoveTo(newOwner)
        }
    }

    /**
     * Unconditionally relocate this item to a new owner, without getting any
     * associated approvals. Do notify the item and the owners.
     */
    fun quietlyMoveTo(newOwner: ItemOwner) {
        val oldOwner = owner
        oldOwner.privilegedRemoveItem(this)
        owner = newOwner
        newOwner.privilegedAddItem(this)
        noticeMove(newOwner, oldOwner)
        oldOwner.noticeItemMoveTo(newOwner, this)
        newOwner.noticeItemMoveFrom(oldOwner, this)
    }

    open fun approveMoveTo(newOwner: ItemOwner) = true

    open fun noticeMove(newOwner: ItemOwner, oldOwner: ItemOwner) = Unit

    companion object {
        val LIMBO = object : ItemOwner {
            val items = mutableListOf<Item>()

            override fun items() = items

            override fun privilegedAddItem(item: Item) {
                items.add(item)
            }

            override fun privilegedRemoveItem(item: Item) {
                items.remove(item)
            }
        }

        fun nearby(names: Collection<String>): Item? {
            return player.inventory.find { it.names.intersect(names).isNotEmpty() }
                ?: player.room.items.find { it.names.intersect(names).isNotEmpty() }
        }
    }
}
