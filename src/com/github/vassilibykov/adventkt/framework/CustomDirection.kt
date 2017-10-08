package com.github.vassilibykov.adventkt.framework

/**
 * A direction which isn't a standard one like 'north' or 'out'.
 */
class CustomDirection(override val name: String) : Direction {
    override val shortcut: String
        get() = name
}