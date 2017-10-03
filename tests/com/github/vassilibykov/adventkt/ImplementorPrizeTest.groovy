package com.github.vassilibykov.adventkt

class ImplementorPrizeTest extends ColossalCaveTest {

    void testImplementorPrizeIsThere() {
        getNuggetAndTeleport()
        play("n", "n", "n")
        assertTrue(player.room.has(cave.implementorPrize))
    }
}
