package com.github.vassilibykov.adventkt

class GrateTest extends ColossalCaveTest {

    void walkToGrate() {
        play("s", "s", "s")
    }

    void testImmovable() {
        walkToGrate()
        play("take grate")
        assertFalse(player.has(cave.grate))
    }

    void testGrateNeedsUnlocking() {
        walkToGrate()
        play("d")
        assertEquals(cave.outsideGrate, player.room)
    }

    void testGrateNeedsUnlockingFromBelow() {
        play("in", "xyzzy", "e", "e") // to belowGrate
        assertEquals(cave.belowGrate, player.room)
    }

    void testGrateUnlockableFromBelow() {
        play("in", "get all", "xyzzy", "e", "e") // to belowGrate
        play("unlock grate", "up")
        assertEquals(cave.outsideGrate, player.room)
    }

    void testGrateUnlockingNeedsKey() {
        walkToGrate()
        play("open grate")
        assertFalse(cave.grate.isOpen().isOn())
    }

    void testOpenGrate() {
        getEverythingFromBuilding()
        walkToGrate()
        play("open grate")
        assertTrue(cave.grate.isOpen().isOn())
    }

}
