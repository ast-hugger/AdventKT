package com.github.vassilibykov.adventkt

/**
 * An [Item] that can't be picked up by the player. (And in general refuses to
 * be moved to another location).
 *
 * @author Vassili Bykov
 */
open class Fixture(vararg names: String, description: String)
    : Item(*names, owned = "should not be owned: $names[0]", dropped = description)
{
    internal constructor(vararg names: String)
            : this(*names, description = "no description() for $names[0]")

    override fun approveMoveTo(newOwner: ItemOwner): Boolean {
        say("The $primaryName is fixed in place.")
        return false
    }
}