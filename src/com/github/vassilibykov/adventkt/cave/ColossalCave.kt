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
import com.github.vassilibykov.adventkt.framework.Direction.*

/**
 * A definition of a subset of Colossal Cave, with some changes and
 * enhancements.
 *
 * @author Vassili Bykov
 */
@Suppress("MemberVisibilityCanPrivate") // public for testing; no worries
class ColossalCave private constructor(): World() {

    companion object {
        fun create(): ColossalCave {
            val instance = ColossalCave()
            instance.runObjectSetup()
            return instance
        }
    }

    /*
        Rooms.
     */

    val outsideBuilding = outdoors("You're in front of building.",
            """You are standing at the end of a road before a small brick building.
            Around you is a forest. A small stream flows out of the building and
            down a gully.""")
    {
        twoWay(insideBuilding, IN, EAST)
        twoWay(hill, WEST)
        oneWay(hill, UP)
        oneWay(forest, NORTH)
        twoWay(valley, DOWN, SOUTH)

        here(adventKtSign)
        detail("fine", "print",
                extraVerbs = setOf("read"),
                message = "\"The Implementor's Prize isn't fully implemented yet.\"")
            .cantTakeMessage = "How do you imagine doing that?"

        action("downstream") { player.moveTo(valley) }

        onPlayerMoveIn { oldRoom ->
            if (player has nugget) {
                val entryWord = if (oldRoom == insideBuilding) "exit" else "approach"
                say("""As you $entryWord the well house, a large box appears hovering in the air.
                    |The box drops to the ground with a thud.""")
                implementorPrize.moveTo(this)
            }
        }
    }

    val adventKtSign = fixture("sign", message = "A new-looking sign is hanging outside the building.") {
        cantTakeMessage = "The sign is nailed securely to the wall."
        vicinityAction("read", "look") {
            say("""The sign says:
                    |
                    |    Welcome to AdventKT, a theme park based on the legendary Colossal Cave.
                    |    There is a priceless gold nugget hidden in the cave, protected by
                    |    an ancient curse. Bring it here to win the Implementor's Prize!
                    |
                    |There is some fine print at the bottom of the sign.""")
        }
    }

    val implementorPrize = fixture("box", "prize",
            message = "A large box with a tag \"Implementor's Prize\" is sitting on the ground.")
    {
        cantTakeMessage = "The Implementor's Prize is far too big for you to carry."

        var isOpen = false

        vicinityAction("open") {
            if (isOpen) {
                say("The Implementor's Prize is already open.")
            } else {
                isOpen = true
                say("""You open the box, releasing a huge cloud of yellow vapor.
                    |The cloud briefly morphs into words
                    |
                    |    TODO("implement this")
                    |
                    |before melting into the clear blue sky.
                    |
                    |Congratulations! You have won the game.""")
            }
        }
    }

    override val start = outsideBuilding

    val insideBuilding = litRoom("You're inside building.",
            """You are inside a building, a well house for a large spring.""")
    {
        here(keys)
        here(lantern)
        here(food)
        here(water)

        action("xyzzy") {
            say(">>Foof!<<")
            player.moveTo(debris)
        }

        action("down", "downstream") {
            say("""The stream flows out through a pair of 1 foot diameter sewer pipes.
                    |It would be advisable to use the exit.""")
        }
    }

    val hill = outdoors("You're at hill in road.",
            """You have walked up a hill, still in the forest. The road slopes back
            |down the other side of the hill. There is a building in the distance.""")
    {
        // EAST: twoWay from outsideBuilding
        twoWay(endOfRoad, WEST)
        oneWay(UnenterableRoom("Down to the east or down to the west?"), DOWN)
    }

    val endOfRoad: Room = outdoors("You're at end of road.",
            """The road, which approaches from the east, ends here amid the trees.""")
    {
        // EAST: twoWay from hill
        oneWay(hill, UP)
    }

    val cliff = outdoors("You're at cliff.",
            """The forest thins out here to reveal a steep cliff. There is no way
            |down, but a small ledge can be seen to the west across the chasm.""")
    {
    }

    val valley = outdoors("You're in valley.",
            """You are in a valley in the forest beside a stream tumbling along a
            |rocky bed.""")
    {
        // UP, EAST: twoWay from outsideBuilding
        twoWay(slit, DOWN, SOUTH)
        action("downstream") { player.moveTo(slit) }
    }

    val magicWords = setOf(
            "plover", "plugh", "zork", "foobar", "blorple", "frob", "foo", "quux",
            "wibble", "wobble", "wubble", "flob", "blep", "blah", "fnord", "piyo")

    val magicWord = random(*magicWords.toTypedArray())

