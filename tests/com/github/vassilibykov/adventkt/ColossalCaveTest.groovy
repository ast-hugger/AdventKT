package com.github.vassilibykov.adventkt

/**
 * Test setup infrastructure.
 */
abstract class ColossalCaveTest extends GroovyTestCase {

    def cave
    def player
    def parser

    /**
     * Cave and player are Kotlin globals, i.e. Java statics of the MainKt class,
     * so the test must reset the class for each test instance, to start with
     * the fresh world.
     */
    void setUp() {
        super.setUp()
        MainKt.reset()
        cave = MainKt.cave
        player = MainKt.player
        parser = cave.parser
        player.moveTo(cave.start) // normally done by World.play()
    }

    void play(String... commands) {
        for (String each : commands) {
            parser.process(each)
        }
    }

    void getEverythingFromBuilding() {
        play("in", "get all", "out")
    }

    void teleportToDebris() {
        play("in", "get all", "xyzzy", "turn on lamp")
    }
}
