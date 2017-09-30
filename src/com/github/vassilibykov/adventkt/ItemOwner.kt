package com.github.vassilibykov.adventkt

interface ItemOwner {

    fun items(): Collection<Item>

    infix fun has(item: Item) = item in items()

    /**
     * Return an item identified by the word.
     */
    fun findItem(word: String) = items().find { word in it.names }

    fun ownItem(item: Item) {
        item.owner.privateRemoveItem(item)
        item.owner = this
        privateAddItem(item)
    }

    fun privateAddItem(item: Item)

    fun privateRemoveItem(item: Item)

    fun approveMoveTo(newOwner: ItemOwner, item: Item) = true

    fun approveMoveFrom(owner: ItemOwner, item: Item) = true

}