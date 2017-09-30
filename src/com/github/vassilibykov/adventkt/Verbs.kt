package com.github.vassilibykov.adventkt

/*
    Global vocabulary verbs, alphabetic order
 */

fun globalVocabulary(): Map<String, Verb> {
    val vocabulary = mutableMapOf<String, Verb>()
    fun addVerb(verb: Verb) = verb.words.forEach { vocabulary.put(it, verb) }
    addVerb(Drop())
    addVerb(Go())
    addVerb(Inventory())
    addVerb(Look())
    addVerb(Quit())
    addVerb(Take())
    Direction.directionNames().forEach {vocabulary.put(it, DirectionVerb(it))}
    return vocabulary
}

class DirectionVerb(word: String) : MovementVerb(word) {
    override fun act(subjects: List<String>) {
        movePlayer(words[0])
    }
}

class Drop: Verb("drop") {
    override fun act(subjects: List<String>) {
        val item = subjects.map { player.findItem(it) }.firstOrNull()
        if (item == null) {
            say("You don't have that.")
            return
        }
        item.moveTo(player.room)
        say("OK")
    }
}

class Go: MovementVerb("go") {
    override fun act(subjects: List<String>) {
        if (subjects.isEmpty()) {
            println("Go where?")
            return
        }
        movePlayer(subjects[0])
    }
}

class Inventory: Verb("inventory", "i", "inv") {
    override fun act(subjects: List<String>) {
        val items = player.inventory
        if (items.isEmpty()) {
            say("You are carrying nothing.")
        } else {
            say("You are currently holding the following:")
            items.forEach { say(it.inventoryDescription()) }
        }
    }
}

class Look: Verb("look") {
    override fun act(subjects: List<String>) {
        if (subjects.isEmpty()) {
            player.room.printFullDescription()
            return
        }
    }
}

class Quit: Verb("quit", "bye") {
    override fun act(subjects: List<String>) {
        throw QuitException()
    }
}

class Take: Verb("take", "get", "pick") {
    override fun act(subjects: List<String>) {
        if ("all" in subjects) {
            takeAll()
            return
        }
        val item = subjects.map { player.room.findItem(it) }.firstOrNull()
        if (item == null) {
            say("There is no such thing here.")
            return
        }
        if (!item.canBeTaken()) {
            say("You can't pick $item.primaryName up.")
            return
        }
        item.moveTo(player)
        say("OK")
    }

    private fun takeAll() {
        // ownItem modifies the item list, so make sure to iterate over a copy
        player.room.items.toList().forEach {
            if (!it.isHidden()) {
                if (it.canBeTaken()) {
                    it.moveTo(player)
                    say("The " + it.primaryName + ": taken.")
                } else {
                    say("You can't pick that up.")
                }
            }
        }
    }
}

class Xyzzy : Verb("xyzzy") {
    override fun act(subjects: List<String>) {
        say("Nothing happens.")
    }

}
