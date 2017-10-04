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
