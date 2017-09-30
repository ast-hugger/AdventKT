package com.github.vassilibykov.adventkt

/**
 *
 * @author vassili
 */

open class Item(
        private vararg val _names: String,
        private val owned: String,
        private val dropped: String)
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

    open fun approveMove(newOwner: ItemOwner) = true

    fun moveTo(newOwner: ItemOwner) {
        if (approveMove(newOwner)
                && owner.approveMoveTo(newOwner, this)
                && newOwner.approveMoveFrom(owner, this))
        {
            uncheckedMoveTo(newOwner)
        }
    }

    fun uncheckedMoveTo(newOwner: ItemOwner) {
        owner.privateRemoveItem(this)
        owner = newOwner
        newOwner.privateAddItem(this)
    }

    companion object {
        val LIMBO = object : ItemOwner {
            val items = mutableListOf<Item>()

            override fun items() = items

            override fun privateAddItem(item: Item) {
                items.add(item)
            }

            override fun privateRemoveItem(item: Item) {
                items.remove(item)
            }
        }

        fun nearby(names: Collection<String>): Item? {
            return player.inventory.find { it.names.intersect(names).isNotEmpty() }
                ?: player.room.items.find { it.names.intersect(names).isNotEmpty() }
        }
    }
}
