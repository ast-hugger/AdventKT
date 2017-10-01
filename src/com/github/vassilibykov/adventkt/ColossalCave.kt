package com.github.vassilibykov.adventkt

import com.github.vassilibykov.adventkt.Direction.*

class ColossalCave: World() {

    /*
        Rooms.

        See the Room class documentation for why we use initialize()
        methods instead of regular init{} blocks.
     */

    val outsideBuilding = object : Room(
            """You are standing at the end of a road before a small brick building.
            Around you is a forest.  A small stream flows out of the building and
            down a gully.""",
            "You're in front of building.") {
        override fun initialize() {
            twoWay(insideBuilding, IN, EAST)
            twoWay(hill, WEST)
            oneWay(hill, UP)
            oneWay(forest, NORTH)
            twoWay(valley, DOWN, SOUTH)
            verb("downstream") { player.moveTo(valley) }
        }
    }

    val insideBuilding = object : Room(
            """You are inside a building, a well house for a large spring.""",
            "You're inside building.")
    {
        override fun initialize() {
            item(keys)
            item(lantern)
            item(food)
            item(water)
            verb("xyzzy") {
                say(">>Foof!<<")
                player.moveTo(debris)
            }
            verb("down", "downstream") {
                say("""The stream flows out through a pair of 1 foot diameter sewer pipes.
                    It would be advisable to use the exit.""")
            }
        }
    }

    val hill = object : Room(
            """You have walked up a hill, still in the forest.  The road slopes back
            down the other side of the hill.  There is a building in the distance.""",
            "You're at hill in road.")
    {
        override fun initialize() {
            twoWay(endOfRoad, WEST)
            oneWay(forest, NORTH)
            oneWay(forest, SOUTH)
            verb("down") { say("Which way?")}
        }
    }

    val endOfRoad = object : Room(
            """The road, which approaches from the east, ends here amid the trees.""",
            "You're at end of road.")
    {
        override fun initialize() {
            oneWay(forest, NORTH)
            oneWay(forest, SOUTH)
        }
    }

    val cliff = object : Room(
            """The forest thins out here to reveal a steep cliff.  There is no way
            down, but a small ledge can be seen to the west across the chasm.""",
            "You're at cliff.")
    {
        override fun initialize() {
        }
    }

    val valley = object : Room(
            """You are in a valley in the forest beside a stream tumbling along a
            rocky bed.""",
            "You're in valley.")
    {
        override fun initialize() {
            twoWay(slit, DOWN, SOUTH)
            verb("downstream") { player.moveTo(slit) }
        }
    }

    val forest: Room = object : Room(
            "You are wandering aimlessly through the forest.",
            "You are wandering aimlessly through the forest.")
    {
        override fun initialize() {
            twoWay(this, NORTH)
            twoWay(this, NORTHWEST)
            twoWay(this, WEST)
            twoWay(this, SOUTHWEST)
            oneWay(outsideBuilding, OUT)
        }
    }

    val slit = object : Room(
            """At your feet all the water of the stream splashes into a 2-inch slit
            in the rock.  Downstream the streambed is bare rock.""",
            "You're at slit in streambed.")
    {
        override fun initialize() {
            twoWay(outsideGrate, SOUTH)
        }
    }

    // strangely, need this explicit type to avoid a type checker recursive loop
    val outsideGrate: Room = object : Room(
            """You are in a 20-foot depression floored with bare dirt.  Set into the
            dirt is a strong steel grate mounted in concrete.  A dry streambed
            leads into the depression.""",
            "You're outside grate.")
    {
        override fun initialize() {
            item(grate)
            twoWay(belowGrate, DOWN, IN)
        }

        override fun approvePlayerMoveTo(newRoom: Room): Boolean {
            if (newRoom == belowGrate && !grate.isOpen.isOn) {
                say("The grate is closed.")
                return false
            }
            return true
        }
    }

    val belowGrate: Room = object : Room(
            """You are in a small chamber beneath a 3x3 steel grate to the surface.
            A low crawl over cobbles leads inward to the west.""",
            "You're below the grate.")
    {
        override fun initialize() {
            unownedItem(grate) // the official owner is outsideGrate, but also visible here
            twoWay(cobble, WEST)
        }

        override fun approvePlayerMoveTo(newRoom: Room): Boolean {
            if (newRoom == outsideGrate && !grate.isOpen.isOn) {
                say("The grate is closed.")
                return false
            }
            return true
        }
    }


    val cobble = object : Room(
            """You are crawling over cobbles in a low passage.  There is a dim light
            at the east end of the passage.""",
            "You're in cobble crawl.")
    {
        override fun initialize() {
            item(cage)
            twoWay(debris, WEST, UP)
        }
    }

