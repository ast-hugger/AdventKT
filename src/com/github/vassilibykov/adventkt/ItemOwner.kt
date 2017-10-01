package com.github.vassilibykov.adventkt

interface ItemOwner {

    val items: Collection<Item>

    /**
     * Indicate whether the player currently owns the item.
     */
    infix fun has(item: Item) = item in items

    /**
     * If the player has an item identified by the word, return it.
     */
    fun findItem(word: String) = items.find { word in it.names }

    fun approveItemMoveTo(newOwner: ItemOwner, item: Item) = true

    fun approveItemMoveFrom(owner: ItemOwner, item: Item) = true

    fun noticeItemMoveTo(newOwner: ItemOwner, item: Item) = Unit

    fun noticeItemMoveFrom(oldOwner: ItemOwner, item: Item) = Unit

    fun primitiveAddItem(item: Item)

    fun primitiveRemoveItem(item: Item)

}