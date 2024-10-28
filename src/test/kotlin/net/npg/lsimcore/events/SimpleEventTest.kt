package net.npg.lsimcore.events

import io.mockk.every
import io.mockk.mockk
import net.npg.lsimcore.base.StdoutLogger
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

class SimpleEventTest {
    @Test
    fun testSimpleEventBorder() {
        val logger = StdoutLogger()
        val called = AtomicInteger(0)

        val broker = mockk<SimpleEventBroker<String>>()
        every { broker.registerEventReceiver(any()) }.returns({ called.incrementAndGet() })
        val returnValue = CompletableFuture<Unit>()
        every { broker.sendEvent(any()) }.returns(returnValue)
        SimpleEventBorder(broker, { true }, logger).use { border ->
            border.sendEvent(SimpleEvent.create("Hallo"))
            returnValue.complete(Unit)
            border.wait4Delivery()
        }
        assertEquals(1, called.get())
    }

}