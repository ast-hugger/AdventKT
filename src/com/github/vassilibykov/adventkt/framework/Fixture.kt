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

package com.github.vassilibykov.adventkt.framework

/**
 * An [Item] that can't be picked up by the player, and in general refuses to
 * be moved to another location as part of the regular [Item.moveTo] protocol.
 *
 * @author Vassili Bykov
 */
open class Fixture(vararg names: String, message: ()->String)
    : Item(*names, owned = { throw IllegalStateException("fixture should not be owned") }, dropped = message)
{
    constructor(vararg names: String, message: String)
        :this(*names, message = { message })

    internal constructor(vararg names: String)
            : this(*names, message = "no description() for $names[0]")

    /**
     * The message displayed when the player tries to pick up this item.
     */
    var cantTakeMessage = "The $primaryName is fixed in place."

    override fun approveMoveTo(newOwner: ItemOwner): Boolean {
        return owner == LIMBO
                || newOwner == LIMBO
                || { say(cantTakeMessage); false }()
    }
}