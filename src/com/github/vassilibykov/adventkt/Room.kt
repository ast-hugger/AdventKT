package com.github.vassilibykov.adventkt

/**
 *
 * @author Vassili Bykov
 */
abstract class Room(private val _shortDescription: String, _description: String) : World.Configurable, ItemOwner {

    open val description =_description.lines().map{ it.trim() }.joinToString("\n")
    open val shortDescription
        get() = _shortDescription
    override val items = mutableListOf<Item>()
    private val exits = mutableMapOf<Direction, Room>()
    private val vocabulary = mutableMapOf<String, Action>()
    var visited = false

    /**
     * Print the description of the room and all the items in it.
     * This is what prints the blurb before every input prompt.
     */
    open fun printDescription() {
        say(if (visited) shortDescription else description)
        val visibleItems = items.filter { !it.isHidden }
        if (!visibleItems.isEmpty()) {
            println()
            visibleItems.forEach { say(it.description) }
        }
    }

    open fun printFullDescription() {
        say(description)
        val visibleItems = items.filter { !it.isHidden }
        if (!visibleItems.isEmpty()) {
            println()
            visibleItems.forEach { say(it.description) }
        }
    }

    open fun exitTo(direction: Direction): Room? = exits[direction]

    /**
     * Invoked when the player is about to be moved to another room from this
     * one. The method may return false to veto the move. In this case, it
     * typically should print a game message explaining why the move hasn't
     * happened.
     */
    open fun approvePlayerMoveTo(newRoom: Room) = true

    /**
     * Invoked when the player is about to be moved to this room from another.
     * The method may return false to veto the move. In this case, it typically
     * should print a game message explaining why the move hasn't happened.
     */
    open fun approvePlayerMoveFrom(oldRoom: Room) = true

    /**
     * Invoked after the player leaves this room for another room.
     */
    open fun noticePlayerMoveTo(newRoom: Room) = Unit

    /**
     * Invoked after the player enters this room. This method may be overridden,
     * but the overriding method must call this super implementation.
     */
    open fun noticePlayerMoveFrom(oldRoom: Room) {
        printDescription()
        visited = true
    }

    fun findAction(word: String): Action? = vocabulary[word]

    fun twoWay(target: Room, vararg directions: Direction) {
        for (direction in directions) {
            if (exits.containsKey(direction)) {
                throw IllegalArgumentException("exit to $direction already exists in $_shortDescription")
            }
            val opposite = direction.opposite()
            if (target.exits.containsKey(opposite)) {
                throw IllegalArgumentException("exit to $opposite already exists in $target._shortDescription")
            }
            addExit(direction, target)
            target.addExit(opposite, this)
        }
    }

    fun oneWay(target: Room, vararg directions: Direction) {
        for (direction in directions) {
            if (exits.containsKey(direction)) {
                throw IllegalArgumentException("exit to $direction already exists")
            }
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

    companion object {
        /**
         * The player is created in this "room" and moved into the starting
         * location when the game begins.
         */
        val NOWHERE = object : Room("You're nowhere.", "This is exactly what the middle of nowhere looks like.") {
            override fun configure() {
            }
        }
    }
}

