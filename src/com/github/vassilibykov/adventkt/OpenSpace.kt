package com.github.vassilibykov.adventkt

/**
 * A room in which an exit into any not explicitly configured direction leads to
 * a default location.
 *
 * The [defaultExit] is specified as a function which provides the value so that
 * it can reference another property of the same class without initialization
 * ordering issues.
 *
 * @author Vassili Bykov
 */
abstract class OpenSpace(shortDescription: String, description: String, private val defaultExit: ()->Room)
    : Room(shortDescription, description)
{
    override fun exitTo(direction: Direction): Room? {
        return super.exitTo(direction) ?:
                (if (direction in Direction.compassDirections)
                    defaultExit()
                else
                    null)
    }
}