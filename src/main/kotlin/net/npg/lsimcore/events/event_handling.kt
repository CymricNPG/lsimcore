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
import net.npg.lsimcore.base.LogLevel
import net.npg.lsimcore.base.Logger
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.function.Predicate

/**
 * a synchronous event
 */
class SimpleEvent<T : Any> private constructor(
    override val content: T,
    override val id: Id,
) : Event<T>, Comparable<SimpleEvent<T>> {

    override fun compareTo(other: SimpleEvent<T>): Int {
        return id.compareTo(other.id)
    }

    companion object {
        fun <T : Any> create(content: T): SimpleEvent<T> {
            return SimpleEvent(content, Id.create())
        }
    }
}

class SimpleEventBorder<T : Any>(
    private val broker: EventBroker<T>,
    private val eventsAccepted: Predicate<Event<T>>,
    private val logger: Logger
) : EventBorder<T>, EventReceiver<T>, Disposable {
    init {
        broker.registerEventReceiver(this).also { this.registrationEventBroker = it }
    }

    private var registrationEventBroker: Disposable
    private val sendConfirmations: MutableCollection<CompletableFuture<Unit>> = mutableListOf()
    private val eventQueue: MutableCollection<Event<T>> = mutableListOf()

    override fun sendEvent(event: Event<T>) {
        synchronized(sendConfirmations) {
            val confirmation = broker.sendEvent(event)
            sendConfirmations.add(confirmation)
        }
    }

    override fun wait4Delivery() {
        synchronized(sendConfirmations) {
            try {
                CompletableFuture.allOf(*sendConfirmations.toTypedArray()).get()
            } catch (ex: ExecutionException) {
                logger.log(LogLevel.ERROR, "Error while waiting for delivery: ", ex)
            } finally {
                sendConfirmations.clear()
            }
        }
    }

    override fun retrieveEvents(filter: Predicate<Event<T>>): Collection<Event<T>> {
        Objects.requireNonNull(filter)
        synchronized(eventQueue) {
            val each = eventQueue.iterator()
            val foundElements = mutableListOf<Event<T>>()
            while (each.hasNext()) {
                val next = each.next()
                if (filter.test(next)) {
                    foundElements.add(next)
                    each.remove()
                }
            }
            return foundElements
        }
    }

    override fun acceptEvent(event: Event<T>) {
        if (eventsAccepted.test(event)) {
            synchronized(eventQueue) {
                eventQueue.add(event)
            }
        }
    }

    override fun close() {
        registrationEventBroker.close()
        synchronized(eventQueue) {
            eventQueue.clear()
        }
    }
}

class SimpleEventBroker<T : Any> : EventBroker<T> {
    private val receivers: MutableList<EventReceiver<T>> = mutableListOf()

    override fun sendEvent(event: Event<T>): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync { processEvent(event) }
    }

    private fun processEvent(event: Event<T>) {
        synchronized(receivers) {
            receivers.forEach { r -> r.acceptEvent(event) }
        }
    }

    override fun registerEventReceiver(receiver: EventReceiver<T>): Disposable {
        synchronized(receivers) {
            receivers.add(receiver)
            return Disposable { synchronized(receivers) { receivers.remove(receiver) } }
        }
    }

}
