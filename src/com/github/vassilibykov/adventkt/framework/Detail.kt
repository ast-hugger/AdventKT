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
 * A simple hidden [Fixture] with no special behavior other than providing a
 * description when it's looked at. Typically created by a [RoomDetailBuilder]
 * used via the DSL.
 *
 * @author Vassili Bykov
 */
class Detail(vararg names: String)
    : Fixture(*names, message = "")
{
    internal val extraVerbs = mutableSetOf<String>()

    override var isHidden = true

    override fun configure(context: World.ConfigurationContext) {
        super.configure(context)
        val allVerbs = extraVerbs.union(setOf("look", "examine", "l", "x"))
        vicinityAction(*allVerbs.toTypedArray()) {
            say(description)
        }
    }
}