    val forest: Room = outdoors("You are wandering aimlessly through the forest.",
            "You are wandering aimlessly through the forest.")
    {
        oneWay(outsideBuilding, OUT)
        oneWay(UnenterableRoom("The trees are too difficult to climb."), UP)

        action("get") { // support "get out"
            if ("out" in subjects) {
                player.moveTo(outsideBuilding)
            } else {
                pass()
            }
        }

        action("go") {
            when {
                "valley" in subjects -> player.moveTo(valley)
                "building" in subjects -> player.moveTo(outsideBuilding)
                "house" in subjects -> player.moveTo(outsideBuilding)
                "slit" in subjects -> player.moveTo(slit)
                "grate" in subjects -> player.moveTo(outsideGrate)
                else -> pass()
            }
        }

        allowItemMoveIn { oldOwner, _ ->
            // No dropping items; that would reveal that the forest is one room.
            // This does not preclude releasing the bird because the bird is
            // moved here from limbo.
            declineIf({ oldOwner == player },
                    """You realize you might never find your way back here
                    |and decide against dropping it.""")
        }

        onItemMoveIn(bird) {
            // The player has released the bird.
            say("""The bird is singing to you in gratitude for your having returned it to
                    |its home. In return, it informs you of a magic word which it thinks
                    |you may find useful somewhere near the Hall of Mists. The magic word
                    |changes frequently, but for now the bird believes it is """" + magicWord + """". You
                    |thank the bird for this information, and it flies off into the forest.""")
            bird.moveTo(Item.LIMBO)
        }

        var stepCount = 0
        onPlayerMoveIn { oldRoom ->
            if (oldRoom != this) {
                say("""You enter the forest and soon become lost among the trees.""")
                stepCount = 0
            } else {
                when(++stepCount) {
                    2 -> say("Are you sure you are not walking in circles?")
                    4 -> say("""You think you see your tracks on the forest floor.
                        |But then again, you are not much of a tracker.""")
                    6 -> say("The forest is not a maze. Maybe you need to try something else.")
                    8 -> say("You are feeling tired. All you wish for is to get out.")
                }
            }
        }

        onPlayerMoveOut { newRoom ->
            if (newRoom != this) {
                say("You finally found your way out of the forest.")
            }
        }
    }

    val slit = outdoors("You're at slit in streambed.",
                        """At your feet all the water of the stream splashes into a 2-inch slit
                        |in the rock. Downstream the streambed is bare rock.""")
    {
        // UP, NORTH: twoWay from slit
        twoWay(outsideGrate, SOUTH)
    }

    // strangely, need this explicit type to avoid a type checker recursive loop
    val outsideGrate: Room = outdoors("You're outside grate.",
            """You are in a 20-foot depression floored with bare dirt. Set into the
            |dirt is a strong steel grate mounted in concrete. A dry streambed
            |leads into the depression.""")
    {
        // NORTH: twoWay from slit
        twoWay(belowGrate, DOWN, IN)

        here(grate)

        allowPlayerMoveIn { oldRoom ->
            declineIf({ oldRoom == belowGrate && !grateOpen.isOn }, "The grate is closed")
        }

        allowPlayerMoveOut { newRoom ->
            declineIf({ newRoom == belowGrate && !grateOpen.isOn }, "The grate is closed")
        }
    }

    val belowGrate = litRoom(
            "You're below the grate.",
            """You are in a small chamber beneath a 3x3 steel grate to the surface.
            |A low crawl over cobbles leads inward to the west.""")
    {
        hereShared(grate) // the official owner is outsideGrate, but also seen here
        twoWay(cobble, WEST)
    }

    val cobble = litRoom(
            "You're in cobble crawl.",
            """You are crawling over cobbles in a low passage. There is a dim light
            |at the east end of the passage.""")
    {
        // EAST: twoWay from belowGrate
        twoWay(debris, WEST, UP)
        here(cage)
    }

    val debris: Room = darkRoom(
            "You're in debris room.",
            """You are in a debris room filled with stuff washed in from the surface.
            |A low wide passage with cobbles becomes plugged with mud and debris
            |here, but an awkward canyon leads upward and west. In the mud someone
            |has scrawled, "MAGIC WORD XYZZY".""")
    {
        twoWay(awkwardCanyon, UP, WEST)
        // EAST, UP: twoWay from cobble
        here(rod)
        action("xyzzy") {
            say(">>Foof!<<")
            player.moveTo(insideBuilding)
        }
    }

    val awkwardCanyon = darkRoom(
            "You are in an awkward sloping east/west canyon.",
            "You are in an awkward sloping east/west canyon.")
    {
        // DOWN, EAST: twoWay from debris
        twoWay(birdChamber, WEST)
    }

    val birdChamber = darkRoom(
            "You're in bird chamber.",
            """You are in a splendid chamber thirty feet high. The walls are frozen
            |rivers of orange stone. An awkward canyon and a good passage exit
            |from east and west sides of the chamber.""")
    {
        // EAST: twoWay from awkwardCanyon
        twoWay(pitTop, WEST)
        here(bird)
    }

    val pitTop = darkRoom("You're at top of small pit.",
            """At your feet is a small pit breathing traces of white mist. An east
            |passage ends here except for a small crack leading on.""")
    {
        // EAST: twoWay from birdChamber
        twoWay(mistHall, DOWN)
        oneWay(crack, WEST)

        here(stoneSteps)
    }

    val stoneSteps: Fixture = fixture("steps", message = {
            when (player.room) {
                pitTop -> "Rough stone steps lead down the pit."
                mistHall -> "Rough stone steps lead up the dome."
                else -> "- error: why are the rough stone steps here? -"
            }
        })
    {}

