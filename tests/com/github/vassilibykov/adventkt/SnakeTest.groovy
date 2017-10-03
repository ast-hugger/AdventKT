package com.github.vassilibykov.adventkt

class SnakeTest extends ColossalCaveTest {

    void testSnakeIsThere() throws Exception {
        walkToSnakeWithoutBird()
        assertTrue(player.room.has(cave.snake))
    }

    void testBirdWorks() {
        walkToSnakeWithBird()
        play("release bird")
        assertFalse(player.room.has(cave.snake))
        assertTrue(player.room.has(cave.bird))
    }
}
