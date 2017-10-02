package com.github.vassilibykov.adventkt

/**
 * An object which can contain [Item]s. Rooms and the player are the most common
 * examples.
 *
 * @author Vassili Bykov
 */
interface ItemOwner {

    val items: Collection<Item>

    /**
     * Indicate whether the player currently owns the item.
     */
    infix fun has(item: Item) = item in items

    /**
     * Return an item one of which names matches the word.
     */
    fun findItem(word: String) = items.find { word in it.names }

    /**
     * Return an item one of which names matches one of the words in the collection.
     */
    fun findItem(words: Collection<String>) = items.find { it.names.any {name -> name in words} }

    fun approveItemMoveTo(newOwner: ItemOwner, item: Item) = true

    fun approveItemMoveFrom(oldOwner: ItemOwner, item: Item) = true

    fun noticeItemMoveTo(newOwner: ItemOwner, item: Item) = Unit

    fun noticeItemMoveFrom(oldOwner: ItemOwner, item: Item) = Unit

    fun primitiveAddItem(item: Item)

    fun primitiveRemoveItem(item: Item)

}