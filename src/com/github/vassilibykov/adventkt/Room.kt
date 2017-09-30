package com.github.vassilibykov.adventkt

/**
 *
 * @author vassili
 */
open class Room(_description: String, private val _shortDescription: String) : ItemOwner {

    open val description =_description.lines().map{ it.trim() }.joinToString("\n")
    open val shortDescription
        get() = _shortDescription
    val exits = mutableMapOf<Direction, Room>()
    val items = mutableListOf<Item>()
    private val vocabulary = mutableMapOf<String, Verb>()
    var visited = false

    /**
     * Print the description of the room and all the items in it.
     * This is what prints the blurb before every input prompt.
     */
    open fun printDescription() {
        say(if (visited) shortDescription else description)
        items.forEach {
            if (!it.isHidden()) {
                say(it.description())
            }
        }
    }

    open fun printFullDescription() {
        say(description)
        items.forEach {
            if (!it.isHidden()) {
                say(it.description())
            }
        }
    }

    open fun approvePlayerMoveTo(newRoom: Room) = true

    open fun approvePlayerMoveFrom(oldRoom: Room) = true

    fun findVerb(word: String): Verb? = vocabulary[word]

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

    fun item(item: Item) = item.uncheckedMoveTo(this)

    fun unownedItem(item: Item) {
        items.add(item)
    }

    fun verb(vararg words: String, action: LocalVerb.()->Unit): LocalVerb {
        val verb = LocalVerb(listOf(*words), action = action)
        verb.addTo(vocabulary)
        return verb
    }

    private fun addExit(direction: Direction, target: Room) = exits.put(direction, target)

    override fun items(): Collection<Item> {
        return items
    }

    override fun privateAddItem(item: Item) {
        items.add(item)
    }

    override fun privateRemoveItem(item: Item) {
        items.remove(item)
    }

    override fun toString() = shortDescription
}

