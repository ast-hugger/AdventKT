package com.github.vassilibykov.adventkt

class InventoryActionsTest extends ColossalCaveTest{

    void testGet() {
        play("in", "get keys")
        assertTrue(player.has(cave.keys))
    }

    void testDrop() {
        play("in", "get keys", "out", "drop keys")
        assertFalse(player.has(cave.keys))
        assertTrue(cave.outsideBuilding.has(cave.keys))
    }

    void testGetAll() {
        getEverythingFromBuilding()
        assertTrue(player.has(cave.keys))
        assertTrue(player.has(cave.lantern))
    }

}
