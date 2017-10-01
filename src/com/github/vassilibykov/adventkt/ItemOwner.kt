package com.github.vassilibykov.adventkt

interface ItemOwner {

    fun items(): Collection<Item>

    infix fun has(item: Item) = item in items()

    /**
     * Return an item identified by the word.
     */
    fun findItem(word: String) = items().find { word in it.names }

    fun approveItemMoveTo(newOwner: ItemOwner, item: Item) = true

    fun approveItemMoveFrom(owner: ItemOwner, item: Item) = true

    fun noticeItemMoveTo(newOwner: ItemOwner, item: Item) = Unit

    fun noticeItemMoveFrom(oldOwner: ItemOwner, item: Item) = Unit

    fun privilegedAddItem(item: Item)

    fun privilegedRemoveItem(item: Item)

}