package com.github.vassilibykov.adventkt

class BirdTest extends ColossalCaveTest {

    void walkToBirdEmptyHanded() {
        teleportToDebris()
        play("w", "w")
    }

    void walkToBirdWithCage() {
        teleportToDebris()
        play("e", "get cage", "w", "w", "w")
    }

    void walkToBirdWithCageAndRod() {
        teleportToDebris()
        play("get rod", "e", "get cage", "w", "w", "w")
    }

    void assertBirdNotCaught() {
        assertTrue(cave.birdChamber.has(cave.bird))
        assertFalse(player.has(cave.cagedBird))
    }

    void assertBirdCaught() {
        assertFalse(cave.birdChamber.has(cave.bird))
        assertTrue(player.has(cave.cagedBird))
    }

    void testBirdIsThere() {
        walkToBirdEmptyHanded()
        assertTrue(player.room.has(cave.bird))
    }

    void testCantCatchWithoutCage() {
        walkToBirdEmptyHanded()
        play("catch bird")
        assertBirdNotCaught()
    }

    void testCanCatchWithCage() {
        walkToBirdWithCage()
        play("catch bird")
        assertBirdCaught()
    }

    void testCantCatchWithCageInRoomButNotHeld() {
        walkToBirdWithCage()
        play("drop cage", "catch bird")
        assertBirdNotCaught()
    }

    void testCantCatchWithRod() {
        walkToBirdWithCageAndRod()
        play("catch bird")
        assertBirdNotCaught()
    }

    void testCatchAndRelease() {
        walkToBirdWithCage()
        play("catch bird", "release bird")
        assertBirdNotCaught()
    }
}
