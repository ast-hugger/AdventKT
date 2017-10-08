/*
 * Copyright (c) 2017 Vassili Bykov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vassilibykov.adventkt.cave

import com.github.vassilibykov.adventkt.framework.*

/**
 * A definition of a portion of Colossal Cave, with some changes and
 * enhancements.
 *
 * It is an example of the world definition DSL. See the commentary in line
 * comments throughout the class.
 *
 * @author Vassili Bykov
 */
@Suppress("MemberVisibilityCanPrivate") // public for testing; no worries
class ColossalCave private constructor(): World() {

    companion object {
        fun create(): ColossalCave {
            val instance = ColossalCave()
            // A World subclass instance must be initialized by its creator calling runObjectSetup().
            instance.runObjectSetup()
            return instance
        }
    }

    /*
        Rooms.
     */

    // An 'outdoor()' declaration is defined at the end of this class.
    // It is a room in which any not explicitly set exit leads to the 'forest' location.
    // Any room declaration starts with two arguments: the short summary displayed
    // when revisiting the room, and a long description printed on first visit.
    //
    // A room body typically includes a number of exit declared using the 'to'
    // syntax and a list of items which start off in the room declared using
    // the '-' syntax similar to an itemized list in Markdown.
    //
    // This room has a 'detail', a special hidden immobile item which the player
    // can interact with using the standard verbs "look", "l", and
    // "examine", and possibly additional verbs--in this case, "read".
    // "fine" and "print" are both names of this item. All together, this declaration
    // allows the player to say "read fine print" or "look at fine print" (as well as
    // less fortunate verb+name combination such as "look fine"), and get back the item's
    // 'message'.
    // The definition is parenthesized so it can span multiple lines.

    val outsideBuilding = outdoors("You're in front of building.",
            """You are standing at the end of a road before a small brick building.
            Around you is a forest. A small stream flows out of the building and
            down a gully.""")
    {
        east and `in` to insideBuilding
        west and up to hill
        down and south to valley
        // exit to a non-standard direction is introduces with 'exit named ...'
        exit named "downstream" to valley
        exit named "grate" to outsideGrate

        - adventKtSign

        (detail named "fine" or "print"
                description "\"The Implementor's Prize isn't fully implemented yet.\""
                extraVerb "read"
                cantTakeMessage "How do you imagine doing that?")

        // A block executed every time the player enters the room, with the room the player
        // enters from as the argument.
        onPlayerEntry { oldRoom ->
            if (player has nugget) {
                val entryWord = if (oldRoom == insideBuilding) "exit" else "approach"
                // '+ "message"' or 'say("message")' both have the effect of printing
                // the message as game output, followed by newline.
                + """As you $entryWord the well house, a large box appears hovering in the air.
                  |The box drops to the ground with a thud.
                  |"""
                implementorPrize moveTo this
            }
        }
    }

    // A 'fixture' is an immobile item. An attempt by the player to pick it up will display
    // a generic message saying the item is "fixed in place", or a custom 'cantTakeMessage'
    // as specified below.
    val adventKtSign = fixture("sign", message = "A new-looking sign is hanging outside the building.") {
        cantTakeMessage = "The sign is nailed securely to the wall."

        // A vicinityAction is an action selected by the specified commands when the item is
        // in the same room as the player (but not owned by the player).
        vicinityAction("read", "look") {
            + """The sign says:
              |
              |    Welcome to AdventKT, a theme park based on the legendary Colossal Cave.
              |    There is a priceless gold nugget hidden in the cave, protected by
              |    an ancient curse. Bring it here to win the Implementor's Prize!
              |
              |There is some fine print at the bottom of the sign."""
        }
    }

    val implementorPrize = fixture("box", "prize",
            message = "A large box with a tag \"Implementor's Prize\" is sitting on the ground.")
    {
        cantTakeMessage = "The Implementor's Prize is far too big for you to carry."

        var isOpen = false

        vicinityAction("open") {
            if (isOpen) {
                + "The Implementor's Prize is already open."
            } else {
                isOpen = true
                + """You open the box, releasing a huge cloud of yellow vapor.
                  |The cloud briefly morphs into words
                  |
                  |    TODO("implement this")
                  |
                  |before melting into the clear blue sky.
                  |
                  |Congratulations! You have won the game."""
            }
        }
    }

