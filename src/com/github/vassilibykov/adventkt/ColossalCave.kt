package com.github.vassilibykov.adventkt

import com.github.vassilibykov.adventkt.Direction.*

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

        See the Room class documentation for why we use setup()
        methods instead of regular init{} blocks.
     */

    val outsideBuilding = object : Room(
            "You're in front of building.",
            """You are standing at the end of a road before a small brick building.
            Around you is a forest.  A small stream flows out of the building and
            down a gully.""") {
        override fun setup() {
            twoWay(insideBuilding, IN, EAST)
            twoWay(hill, WEST)
            oneWay(hill, UP)
            oneWay(forest, NORTH)
            twoWay(valley, DOWN, SOUTH)
            action("downstream") { player.moveTo(valley) }
        }
    }

    val insideBuilding = object : Room(
            "You're inside building.",
            """You are inside a building, a well house for a large spring.""")
    {
        override fun setup() {
            item(keys)
            item(lantern)
            item(food)
            item(water)
            action("xyzzy") {
                say(">>Foof!<<")
                player.moveTo(debris)
            }
            action("down", "downstream") {
                say("""The stream flows out through a pair of 1 foot diameter sewer pipes.
                    It would be advisable to use the exit.""")
            }
        }
    }

    val hill = object : Room(
            "You're at hill in road.",
            """You have walked up a hill, still in the forest.  The road slopes back
            down the other side of the hill.  There is a building in the distance.""")
    {
        override fun setup() {
            twoWay(endOfRoad, WEST)
            oneWay(forest, NORTH)
            oneWay(forest, SOUTH)
            action("down") { say("Which way?")}
        }
    }

    val endOfRoad = object : Room(
            "You're at end of road.",
            """The road, which approaches from the east, ends here amid the trees.""")
    {
        override fun setup() {
            oneWay(forest, NORTH)
            oneWay(forest, SOUTH)
        }
    }

    val cliff = object : Room(
            "You're at cliff.",
            """The forest thins out here to reveal a steep cliff.  There is no way
            down, but a small ledge can be seen to the west across the chasm.""")
    {
        override fun setup() {
        }
    }

    val valley = object : Room(
            "You're in valley.",
            """You are in a valley in the forest beside a stream tumbling along a
            rocky bed.""")
    {
        override fun setup() {
            twoWay(slit, DOWN, SOUTH)
            action("downstream") { player.moveTo(slit) }
        }
    }

    val forest: Room = object : Room(
            "You are wandering aimlessly through the forest.",
            "You are wandering aimlessly through the forest.")
    {
        override fun setup() {
            twoWay(this, NORTH)
            twoWay(this, NORTHWEST)
            twoWay(this, WEST)
            twoWay(this, SOUTHWEST)
            oneWay(outsideBuilding, OUT)
        }
    }

    val slit = object : Room(
            "You're at slit in streambed.",
            """At your feet all the water of the stream splashes into a 2-inch slit
            in the rock.  Downstream the streambed is bare rock.""")
    {
        override fun setup() {
            twoWay(outsideGrate, SOUTH)
        }
    }

    // strangely, need this explicit type to avoid a type checker recursive loop
    val outsideGrate: Room = object : Room(
            "You're outside grate.",
            """You are in a 20-foot depression floored with bare dirt.  Set into the
            dirt is a strong steel grate mounted in concrete.  A dry streambed
            leads into the depression.""")
    {
        override fun setup() {
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

        override fun approvePlayerMoveFrom(oldRoom: Room): Boolean {
            if (oldRoom == belowGrate && !grate.isOpen.isOn) {
                say("The grate is closed.")
                return false
            }
            return true
        }
    }

    val belowGrate: Room = object : Room(
            "You're below the grate.",
            """You are in a small chamber beneath a 3x3 steel grate to the surface.
            A low crawl over cobbles leads inward to the west.""")
    {
        override fun setup() {
            unownedItem(grate) // the official owner is outsideGrate, but also visible here
            twoWay(cobble, WEST)
        }
    }


    val cobble = object : Room(
            "You're in cobble crawl.",
            """You are crawling over cobbles in a low passage.  There is a dim light
            at the east end of the passage.""")
    {
        override fun setup() {
            item(cage)
            twoWay(debris, WEST, UP)
            // EAST: twoWay from belowGrate
        }
    }

    val debris: DarkRoom = object : DarkRoom(
            """You are in a debris room filled with stuff washed in from the surface.
            A low wide passage with cobbles becomes plugged with mud and debris
            here, but an awkward canyon leads upward and west.  In the mud someone
            has scrawled, "MAGIC WORD XYZZY".""",
            "You're in debris room.")
    {
        override fun setup() {
            item(rod)
            twoWay(awkwardCanyon, WEST)
            // EAST, UP: twoWay from cobble
            action("xyzzy") {
                say(">>Foof!<<")
                player.moveTo(insideBuilding)
            }
        }
    }

    val awkwardCanyon = object : DarkRoom(
            "You are in an awkward sloping east/west canyon.",
            "You are in an awkward sloping east/west canyon.")
    {
        override fun setup() {
            twoWay(birdChamber, WEST)
            // EAST: twoWay from debris
        }
    }

    val birdChamber = object : DarkRoom(
            """You are in a splendid chamber thirty feet high.  The walls are frozen
            rivers of orange stone.  An awkward canyon and a good passage exit
            from east and west sides of the chamber.""",
            "You're in bird chamber.")
    {
        override fun setup() {
            item(bird)
            twoWay(pitTop, WEST)
            // EAST: twoWay from awkwardCanyon
        }
    }

    val pitTop = object : DarkRoom(
            """At your feet is a small pit breathing traces of white mist.  An east
            passage ends here except for a small crack leading on.""",
            "You're at top of small pit.")
    {
        override fun setup() {
        }
    }

    /*
        The player and the lantern; required properties.
     */

    override val player : Player = Player(outsideBuilding)

    val lantern = Lantern()

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

        A regular action defined for an item is only active when the item is
        held by the player. A vicinity action is active when the player is in
        the room with the item.

        A guard is attached to a action to only apply it if a guard condition
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

        override val description get() = if (isOpen.isOn) "The grate is open." else "The grate is closed."

        override val canBeTaken get() = false

        override fun setup() {
            vicinityAction("open", "unlock") { isOpen.turnOn() }
                    // Guards are LIFO, so the !isOpen.isOn check is performed first.
                    .guardedBy({ player has keys },
                            "The grate is locked and you don't have the key.")
                    .guardedBy({ !isOpen.isOn },
                            "The grate is already open.")

            vicinityAction("close") { isOpen.turnOff() }
        }
    }
    val grate = Grate()

    val cage = Item("cage",
            owned = "Wicker cage",
            dropped = "There is a small wicker cage discarded nearby.")

    val rod = Item("rod",
            owned = "Black rod",
            dropped = "A three foot black rod with a rusty star on an end lies nearby.")

    val bird : Item = object : Item("bird",
            owned = "- bird (in error) ",
            dropped = "A cheerful little bird is sitting here singing.")
    {
        override fun setup() {
            vicinityAction("take", "get", "catch") {
                if (referringTo(bird())) {
                    when {
                        player has rod ->
                            say("""The bird was unafraid when you entered, but as you approach it becomes
                                disturbed and you cannot catch it.""")
                        player has cage -> {
                            cage.moveTo(Item.LIMBO)
                            bird().moveTo(Item.LIMBO)
                            cagedBird.moveTo(player)
                            say("OK")
                        }
                        else -> say("You can catch the bird, but you cannot carry it.")
                    }
                } else {
                    pass()
                }
            }
        }

        override fun approveMoveTo(newOwner: ItemOwner): Boolean {
            if (newOwner == player &&  player has rod) {
                say("""The bird was unafraid when you entered, but as you approach it becomes
                    disturbed and you cannot catch it.""")
                return false
            }
            return true
        }
    }
    private fun bird() = bird // can't reference 'bird' from itself directly, so need this

    private fun cagedBird() = cagedBird // can't reference cagedBird in itself directly. ugh
    val cagedBird : Item = object : Item("bird", "cage",
            owned = "Little bird in cage",
            dropped = "There is a little bird in the cage.")
    {
        override fun setup() {
            // Allows 'open cage' and 'release bird'.
            // Also allows 'open bird' and 'release cage' but oh well.
            action("open", "release") {
                cagedBird().moveTo(Item.LIMBO)
                cage.moveTo(player)
                bird.moveTo(player.room)
            }
        }
    }

}
