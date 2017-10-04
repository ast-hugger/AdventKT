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
 * An action attached to a room or an item. This is usually done using the
 * `action` declaration of the room/item definition DSL, with the action effect
 * specified as a lambda. Other than
 *
 * If attached to a room, the action is only checked for applicability if the
 * player is in the room.
 *
 * If attached to an items as a regular action, the action is checked for
 * applicability only if it's currently owned by the player. If attached as a
 * vicinity action, it is checked only if it's in the same room as the player.
 *
 * @see Room.action
 * @see Item.action
 * @see Item.vicinityAction
 *
 * @author Vassili Bykov
 */
open class LocalAction(words: Collection<String>, private var effect: LocalAction.()->Unit)
    : Action(*(words.toTypedArray()))
{
    /**
     * The subjects of the currently executing command, captured so that the
     * effect block can example them.
     */
    internal var subjects = listOf<String>()
        private set

    /**
     * A DSL clause; amends the action's effect so that the original effect
     * is only invoked in the [guard] evaluates to true. Otherwise, the
     * [failMessage] is printed and the original effect is not invoked.
     * [guardedBy] clauses can be chained. In that case, they are applied
     * in LIFO order.
     */
    fun guardedBy(guard: LocalAction.()->Boolean, failMessage: String): LocalAction {
        val originalAction = effect
        effect = {
            if (guard()) {
                originalAction()
            } else {
                say(failMessage)
            }
        }
        return this
    }

    /**
     * Indicate whether any of the current subject words refer to the [item].
     */
    fun referringTo(item: Item) = subjects.any { it in item.names }

    override fun act(subjects: List<String>) {
        this.subjects = subjects
        effect()
    }
}