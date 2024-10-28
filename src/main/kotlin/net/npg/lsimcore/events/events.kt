/************************************************************************************
 *     Copyright 2024 Roland Spatzenegger
 *     This file is part of LSimCore.
 *
 *     LSimCore is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     LSimCore is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with LSimCore.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************************/
package net.npg.lsimcore.events

import net.npg.lsimcore.base.Id
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

/**
 * EventBorder is a simplified interface to any event delivery system.
 */

/**
 * generic event with a content
 */
interface Event<T : Any> {
    val id: Id
    val content: T
}


/**
 * the interface to send and receive events. Two event types are supported: synchronous events and asnyc events
 */
interface EventBorder<T : Any> {

    /**
     * send a for which the caller can wait for a confirmation of the delivery
     */
    fun sendEvent(event: Event<T>)

    /**
     * wait till all synchronous events are delivered
     */
    fun wait4Delivery()

    fun retrieveEvents(filter: Predicate<Event<T>>): Collection<Event<T>>
}

/**
 * receives all events
 */
interface EventReceiver<T : Any> {
    fun acceptEvent(event: Event<T>)
}

fun interface Disposable : AutoCloseable

interface EventBroker<T : Any> {
    /**
     * sends an event for which the caller can wait for a confirmation of the delivery
     */
    fun sendEvent(event: Event<T>): CompletableFuture<Unit>

    fun registerEventReceiver(receiver: EventReceiver<T>): Disposable

}