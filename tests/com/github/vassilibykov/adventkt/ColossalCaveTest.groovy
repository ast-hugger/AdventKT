package com.github.vassilibykov.adventkt

class ColossalCaveTest extends GroovyTestCase {

    def cave
    def player
    def parser

    void setUp() {
        super.setUp()
        cave = MainKt.cave
        player = MainKt.player
        parser = new Parser()
    }

    void testMoveSouth() {
        parser.process("s")
        assertEquals(cave.valley, player.room)
    }
}