    val crack = UnenterableRoom(
            """The crack is far too small for you to follow. At its widest it is
            |barely wide enough to admit your foot.""")

    val randomDwarf = fixture("dwarf")
    {
        isHidden = true
        onTurnEnd {
            withProbability(0.3) {
                if (axe.owner == Item.LIMBO) {
                    blankLine()
                    say("""A little dwarf just walked around a corner, saw you, threw a little
                        |axe at you which missed, cursed, and ran away.""")
                    axe.moveTo(player.room)
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
        // UP: twoWay from pitTop
        twoWay(eastBank, WEST)
        twoWay(kingHall, DOWN)
        oneWay(kingHall, NORTH) // the return passage from kingHall is EAST
        twoWay(nuggetRoom, SOUTH)

        hereShared(stoneSteps)
        hereShared(randomDwarf)

        action(magicWord) {
            say("""The cave walls around you become a blur.
                    |>>Foof!<<""")
            player.moveTo(outsideGrate)
        }
        magicWords.forEach {
            action(it) {
                say("A hollow voice says, \"Fool!\"")
            }
        }

        allowPlayerMoveOut { newRoom ->
            declineIf({ newRoom == pitTop && player has nugget },
                    "An invisible force stops you from climbing the dome.")
        }
    }

    val eastBank = darkRoom("You're on east bank of fissure.",
            """You are on the east bank of a fissure slicing clear across the hall.
            |The mist is quite thick here, and the fissure is too wide to jump.""")
    {
        // EAST: twoWay from mistHall
        hereShared(randomDwarf)
    }

    val nuggetRoom = darkRoom("You're in nugget-of-gold room.",
            """This is a low room with a crude note on the wall. The note says,
            |"You won't get it up the steps".""")
    {
        // NORTH: twoWay from mistHall
        here(nugget)
        hereShared(randomDwarf)
    }

    val nugget = item("nugget", "gold",
            owned = "Large gold nugget",
            dropped = "There is a large sparkling nugget of gold here!")
    {}

    val kingHall = darkRoom("You're in Hall of Mt King.",
            """You are in the Hall of the Mountain King, with passages off in all
            |directions.""")
    {
        // UP: twoWay from mistHall
        oneWay(mistHall, EAST) // the return passage from mistHall is NORTH
        oneWay(unimplementedPassage, NORTH)
        oneWay(unimplementedPassage, SOUTH)
        oneWay(unimplementedPassage, WEST)
        oneWay(unimplementedPassage, SOUTHWEST)

        here(snake)
        hereShared(randomDwarf)

        allowPlayerMoveOut { newRoom ->
            declineIf({ this has snake && newRoom != mistHall }, "You can't get by the snake.")
        }

        onItemMoveIn(bird) {
            if (this has snake) {
                say("""The little bird attacks the green snake, and in an astounding flurry
                    |drives the snake away.""")
                snake.moveTo(Item.LIMBO)
            }
        }
    }

    val unimplementedPassage = UnenterableRoom(
            """The passage is blocked by orange safety fencing with a sign saying,
            |"No entry. This part of the cave is under construction."""")

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

    val grateOpen = Toggle(false,
            turnedOnMessage = "You unlock and open the grate.",
            turnedOffMessage = "You close and lock the grate.",
            alreadyOnMessage = "The grate is already open.",
            alreadyOffMessage = "The grate is already closed.")

    val grate = fixture("grate", "door",
            message = { if (grateOpen.isOn) "The grate is open." else "The grate is closed." })
    {
        vicinityAction("open", "unlock") { grateOpen.turnOn() }
                // Guards are LIFO, so the !isOpen.isOn check is performed first.
                .guardedBy({ player has keys },
                        "The grate is locked and you don't have the key.")
                .guardedBy({ !grateOpen.isOn },
                        "The grate is already open.")

        vicinityAction("close") { grateOpen.turnOff() }
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
                say("""A hollow voice says, "What did you expect, a crystal bridge?"""")
            } else {
                say("You look ridiculous waving the black rod.")
            }
        }
    }

    val bird: Item = item("bird",
            owned = {""},
            dropped = {
                if (Item.LIMBO has snake)
                    """A little bird is sitting here looking sad and lonely.
                |It probably misses its home in the forest."""
                else
                    "A cheerful little bird is sitting here singing."
            })
    {
        vicinityAction("take", "get", "catch") {
            if (referringTo(bird())) {
                when {
                    player has rod ->
                        say("""As you approach, the bird becomes disturbed and you cannot catch it.""")
                    player has cage -> {
                        cage.moveTo(Item.LIMBO)
                        bird().moveTo(Item.LIMBO)
                        cagedBird.moveTo(player)
                        // no message is printed by the above because cagedBird was in limbo, not the room
                        say("You catch the bird and put it in the cage.")
                    }
                    else -> say("You can catch the bird, but you cannot carry it.")
                }
            } else {
                pass()
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
            cagedBird() moveTo Item.LIMBO
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
                    ?: if (direction in Direction.compassDirections) forest else null
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
