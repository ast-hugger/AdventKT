package com.github.vassilibykov.adventkt

import com.github.vassilibykov.adventkt.Direction.*

class ColossalCave: World() {

    /*
        All the rooms are declared first so they can be
        freely connected later.
     */

    val outsideBuilding = Room(
            """You are standing at the end of a road before a small brick building.
            Around you is a forest.  A small stream flows out of the building and
            down a gully.""",
            "You're in front of building.")

    val insideBuilding = Room(
            """You are inside a building, a well house for a large spring.""",
            "You're inside building.")

    val hill = Room(
            """You have walked up a hill, still in the forest.  The road slopes back
            down the other side of the hill.  There is a building in the distance.""",
            "You're at hill in road.")

    val valley = Room(
            """You are in a valley in the forest beside a stream tumbling along a
            rocky bed.""",
            "You're in valley.")

    val endOfRoad = Room(
            """The road, which approaches from the east, ends here amid the trees.""",
            "You're at end of road.")

    val cliff = Room(
            """The forest thins out here to reveal a steep cliff.  There is no way
            down, but a small ledge can be seen to the west across the chasm.""",
            "You're at cliff.")

    val forest = Room(
            "You are wandering aimlessly through the forest.",
            "You are wandering aimlessly through the forest.")

    val slit = Room(
            """At your feet all the water of the stream splashes into a 2-inch slit
            in the rock.  Downstream the streambed is bare rock.""",
            "You're at slit in streambed.")

    val outsideGrate = Room(
            """You are in a 20-foot depression floored with bare dirt.  Set into the
            dirt is a strong steel grate mounted in concrete.  A dry streambed
            leads into the depression.""",
            "You're outside grate.")

    val belowGrate = Room(
            """You are in a small chamber beneath a 3x3 steel grate to the surface.
            A low crawl over cobbles leads inward to the west.""",
            "You're below the grate.")

    val cobble = Room(
            """You are crawling over cobbles in a low passage.  There is a dim light
            at the east end of the passage.""",
            "You're in cobble crawl.")

    val debris = DarkRoom(
            """You are in a debris room filled with stuff washed in from the surface.
            A low wide passage with cobbles becomes plugged with mud and debris
            here, but an awkward canyon leads upward and west.  In the mud someone
            has scrawled, "MAGIC WORD XYZZY".""",
            "You're in debris room."
    )

    val awkwardCanyon = DarkRoom(
            "You are in an awkward sloping east/west canyon.",
            "You are in an awkward sloping east/west canyon.")

    val birdChamber = DarkRoom(
            """You are in a splendid chamber thirty feet high.  The walls are frozen
            rivers of orange stone.  An awkward canyon and a good passage exit
            from east and west sides of the chamber.""",
            "You're in bird chamber."
    )

    val pitTop = DarkRoom(
            """At your feet is a small pit breathing traces of white mist.  An east
            passage ends here except for a small crack leading on.""",
            "YOu're at top of small pit."
    )

    /*
        The player and the lantern; required properties.
     */

    override val player = Player(outsideBuilding)

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
        val isLocked = Toggle(true,
                turnOnMessage = "The grate is now locked.",
                turnOffMessage = "The grate is now unlocked.",
                alreadyOnMessage = "The grate is already locked.",
                alreadyOffMessage = "The grate is already unlocked.")

        val isOpen = Toggle(false,
                turnOnMessage = "You open the grate.",
                turnOffMessage = "You close the grate.",
                alreadyOnMessage = "The grate is already open.",
                alreadyOffMessage = "The grate is already closed.")

        override fun description() = if (isOpen.isOn) "The grate is open." else "The grate is closed."

        override fun canBeTaken() = false // the player can't pick this up

        init {
            vicinityVerb("unlock") { isLocked.turnOff() }
                    .guardedBy({ player has keys }, "You don't have a key.")

            vicinityVerb("lock") { isLocked.turnOn() }
                    .guardedBy({ player has keys }, "You don't have a key.")

            vicinityVerb("open") { isOpen.turnOn() }
                    .guardedBy({ !isLocked.isOn }, "The grate is locked.")

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
            dropped = "A cheerful little bird is sitting here singing.") {
        init {
            verb("take", "get", "catch") {
                if (referringTo(item)) {
                    when {
                        player has rod ->
                            say("""The bird was unafraid when you entered, but as you approach it becomes
                                disturbed and you cannot catch it.""")
                        player has cage -> {
                            Item.LIMBO.ownItem(cage)
                            Item.LIMBO.ownItem(item)
                            player.ownItem(cagedBird)
                            say("OK")
                        }
                        else -> say("You can catch the bird, but you cannot carry it.")
                    }
                } else {
                    pass()
                }
            }
        }
    }

    val cagedBird = Item("bird",
            owned = "Little bird in cage",
            dropped = "There is a little bird in the cage.")

    init {
        with (outsideBuilding) {
            twoWay(insideBuilding, IN, EAST)
            twoWay(hill, WEST)
            oneWay(hill, UP)
            oneWay(forest, NORTH)
            twoWay(valley, DOWN, SOUTH)
            verb("downstream") { player.moveTo(valley) }
        }

        with (insideBuilding) {
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

        with (hill) {
            twoWay(endOfRoad, WEST)
            oneWay(forest, NORTH)
            oneWay(forest, SOUTH)
            verb("down") { say("Which way?")}
        }

        with (endOfRoad) {
            oneWay(forest, NORTH)
            oneWay(forest, SOUTH)
        }

        with (forest) {
            twoWay(forest, NORTH)
            twoWay(forest, NORTHWEST)
            twoWay(forest, WEST)
            twoWay(forest, SOUTHWEST)
            oneWay(outsideBuilding, OUT)
        }

        with (valley) {
            twoWay(slit, DOWN, SOUTH)
            verb("downstream") { player.moveTo(slit) }
        }

        with (slit) {
            twoWay(outsideGrate, SOUTH)
        }

        with (outsideGrate) {
            item(grate)
            verb("down", "in") { player.moveTo(belowGrate)}
                    .guardedBy({ grate.isOpen.isOn }, "The grate is closed.")
        }

        with (belowGrate) {
            twoWay(cobble, WEST)
            verb("up", "out") { player.moveTo(outsideGrate) }
                    .guardedBy({ grate.isOpen.isOn }, "The grate is closed.")
        }

        with (cobble) {
            item(cage)
            twoWay(debris, WEST, UP)
        }

        with (debris) {
            item(rod)
            twoWay(awkwardCanyon, WEST)
            verb("xyzzy") {
                say(">>Foof!<<")
                player.moveTo(insideBuilding)
            }
        }

        with (awkwardCanyon) {
            twoWay(birdChamber, WEST)
        }

        with (birdChamber) {
            item(bird)
            twoWay(pitTop, WEST)
        }
    }
}
