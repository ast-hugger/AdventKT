/*
 * Copyright (c) 2017 Vassili Bykov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vassilibykov.adventkt.cave

import com.github.vassilibykov.adventkt.cave.MainKt

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

    void walkToSnakeWithoutBird() {
        walkToBirdWithCage()
        play("w", "d", "d")
    }

    void walkToSnakeWithBird() {
        walkToBirdWithCage()
        play("catch bird", "w", "d", "d")
    }

    void walkToNugget() {
        teleportToDebris()
        play("w", "w", "w", "d", "s")
    }

    void getNuggetAndTeleport() {
        walkToNugget()
        play("get nugget", "n", cave.magicWord)
    }
}
