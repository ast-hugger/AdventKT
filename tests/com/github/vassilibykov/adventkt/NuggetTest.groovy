package com.github.vassilibykov.adventkt

class NuggetTest extends ColossalCaveTest {

    void testNugetIsThere() {
        walkToNugget()
        assertTrue(player.room.has(cave.nugget))
    }

    void testCantBringUpstairs() {
        walkToNugget()
        play("get nugget", "w", "u")
        assertEquals(cave.mistHall, player.room)
    }

    void testTeleportWithNugget() {
        walkToNugget()
        play("get nugget", "w", cave.magicWord)
        assertEquals(cave.outsideGrate, player.room)
    }
}