    // The abstract property 'start' of the World class must be defined to point
    // at the room where the player starts the game.
    override val start = outsideBuilding

    // A regular 'room'. Unlike 'outdoors', the only available exits are the ones
    // explicitly specified.
    val insideBuilding: Room = room("You're inside building.",
            """You are inside a building, a well house for a large spring.""")
    {
        out and west to outsideBuilding

        - keys
        - lantern
        - food
        - water

        action("xyzzy") {
            + ">>Foof!<<"
            player moveTo debris
        }

        action("down", "downstream") {
            + """The stream flows out through a pair of 1 foot diameter sewer pipes.
              |It would be advisable to use the exit."""
        }
    }

    val hill: Room = outdoors("You're at hill in road.",
            """You have walked up a hill, still in the forest. The road slopes back
            |down the other side of the hill. There is a building in the distance.""")
    {
        east to outsideBuilding
        west to endOfRoad
        down noEntry "Down to the east or down to the west?"
    }

    val endOfRoad: Room = outdoors("You're at end of road.",
            """The road, which approaches from the east, ends here amid the trees.""")
    {
        east and up to hill
    }

    val cliff = outdoors("You're at cliff.",
            """The forest thins out here to reveal a steep cliff. There is no way
            |down, but a small ledge can be seen to the west across the chasm.""")
    {
    }

    val valley: Room = outdoors("You're in valley.",
            """You are in a valley in the forest beside a stream tumbling along a
            |rocky bed.""")
    {
        north and up to outsideBuilding
        south and down to slit
        exit named "downstream" to slit
    }

    // Magic words for teleporting out of mistHall.
    val magicWords = setOf(
            "plover", "plugh", "zork", "foobar", "blorple", "frob", "foo", "quux",
            "wibble", "wobble", "wubble", "flob", "blep", "blah", "fnord", "piyo")

    // The current magic word revealed by the bird.
    val magicWord = random(*magicWords.toTypedArray())

    val forest: Room = outdoors("You are wandering aimlessly through the forest.",
            "You are wandering aimlessly through the forest.")
    {
        out to outsideBuilding
        up noEntry "The trees are too difficult to climb."
        exit named "valley" to valley
        exit named "building" to outsideBuilding
        exit named "house" to outsideBuilding
        exit named "slit" to slit
        exit named "grate" to outsideGrate
        action("get") { // recognize "get out" as a way to get out of the forest
            if ("out" in subjects) {
                player moveTo outsideBuilding
            } else {
                pass()
            }
        }

        detail named "tree" or "trees" description "You can't see the forest for all of them."

        // The associated block is evaluated when an item is about to be moved
        // into this room. Returning false will veto the move.
        allowItemMoveIn { oldOwner, _ ->
            // If oldOwner is player, the player is trying to drop the item.
            // We want to prohibit that because it would reveal that the forest
            // is only one room. We could use a regular 'if/else', but it reads
            // smoother with the supplied 'declineIf': if the predicate matches,
            // the message is printed and false is returned.
            declineIf({ oldOwner == player },
                    """You realize you might never find your way back here
                    |and decide against dropping it.""")
        }

        // The associated block is evaluated after the matching item has been moved
        // into this room. A bird is moved into this room when it's released by the
        // player. That can happen despite the above prohibition on dropping things
        // because a bird owned by the player is represented by 'cagedBird', dropping
        // which moves 'cagedBird' to limbo and 'bird' from limbo to here.
        onItemMoveIn(bird) {
            // Print the message revealing the magic word, them move back to limbo.
            + """The bird is singing to you in gratitude for your having returned it to
               |its home. In return, it informs you of a magic word which it thinks
               |you may find useful somewhere near the Hall of Mists. The magic word
               |changes frequently, but for now the bird believes it is "$magicWord". You
               |thank the bird for this information, and it flies off into the forest."""
            bird moveTo LIMBO
        }

        var stepCount = 0 // keep track of how long the player has been wandering

        onPlayerEntry { oldRoom ->
            if (oldRoom != this) {
                + """You enter the forest and soon become lost among the trees."""
                stepCount = 0
            } else {
                when(++stepCount) {
                    2 -> + "Are you sure you are not walking in circles?"
                    4 -> + """You think you see your tracks on the forest floor
                           |But then again, you've never been much of a tracker."""
                    6 -> + "The forest is not a maze. Maybe you need to try something else."
                    8 -> + "You are feeling tired. All you wish for is to get out."
                }
            }
        }

        onPlayerExit { newRoom ->
            if (newRoom != this) {
                + "You finally found your way out of the forest."
            }
        }
    }

