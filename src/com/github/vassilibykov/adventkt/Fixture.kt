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

package com.github.vassilibykov.adventkt

/**
 * An [Item] that can't be picked up by the player. (And in general refuses to
 * be moved to another location).
 *
 * @author Vassili Bykov
 */
open class Fixture(vararg names: String, dropped: String)
    : Item(*names, owned = "$names[0] should not be owned", dropped = dropped)
{
    internal constructor(vararg names: String)
            : this(*names, dropped = "no description() for $names[0]")

    var cantTakeMessage = "The $primaryName is fixed in place."

    override fun approveMoveTo(newOwner: ItemOwner): Boolean {
        return if (owner != Item.LIMBO) {
            say(cantTakeMessage)
            false
        } else {
            true
        }
    }
}