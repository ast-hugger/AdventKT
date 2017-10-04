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

class GrateTest extends ColossalCaveTest {

    void walkToGrate() {
        play("s", "s", "s")
    }

    void testFixedInPlace() {
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
        assertFalse(cave.grateOpen.isOn())
    }

    void testOpenGrate() {
        getEverythingFromBuilding()
        walkToGrate()
        play("open grate")
        assertTrue(cave.grateOpen.isOn())
    }
}
