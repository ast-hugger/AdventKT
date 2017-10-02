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

        See the Room class documentation for why we use configure()
        methods instead of regular init{} blocks.
     */

    val finePrint = Detail("fine", "print",
            description = "\"The Implementor's Prize has not been implemented yet.\"",
            verbs = setOf("look", "examine", "x", "l", "read"))

    val outsideBuilding = object : OpenSpace("You're in front of building.",
            """You are standing at the end of a road before a small brick building.
            Around you is a forest.  A small stream flows out of the building and
            down a gully.""",
            {forest})
    {
        override fun configure() {
            twoWay(insideBuilding, IN, EAST)
            twoWay(hill, WEST)
            oneWay(hill, UP)
            oneWay(forest, NORTH)
            twoWay(valley, DOWN, SOUTH)
            item(adventKtSign)
            // Unlike Zork and later Infocom games, the original CC didn't support
            // examining details of the scenery with command like "look at the stream".
            // Here is how details can be defined in AdventKT.
            item(finePrint)
            action("downstream") { player.moveTo(valley) }
        }

        override fun noticePlayerMoveFrom(oldRoom: Room) {
            if (player has nugget) {
                say("""Congratulations!""")
                throw QuitException()
            } else {
                super.noticePlayerMoveFrom(oldRoom)
            }
        }
    }

    override val start = outsideBuilding

    val adventKtSign = object : Fixture("sign", description = "A freshly painted sign is hanging outside the building.") {
        override fun configure() {
            super.configure()
            vicinityAction("read", "look") {
                say("""The sign says:
                    |    Welcome to AdventKT, a theme park based on the legendary Colossal Cave.
                    |    There is a priceless gold nugget hidden in the cave, protected by
                    |    an ancient curse. Bring it here to win the Implementor's Prize!
                    |There is some fine print at the bottom of the sign.""")
            }
        }
    }

    val insideBuilding = object : Room("You're inside building.",
            """You are inside a building, a well house for a large spring.""")
    {
        override fun configure() {
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
                    |It would be advisable to use the exit.""")
            }
        }
    }

    val hill = object : OpenSpace("You're at hill in road.",
            """You have walked up a hill, still in the forest.  The road slopes back
            |down the other side of the hill.  There is a building in the distance.""",
            {forest})
    {
        override fun configure() {
            // EAST: twoWay from outsideBuilding
            twoWay(endOfRoad, WEST)
            action("down") { say("Which way?")}
        }
    }

    val endOfRoad = object : OpenSpace("You're at end of road.",
            """The road, which approaches from the east, ends here amid the trees.""",
            {forest})
    {
        override fun configure() {
            // EAST: twoWay from hill
        }
    }

    val cliff = object : Room("You're at cliff.",
            """The forest thins out here to reveal a steep cliff.  There is no way
            |down, but a small ledge can be seen to the west across the chasm.""")
    {
        override fun configure() {
        }
    }

    val valley = object : OpenSpace("You're in valley.",
            """You are in a valley in the forest beside a stream tumbling along a
            |rocky bed.""",
            {forest})
    {
        override fun configure() {
            // UP, EAST: twoWay from outsideBuilding
            twoWay(slit, DOWN, SOUTH)
            action("downstream") { player.moveTo(slit) }
        }
    }

    val magicWord = random("plover", "plugh", "zork", "foobar", "blorple", "frob", "foo", "quux",
            "wibble", "wobble", "wubble", "flob", "blep", "blah", "fnord", "piyo")

    val forest: Room = object : OpenSpace("You are wandering aimlessly through the forest.",
            "You are wandering aimlessly through the forest.",
            {forest()})
    {
        var stepCount = 0

        override fun configure() {
            oneWay(outsideBuilding, OUT)
            oneWay(UnenterableRoom("The trees are too difficult to climb."), UP)
        }

        override fun noticePlayerMoveFrom(oldRoom: Room) {
            if (oldRoom != this) {
                say("""You enter the forest and soon become lost among the trees.""")
                stepCount = 0
            } else {
                super.noticePlayerMoveFrom(oldRoom)
                when(++stepCount) {
                    2 -> say("Are you sure you are not walking in circles?")
                    4 -> say("""You think you see your tracks on the forest floor.
                        |But then again, you are not much of a tracker.""")
                    6 -> say("A forest is not a maze. Maybe you need to try something else.")
                    8 -> say("You are feeling tired. All you wish for is to get out.")
                }
            }
        }

        override fun noticePlayerMoveTo(newRoom: Room) {
            if (newRoom != this) {
                say("You finally found your way back to the well house.")
            }
            super.noticePlayerMoveTo(newRoom)
        }

        override fun approveItemMoveFrom(oldOwner: ItemOwner, item: Item): Boolean {
            if (oldOwner == player) {
                // When releasing the bird, the bird is moved here from limbo, not player.
                say("""You realize you might never find your way back here
                    |and decide against dropping it.""")
                return false
            }
            return true
        }

        override fun noticeItemMoveFrom(oldOwner: ItemOwner, item: Item) {
            if (item == bird) {
                // The player is releasing the bird.
                say("""The bird is singing to you in gratitude for your having returned it to
                    |its home.  In return, it informs you of a magic word which it thinks
                    |you may find useful somewhere near the Hall of Mists.  The magic word
                    |changes frequently, but for now the bird believes it is """" + magicWord + """".  You
                    |thank the bird for this information, and it flies off into the forest.""")
                bird.moveTo(Item.LIMBO)
            }
        }
    }
    fun forest() = forest

    val slit = object : OpenSpace("You're at slit in streambed.",
            """At your feet all the water of the stream splashes into a 2-inch slit
            |in the rock.  Downstream the streambed is bare rock.""",
            {forest})
    {
        override fun configure() {
            // UP, NORTH: twoWay from slit
            twoWay(outsideGrate, SOUTH)
        }
    }

    // strangely, need this explicit type to avoid a type checker recursive loop
    val outsideGrate: Room = object : OpenSpace("You're outside grate.",
            """You are in a 20-foot depression floored with bare dirt.  Set into the
            |dirt is a strong steel grate mounted in concrete.  A dry streambed
            |leads into the depression.""",
            {forest})
    {
        override fun configure() {
            item(grate)
            // NORTH: twoWay from slit
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
            |A low crawl over cobbles leads inward to the west.""")
    {
        override fun configure() {
            unownedItem(grate) // the official owner is outsideGrate, but also visible here
            twoWay(cobble, WEST)
        }
    }


    val cobble = object : Room(
            "You're in cobble crawl.",
            """You are crawling over cobbles in a low passage.  There is a dim light
            |at the east end of the passage.""")
    {
        override fun configure() {
            item(cage)
            twoWay(debris, WEST, UP)
            // EAST: twoWay from belowGrate
        }
    }

    val debris: DarkRoom = object : DarkRoom(
            "You're in debris room.",
            """You are in a debris room filled with stuff washed in from the surface.
            |A low wide passage with cobbles becomes plugged with mud and debris
            |here, but an awkward canyon leads upward and west.  In the mud someone
            |has scrawled, "MAGIC WORD XYZZY".""")
    {
        override fun configure() {
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
        override fun configure() {
            twoWay(birdChamber, WEST)
            // EAST: twoWay from debris
        }
    }

    val birdChamber = object : DarkRoom(
            "You're in bird chamber.",
            """You are in a splendid chamber thirty feet high.  The walls are frozen
            |rivers of orange stone.  An awkward canyon and a good passage exit
            |from east and west sides of the chamber.""")
    {
        override fun configure() {
            item(bird)
            twoWay(pitTop, WEST)
            // EAST: twoWay from awkwardCanyon
        }
    }

    val pitTop = object : DarkRoom("You're at top of small pit.",
            """At your feet is a small pit breathing traces of white mist.  An east
            |passage ends here except for a small crack leading on.""")
    {
        override fun configure() {
            twoWay(mistHall, DOWN)
            oneWay(crack, WEST)
        }
    }

    val crack = UnenterableRoom(
            """The crack is far too small for you to follow.  At its widest it is
            |barely wide enough to admit your foot.""")

    val mistHall: Room = object : DarkRoom("You're in Hall of Mists.",
            """You are at one end of a vast hall stretching forward out of sight to
            |the west.  There are openings to either side.  Nearby, a wide stone
            |staircase leads downward.  The hall is filled with wisps of white mist
            |swaying to and fro almost as if alive.  A cold wind blows up the
            |staircase.  There is a passage at the top of a dome behind you.""")
    {
        override fun configure() {
            // UP: twoWay from pitTop
            twoWay(eastBank, WEST)
            twoWay(kingHall, DOWN)
            oneWay(kingHall, NORTH) // the return passage from kingHall is EAST
            twoWay(nuggetRoom, EAST)
            action(magicWord) {
                say("""The cave walls around you become a blur.
                    |>>Foof!<<""")
                player.moveTo(outsideGrate)
            }
        }

        override fun approvePlayerMoveTo(newRoom: Room): Boolean {
            if (newRoom == pitTop && player has nugget) {
                say("An invisible force stops you from climbing the dome.")
                return false
            }
            return true
        }
    }

    val eastBank = object : DarkRoom("You're on east bank of fissure.",
            """You are on the east bank of a fissure slicing clear across the hall.
            |The mist is quite thick here, and the fissure is too wide to jump.""")
    {
        override fun configure() {
            // EAST: twoWay from mistHall
        }
    }

    val nuggetRoom = object : DarkRoom("You're in nugget-of-gold room.",
            """This is a low room with a crude note on the wall.  The note says,
            |"You won't get it up the steps".""")
    {
        override fun configure() {
            // DOWN, WEST: twoWay from mistHall
            item(nugget)
        }
    }

    val nugget = Item("nugget", "gold",
            owned = "Large gold nugget",
            dropped = "There is a large sparkling nugget of gold here!")

    val kingHall = object : DarkRoom("You're in Hall of Mt King.",
            """You are in the Hall of the Mountain King, with passages off in all
            |directions.""")
    {
        override fun configure() {
            // UP: twoWay from mistHall
            oneWay(mistHall, EAST) // the return passage from mistHall is NORTH
            oneWay(unimplementedPassage, NORTH)
            oneWay(unimplementedPassage, SOUTH)
            oneWay(unimplementedPassage, WEST)
            oneWay(unimplementedPassage, SOUTHWEST)
            item(snake)
        }

        override fun approvePlayerMoveTo(newRoom: Room): Boolean {
            if (this has snake && newRoom != mistHall) {
                say("You can't get by the snake.")
                return false
            }
            return true
        }

        override fun noticeItemMoveFrom(oldOwner: ItemOwner, item: Item) {
            if (item == bird && this has snake) {
                say("""The little bird attacks the green snake, and in an astounding flurry
                    |drives the snake away.""")
                snake.moveTo(Item.LIMBO)
            }
        }
    }

    val unimplementedPassage = UnenterableRoom(
            """The passage is blocked by orange safety fencing with a sign saying,
            |"No entry. This part of the cave is under construction.""")

    val snake = object : Item("snake", owned = "", dropped = "A huge green fierce snake bars the way!") {
        override fun approveMoveTo(newOwner: ItemOwner): Boolean {
            if (newOwner == player) {
                say("This doesn't sound like a very good idea.")
                return false
            }
            return true
        }
    }

    /*
        Items
     */

    val lantern = Lantern()

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
        The grate is is a fixture rather than item, so it can't be picked up
        by the player. It also illustrates vicinity actions and guards.

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

    inner class Grate : Fixture("grate", "door") {
        val isOpen = Toggle(false,
                turnOnMessage = "You unlock and open the grate.",
                turnOffMessage = "You close and lock the grate.",
                alreadyOnMessage = "The grate is already open.",
                alreadyOffMessage = "The grate is already closed.")

        override val description get() = if (isOpen.isOn) "The grate is open." else "The grate is closed."

        override fun configure() {
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

    val rod = object : Item("rod",
            owned = "Black rod",
            dropped = "A three foot black rod with a rusty star on an end lies nearby.") {
        override fun configure() {
            action("wave") {
                if (player.room == eastBank) {
                    say("""A hollow voice says, "Sorry, the bridge has not been built yet."""")
                } else {
                    say("You look ridiculous waving the black rod.")
                }
            }
        }
    }

    val bird : Item = object : Item("bird")
    {
        override val description: String get() =
            if (Item.LIMBO has snake)
                """A little bird is sitting here looking sad and lonely.
                |It probably misses its home in the forest."""
            else
                "A cheerful little bird is sitting here singing."

        override fun configure() {
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
                            say("You catch the bird and put in the cage.")
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
                say("""As you approach, the bird becomes disturbed and you cannot catch it.""")
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
        override fun configure() {
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
