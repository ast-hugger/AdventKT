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

class BirdTest extends ColossalCaveTest {

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