    val slit = outdoors("You're at slit in streambed.",
                        """At your feet all the water of the stream splashes into a 2-inch slit
                        |in the rock. Downstream the streambed is bare rock.""")
    {
        north and up to valley
        south to outsideGrate
        down noEntry "You don't fit through a two-inch slit!"
    }

    // strangely, need this explicit type to avoid a type checker recursive loop
    val outsideGrate: Room = outdoors("You're outside grate.",
            """You are in a 20-foot depression floored with bare dirt. Set into the
            |dirt is a strong steel grate mounted in concrete. A dry streambed
            |leads into the depression.""")
    {
        north to slit
        down and `in` to belowGrate unless { !grateOpen.isOn } thenSay "The grate is closed."
        exit named "building" to outsideBuilding

        - grate
    }

    val belowGrate = room(
            "You're below the grate.",
            """You are in a small chamber beneath a 3x3 steel grate to the surface.
            |A low crawl over cobbles leads inward to the west.""")
    {
        west to cobble
        up and out to outsideGrate unless { !grateOpen.isOn } thenSay "The grate is closed."
        exit named "pit" to pitTop

        - grate
    }

    val cobble: Room = room(
            "You're in cobble crawl.",
            """You are crawling over cobbles in a low passage. There is a dim light
            |at the east end of the passage.""")
    {
        east to belowGrate
        west and up to debris

        - cage
    }

    val debris: Room = darkRoom(
            "You're in debris room.",
            """You are in a debris room filled with stuff washed in from the surface.
            |A low wide passage with cobbles becomes plugged with mud and debris
            |here, but an awkward canyon leads upward and west. In the mud someone
            |has scrawled, "MAGIC WORD XYZZY".""")
    {
        east and down to cobble
        west and up to awkwardCanyon

        - rod

        action("xyzzy") {
            + ">>Foof!<<"
            player moveTo insideBuilding
        }
    }

    val awkwardCanyon = darkRoom(
            "You are in an awkward sloping east/west canyon.",
            "You are in an awkward sloping east/west canyon.")
    {
        east and down to debris
        west to birdChamber
    }

    val birdChamber: Room = darkRoom(
            "You're in bird chamber.",
            """You are in a splendid chamber thirty feet high. The walls are frozen
            |rivers of orange stone. An awkward canyon and a good passage exit
            |from east and west sides of the chamber.""")
    {
        east to awkwardCanyon
        west to pitTop

        - bird
    }

    val pitTop: Room = darkRoom("You're at top of small pit.",
            """At your feet is a small pit breathing traces of white mist. An east
            |passage ends here except for a small crack leading on.""")
    {
        east to birdChamber
        down to mistHall
        west noEntry """The crack is far too small for you to follow. At its widest it is
                     |barely wide enough to admit your foot."""
        exit named "entrance" to belowGrate

        - stoneSteps
    }

    val stoneSteps: Fixture = fixture("steps", message = {
            when (player.room) {
                pitTop -> "Rough stone steps lead down the pit."
                mistHall -> "Rough stone steps lead up the dome."
                else -> "- error: why are the rough stone steps here? -"
            }
        })
    {}

