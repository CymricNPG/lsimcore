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

import java.util.concurrent.CompletableFuture

/**
 * EventBorder is a simplified interface to any event delivery system.
 */

/**
 * generic event with a content
 */
interface Event<T : Any> {
    val content: T
}


/**
 * the interface to send and receive events. Two event types are supported: synchronous events and asnyc events
 */
interface EventBorder {

    /**
     * send a synchronous, for which the caller can wait for a confirmation of the delivery
     */
    fun sendEvent(event: Event<*>)

    /**
     * send an event where we don't care when ist delivered
     */
    fun sendAsyncEvent(event: Event<*>)

    /**
     * wait till all synchronous events are delivered
     */
    fun wait4Delivery()

    /**
     * poll all waiting events
     */
//
//    fun
}

interface EventReceiver {
    fun acceptEvent(event: Event<*>)
    fun isDeliverableEvent(event: Event<*>): Boolean
}

fun interface Disposable : AutoCloseable

interface EventBroker {
    /**
     * send a synchronous, for which the caller can wait for a confirmation of the delivery
     */
    fun sendSyncEvent(event: Event<*>): CompletableFuture<Unit>

    /**
     * send an event where we don't care when ist delivered
     */
    fun sendAsyncEvent(event: Event<*>)

    fun registerEventReceiver(receiver: EventReceiver): Disposable

}