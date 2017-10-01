package com.github.vassilibykov.adventkt

/**
 *
 * @author vassili
 */
abstract class Room(private val _shortDescription: String, _description: String) : World.Object, ItemOwner {

    open val description =_description.lines().map{ it.trim() }.joinToString("\n")
    open val shortDescription
        get() = _shortDescription
    val exits = mutableMapOf<Direction, Room>()
    override val items = mutableListOf<Item>()
    private val vocabulary = mutableMapOf<String, Action>()
    var visited = false

    /**
     * Print the description of the room and all the items in it.
     * This is what prints the blurb before every input prompt.
     */
    open fun printDescription() {
        say(if (visited) shortDescription else description)
        items.forEach {
            if (!it.isHidden) {
                say(it.description)
            }
        }
    }

    open fun printFullDescription() {
        say(description)
        items.forEach {
            if (!it.isHidden) {
                say(it.description)
            }
        }
    }

    open fun approvePlayerMoveTo(newRoom: Room) = true

    open fun approvePlayerMoveFrom(oldRoom: Room) = true

    open fun noticePlayerMoveTo(newRoom: Room) = Unit

    open fun noticePlayerMoveFrom(oldRoom: Room) = Unit

    fun findAction(word: String): Action? = vocabulary[word]

    fun twoWay(target: Room, vararg directions: Direction) {
        for (direction in directions) {
            addExit(direction, target)
            target.addExit(direction.opposite(), this)
        }
    }

    fun oneWay(target: Room, vararg directions: Direction) {
        for (direction in directions) {
            addExit(direction, target)
        }
    }

    fun item(item: Item) = item.primitiveMoveTo(this)

    fun unownedItem(item: Item) {
        items.add(item)
    }

    fun action(vararg words: String, effect: LocalAction.()->Unit): LocalAction {
        val verb = LocalAction(listOf(*words), effect = effect)
        verb.addTo(vocabulary)
        return verb
    }

    private fun addExit(direction: Direction, target: Room) = exits.put(direction, target)

    override fun primitiveAddItem(item: Item) {
        items.add(item)
    }

    override fun primitiveRemoveItem(item: Item) {
        items.remove(item)
    }

    override fun toString() = "Room: " + shortDescription
}