    // Here is all there is to the dwarf that occasionally shows to throw an axe at you.
    // It is a fixture with an 'onTurnEnd' action which gets executed after processing
    // any player input if the player is in the same room.
    val dwarf = fixture("dwarf") // needs no description because it's hidden
    {
        isHidden = true
        onTurnEnd {
            withProbability(0.3) {
                // If the axe is in limbo, it's not carried by the player or lying
                // somewhere, so we can have the dwarf throw it.
                if (axe isIn LIMBO) {
                    + ""
                    + """A little dwarf just walked around a corner, saw you, threw a little
                        |axe at you which missed, cursed, and ran away."""
                    axe moveTo player.room
                }
            }
        }
    }

    val axe = item("axe", owned = "Dwarf's axe", dropped = "There is a little axe here.") {}

    val mistHall: Room = darkRoom("You're in Hall of Mists.",
            """You are at one end of a vast hall stretching forward out of sight to
            |the west. There are openings to either side. Nearby, a wide stone
            |staircase leads downward. The hall is filled with wisps of white mist
            |swaying to and fro almost as if alive. A cold wind blows up the
            |staircase. There is a passage at the top of a dome behind you.""")
    {
        (exit named "dome" and up to pitTop unless { player has nugget }
                thenSay "An invisible force stops you from climbing the dome.")
        west to eastBank
        down and north to kingHall
        south to nuggetRoom

        - stoneSteps
        - dwarf

        // Have the magic word du jour work as advertised.
        action(magicWord) {
            + """The cave walls around you become a blur.
              |>>Foof!<<"""
            player moveTo outsideGrate
        }
        // Any other magic word is recognized but frowned upon.
        // Note that this also defines a discouraging action for the currently valid magic word.
        // However, its real teleport action defined earlier will take precedence.
        magicWords.forEach {
            action(it) {
                + "A hollow voice says, \"Fool!\""
            }
        }
    }

    val eastBank = darkRoom("You're on east bank of fissure.",
            """You are on the east bank of a fissure slicing clear across the hall.
            |The mist is quite thick here, and the fissure is too wide to jump.""")
    {
        east to mistHall

        - dwarf
    }

    val nuggetRoom = darkRoom("You're in nugget-of-gold room.",
            """This is a low room with a crude note on the wall. The note says,
            |"You won't get it up the steps".""")
    {
        north and out to mistHall

        - nugget
        - dwarf
    }

    val nugget = item("nugget", "gold",
            owned = "Large gold nugget",
            dropped = "There is a large sparkling nugget of gold here!")
    {}

    val kingHall = darkRoom("You're in Hall of Mt King.",
            """You are in the Hall of the Mountain King, with passages off in all
            |directions.""")
    {
        up and east to mistHall
        (north and south and west and southwest
                noEntry
                    """The passage is blocked by orange safety fencing with a sign saying,
                    |"No entry. This part of the cave is under construction.""""
                unless { this has snake }
                    thenSay "You can't get by the snake.")

        - snake
        - dwarf

        onItemMoveIn(bird) {
            if (this has snake) {
                 + """The little bird attacks the green snake, and in an astounding flurry
                   |drives the snake away."""
                snake moveTo LIMBO
            }
        }
    }

    val snake = fixture("snake", message = "A huge green fierce snake bars the way!") {
        cantTakeMessage = "This doesn't sound like a very good idea."
    }

    /*
        Items
     */

    val lantern = Lantern()

    val keys = item("keys", "key",
            owned = "Set of keys",
            dropped = "There are some keys on the ground here.")
    {
        isPlural = true
    }

    val food = item("food",
            owned = "Tasty food",
            dropped = "There is food here.")
    {}

    val water = item("water",
            owned = "Water in the bottle",
            dropped = "There is a bottle of water here.")
    {}

    // A Toggle is an object representing a Boolean state ('isOn'), with state transition
    // messages displayed to the user when the state is changed using 'turnOn' and 'turnOff'.
    val grateOpen = Toggle(false,
            turnedOnMessage = "You unlock and open the grate.",
            turnedOffMessage = "You close and lock the grate.",
            alreadyOnMessage = "The grate is already open.",
            alreadyOffMessage = "The grate is already closed.")

