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
import java.util.concurrent.ConcurrentHashMap.newKeySet
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveAction

///**
// * a synchronous event
// */
//class SyncEvent<T : Event<T>>(
//    override val content: T,
//) : Event<T> {
//}
//
///**
// * an asynchronous event
// */
//class AsyncEvent<T : Event<T>>(
//    override val content: T,
//) : Event<T> {
//}
//
//
//interface EventWrapper<T : Event> : Event {
//    val event: T;
//    val validTime: Time
//}
//
//class BorderEventWrapper<T : Event>(
//    override val event: T,
//    override val validTime: Time
//) : EventWrapper<T> {
//    override fun compareTo(other: Event): Int {
//        if (other is EventWrapper<*>) {
//            if (validTime.compareTo(other.validTime) != 0) {
//                return validTime.compareTo(other.validTime)
//            }
//        }
//        return hashCode().compareTo(other.hashCode())
//    }
//}
//
//
//class EventBorderControl(val eventBus: EventBus) {
//
//    private val eventsDelivered = mutableListOf<CompletableFuture<Unit>>()
//    private val borderEventQueue = BorderEventQueue()
//
//    fun sendEvent(event: Event, time: Time) {
//        synchronized(eventsDelivered) {
//            eventsDelivered.add(eventBus.sendEvent(BorderEventWrapper(event, time)))
//        }
//    }
//
//    fun wait4Delivery() {
//        synchronized(eventsDelivered) {
//            if (eventsDelivered.isNotEmpty()) {
//                CompletableFuture.allOf(*eventsDelivered.toTypedArray()).join()
//                eventsDelivered.clear()
//            }
//        }
//    }
//
//    fun <E : Event> registerReceiver(eventType: Class<E>, receiver: EventReceiver<EventWrapper<E>>) {
//        eventBus.registerSink(BorderSink(Id.create(), eventType, borderEventQueue, receiver))
//    }
//
//    fun deliverPendingEvents() {
//        val tasks = borderEventQueue.getTasks()
//        tasks.forEach { (it.receiver as EventReceiver<BorderEventWrapper<out Event>>).invoke(it.event) }
//    }
//}
//
//class BorderEventQueue() {
//    private val eventQueue = PriorityBlockingQueue<BorderDeliveryTask<out Event>>()
//
//    fun add(task: BorderDeliveryTask<out Event>) {
//        eventQueue.add(task)
//    }
//
//    //TODO add criteria
//    fun getTasks(): List<BorderDeliveryTask<out Event>> {
//        val copy = eventQueue.toList()
//        eventQueue.clear()
//        return copy
//    }
//}
//
//interface DeliveryTask<T : Event> : Comparable<DeliveryTask<T>> {
//    val event: T
//    val receiver: EventReceiver<T>
//}
//
//class BorderDeliveryTask<E : Event>(
//    override val event: BorderEventWrapper<E>,
//    override val receiver: EventReceiver<BorderEventWrapper<E>>
//) : DeliveryTask<BorderEventWrapper<E>> {
//    override fun compareTo(other: DeliveryTask<BorderEventWrapper<E>>): Int {
//        return event.compareTo(other.event)
//    }
//
//}
//
//interface Sink<T : Event> {
//    fun sendEvent(event: T)
//    fun isValidEvent(event: T): Boolean
//}
//
//class BorderSink<E : Event>(
//    private val id: Id,
//    private val eventType: Class<E>,
//    private val borderEventQueue: BorderEventQueue,
//    private val receiver: EventReceiver<BorderEventWrapper<E>>
//) : Sink<BorderEventWrapper<E>> {
//    override fun sendEvent(eventWrapper: BorderEventWrapper<E>) {
//        if (isValidEvent(eventWrapper)) {
//            borderEventQueue.add(
//                BorderDeliveryTask(eventWrapper, receiver)
//            )
//        } else {
//            throw IllegalArgumentException("Not allowed: $eventWrapper in $id")
//        }
//    }
//
//    override fun isValidEvent(eventWrapper: BorderEventWrapper<E>): Boolean {
//        return eventType.isInstance(eventWrapper.event)
//    }
//}

class ThreadedEventBus(private val forkJoinPool: ForkJoinPool) : EventBroker {

    private val sinks: MutableSet<EventReceiver> = newKeySet()

    override fun sendSyncEvent(event: Event<*>): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync({
            sendEventToSinks(event)
        }, forkJoinPool)
    }

    private fun sendEventToSinks(event: Event<*>) {
        synchronized(sinks) {
            val tasks = sinks
                .filter { it.isDeliverableEvent(event) }
                .map {
                    object : RecursiveAction() {
                        override fun compute() {
                            it.acceptEvent(event)
                        }
                    }
                }.toList()
            ForkJoinTask.invokeAll(tasks)
        }
    }


    override fun sendAsyncEvent(event: Event<*>) {
        CompletableFuture.supplyAsync({
            sendEventToSinks(event)
        }, forkJoinPool)
    }

    override fun registerEventReceiver(receiver: EventReceiver): Disposable {
        synchronized(sinks) {
            require(!sinks.contains(receiver))
            sinks.add(receiver)
            return Disposable { removeEventReceiver(receiver) }
        }
    }

    private fun removeEventReceiver(receiver: EventReceiver) {
        synchronized(sinks) {
            require(sinks.contains(receiver))
            sinks.remove(receiver)
        }
    }

}
