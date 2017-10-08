package com.github.vassilibykov.adventkt.framework

/**
 * An instance is stored in a room property named 'detail' and
 * handles DSL declarations like
 *
 *     detail named "tree" or "trees" description "Lots of trees."
 *
 * The `named` keyword must come first, optionally followed by one
 * or more `or`s, followed by `description`. `name` and `description`
 * are required. `description` may be followed by `cantTakeMessage`
 * and one or more `extraVerb`s.
 */
class RoomDetailBuilder(private val room: Room) {

    private val names = mutableListOf<String>()
    private var detail: Detail? = null

    infix fun named(name: String): RoomDetailBuilder {
        if (detail != null) throw IllegalStateException("detail names must be specified before all other properties")
        names.add(name)
        return this
    }

    infix fun or(name: String): RoomDetailBuilder {
        if (names.isEmpty()) throw IllegalStateException("no other names have been specified yet")
        return named(name)
    }

    infix fun description(message: String): RoomDetailBuilder {
        if (names.isEmpty()) throw IllegalStateException("detail name must be specified first")
        if (detail != null) throw IllegalStateException("only one description is allowed")
        detail = Detail(*names.toTypedArray())
        detail?.dropped = { message }
        detail?.primitiveMoveTo(room)
        return this
    }

    infix fun cantTakeMessage(message: String): RoomDetailBuilder {
        if (detail == null) throw IllegalStateException("detail description must be specified first")
        detail?.cantTakeMessage = message
        return this
    }

    infix fun extraVerb(verb: String): RoomDetailBuilder {
        if (detail == null) throw IllegalStateException("detail description must be specified first")
        detail?.extraVerbs?.add(verb)
        return this
    }
}