    val grate = fixture("grate", "door",
            message = { if (grateOpen.isOn) "The grate is open." else "The grate is closed." })
    {
        // An action with a .guardedBy() clause will only execute its action block if the
        // predicate in the clause returns true. Otherwise, the refusal message in the clause
        // is printed.
        // .guardedBy() clauses can be chained as below. In that case, all of them are applied,
        // and all must pass for the action to happen. The LAST clause is applied FIRST.
        vicinityAction("open", "unlock") { grateOpen.turnOn() }
                .guardedBy({ player has keys },
                        "The grate is locked and you don't have the key.")
                // The following clause is required despite having a similar message in the Toggle.
                // Otherwise, at attempt by a player without a key to open and already open grate
                // would say that the grate is locked.
                .guardedBy({ !grateOpen.isOn },
                        "The grate is already open.")

        vicinityAction("close") { grateOpen.turnOff() }
                .guardedBy({ player has keys },
                        "You don't have the key to lock it.")
    }

    val cage = item("cage",
            owned = "Wicker cage",
            dropped = "There is a small wicker cage discarded nearby.")
    {}

    val rod = item("rod",
            owned = "Black rod",
            dropped = "A three foot black rod with a rusty star on an end lies nearby.")
    {
        action("wave") {
            if (player.room == eastBank) {
                + "A hollow voice says, \"What did you expect, a crystal bridge?\""
            } else {
                + "You look silly waving the black rod."
            }
        }
    }

    // The apparent bird and cage are actually three items: bird, cagedBird, and cage.
    // The bird in a room is always 'bird', and the bird carried by the player is always 'cagedBird'.
    // The cage is involved in transitions between these.

    val bird: Item = item("bird",
            owned = {""},
            // Here is an example of a dynamic object description, specified as a block instead of a string.
            // We are giving a hint to release the bird in the forest after the snake has been driven away.
            dropped = {
                if (LIMBO has snake)
                    """A little bird is sitting here looking sad and lonely.
                    |It probably misses its home in the forest."""
                else
                    "A cheerful little bird is sitting here singing."
            })
    {
        vicinityAction("take", "get", "catch") {
            when {
                player has rod ->
                    + "As you approach, the bird becomes disturbed and you cannot catch it."
                player has cage -> {
                    cage moveTo LIMBO
                    bird() moveTo LIMBO
                    cagedBird moveTo player
                    + "You catch the bird and put it in the cage."
                }
                else -> say("You can catch the bird, but you cannot carry it.")
            }
        }

        allowMoveTo(player) {
            declineIf({ player has rod }, "As you approach, the bird becomes disturbed and you cannot catch it.")
        }
    }
    private fun bird() = bird // can't reference 'bird' from itself directly, so need this

    val cagedBird = item("bird", "cage",
            owned = "Little bird in cage",
            dropped = "There is a little bird in the cage.")
    {
        // Allows 'open cage' and 'release bird'.
        // Also allows 'open bird' and 'release cage' but oh well.
        action("open", "release") {
            cagedBird() moveTo LIMBO
            cage moveTo player
            bird moveTo player.room
        }
    }
    private fun cagedBird(): Item = cagedBird // can't reference cagedBird in itself directly. ugh

    /**
     * The standard room for the outdoors. Any not explicitly connected
     * compass direction leads to the forest.
     */
    private inner class Outdoors(summary: String, description: String) : Room(summary, description) {
        override fun exitTo(direction: Direction): Room? {
            return super.exitTo(direction)
                    ?: if (direction in StandardDirection.compassDirections) forest else null
        }
    }

    /**
     * Declare a room for the outdoors part of Colossal Cave. Any compass
     * direction which is not explicitly configured leads to the forest.
     */
    private fun outdoors(summary: String, description: String, configurator: Room.()->Unit): Room {
        val room = Outdoors(summary, description)
        room.configurator = configurator
        return room
    }
}
