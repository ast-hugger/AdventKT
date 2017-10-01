package com.github.vassilibykov.adventkt

class ColossalCaveTest extends GroovyTestCase {

    def cave
    def player
    def parser

    void setUp() {
        super.setUp()
        MainKt.reset()
        cave = MainKt.cave
        player = MainKt.player
        parser = new Parser()
    }

    void replay(String... commands) {
        for (String each : commands) {
            parser.process(each)
        }
    }

    void getAll() {
        replay("in", "get all", "out")
    }

    void walkToGrate() {
        replay("s", "s", "s")
    }

    void testMoveSouth() {
        parser.process("s")
        assertEquals(cave.valley, player.room)
    }

    void testGetAll() {
        getAll()
        assertTrue(player.has(cave.keys))
        assertTrue(player.has(cave.lantern))
    }

    void testGrateNeedsUnlocking() {
        walkToGrate()
        replay("d")
        assertEquals(cave.outsideGrate, player.room)
    }

    void testGrateUnlockingNeedsKey() {
        walkToGrate()
        replay("open grate")
        assertFalse(cave.grate.isOpen().isOn())
    }

    void testOpenGrate() {
        getAll()
        walkToGrate()
        replay("open grate")
        assertTrue(cave.grate.isOpen().isOn())
    }
}
