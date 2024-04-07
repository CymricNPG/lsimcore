package net.npg.lsimcore

import io.mockk.mockk
import net.npg.lsimcore.events.Event
import net.npg.lsimcore.events.EventReceiver
import net.npg.lsimcore.events.ThreadedEventBus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertNotNull


internal class EventBusTest {

    private var received = AtomicInteger(0)

    @Test
    fun registerSink() {
        val eventBus = ThreadedEventBus(mockk())
        val disposable = eventBus.registerEventReceiver(object : EventReceiver {
            override fun acceptEvent(event: Event<*>) = Unit
            override fun isDeliverableEvent(event: Event<*>): Boolean = true
        })
        assertNotNull(disposable)
        disposable.close()
    }

    fun receive(event: Event<*>) {
        Thread.sleep(50)
        received.incrementAndGet()
    }

    @Test
    fun sendEvent() {
        val parallelism = ForkJoinPool.getCommonPoolParallelism()
        val stealer = Executors.newWorkStealingPool(parallelism) as ForkJoinPool

        val eventBus = ThreadedEventBus(stealer)
        received.set(0)
        eventBus.registerEventReceiver(object : EventReceiver {
            override fun acceptEvent(event: Event<*>) = receive(event)
            override fun isDeliverableEvent(event: Event<*>): Boolean = true
        }).use {
            val waiter = eventBus.sendSyncEvent(MyEvent(0))
            assertEquals(0, received.get()) // it can happen that the event is delivered before send returns!
            waiter.join()
            assertEquals(1, received.get())
        }
    }

    @Test
    fun sendEventFiltered() {
        val parallelism = ForkJoinPool.getCommonPoolParallelism()
        val stealer = Executors.newWorkStealingPool(parallelism) as ForkJoinPool

        val eventBus = ThreadedEventBus(stealer)
        received.set(0)
        eventBus.registerEventReceiver(object : EventReceiver {
            override fun acceptEvent(event: Event<*>) = receive(event)
            override fun isDeliverableEvent(event: Event<*>): Boolean = false
        }).use {
            val waiter = eventBus.sendSyncEvent(MyEvent(0))
            assertEquals(0, received.get()) // it can happen that the event is delivered before send returns!
            waiter.join()
            assertEquals(0, received.get())
        }
    }
}

class MyEvent(override val content: Int) : Event<Int>