    val debris: DarkRoom = object : DarkRoom(
            """You are in a debris room filled with stuff washed in from the surface.
            A low wide passage with cobbles becomes plugged with mud and debris
            here, but an awkward canyon leads upward and west.  In the mud someone
            has scrawled, "MAGIC WORD XYZZY".""",
            "You're in debris room.")
    {
        override fun initialize() {
            item(rod)
            twoWay(awkwardCanyon, WEST)
            verb("xyzzy") {
                say(">>Foof!<<")
                player.moveTo(insideBuilding)
            }
        }
    }

    val awkwardCanyon = object : DarkRoom(
            "You are in an awkward sloping east/west canyon.",
            "You are in an awkward sloping east/west canyon.")
    {
        override fun initialize() {
            twoWay(birdChamber, WEST)
        }
    }

    val birdChamber = object : DarkRoom(
            """You are in a splendid chamber thirty feet high.  The walls are frozen
            rivers of orange stone.  An awkward canyon and a good passage exit
            from east and west sides of the chamber.""",
            "You're in bird chamber.")
    {
        override fun initialize() {
            item(bird)
            twoWay(pitTop, WEST)
        }
    }

    val pitTop = object : DarkRoom(
            """At your feet is a small pit breathing traces of white mist.  An east
            passage ends here except for a small crack leading on.""",
            "You're at top of small pit.")
    {
        override fun initialize() {
        }
    }

    /*
        The player and the lantern; required properties.
     */

    override val player : Player = Player(outsideBuilding)

    override val lantern = Lantern()

    /*
        Some simple items with no special behavior.
     */

    val keys = Item("keys", "key",
            owned = "Set of keys",
            dropped = "There are some keys on the ground here.")

    val food = Item("food",
            owned = "Tasty food",
            dropped = "There is food here.")

    val water = Item("water",
            owned = "Water in the bottle",
            dropped = "There is a bottle of water here.")

    /*
        The grate is an example of two points: vicinity verbs and guards.

        A regular verb defined for an item is only active when the item is
        held by the player. A vicinity verb is active when the player is in
        the room with the item.

        A guard is attached to a verb to only apply it if a guard condition
        is met, and print a message otherwise. The grate can only be
        open if it's unlocked, and it can only be unlocked if the player has
        the keys.

        This is a real rather than an anonymous class so that the [grate]
        has the type of this class rather than generic [Item], and we can
        access its properties.
     */

    inner class Grate : Item("grate", "door") {
        val isOpen = Toggle(false,
                turnOnMessage = "You unlock and open the grate.",
                turnOffMessage = "You close and lock the grate.",
                alreadyOnMessage = "The grate is already open.",
                alreadyOffMessage = "The grate is already closed.")

        override fun description() = if (isOpen.isOn) "The grate is open." else "The grate is closed."

        override fun canBeTaken() = false // the player can't pick this up

        init {
            vicinityVerb("open", "unlock") { isOpen.turnOn() }
                    .guardedBy({ player has keys },
                            "The grate is locked and you don't have the key.")

            vicinityVerb("close") { isOpen.turnOff() }
        }
    }
    val grate = Grate()

    val cage = Item("cage",
            owned = "Wicker cage",
            dropped = "There is a small wicker cage discarded nearby.")

    val rod = Item("rod",
            owned = "Black rod",
            dropped = "A three foot black rod with a rusty star on an end lies nearby.")

    val bird = object : Item("bird",
            owned = "- bird (in error) ",
            dropped = "A cheerful little bird is sitting here singing.")
    {
        init {
            vicinityVerb("take", "get", "catch") {
                if (referringTo(this.item)) {
                    when {
                        player has rod ->
                            say("""The bird was unafraid when you entered, but as you approach it becomes
                                disturbed and you cannot catch it.""")
                        player has cage -> {
                            cage.uncheckedMoveTo(Item.LIMBO)
                            this.item.uncheckedMoveTo(Item.LIMBO)
                            cagedBird.uncheckedMoveTo(player)
                            say("OK")
                        }
                        else -> say("You can catch the bird, but you cannot carry it.")
                    }
                } else {
                    pass()
                }
            }
        }

        override fun approveMove(newOwner: ItemOwner): Boolean {
            if (newOwner == player &&  player has rod) {
                say("""The bird was unafraid when you entered, but as you approach it becomes
                    disturbed and you cannot catch it.""")
                return false
            }
            return true
        }
    }

    val cagedBird = Item("bird",
            owned = "Little bird in cage",
            dropped = "There is a little bird in the cage.")

}
