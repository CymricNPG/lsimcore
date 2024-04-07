//package net.npg.lsimcore
//
//import net.npg.lsimcore.events.Event
//import net.npg.lsimcore.events.EventBorderControl
//import net.npg.lsimcore.events.EventBus
//import net.npg.lsimcore.events.EventWrapper
//import net.npg.lsimcore.time.fromMs
//import org.junit.jupiter.api.Test
//import java.util.concurrent.ForkJoinPool
//import kotlin.test.assertTrue
//
//internal class ModelTest {
//
//    @Test
//    fun testEvents() {
//        val eventBus = EventBus(ForkJoinPool.commonPool())
//
//        val modelBorder1 = EventBorderControl(eventBus)
//        val model1Received = mutableListOf<EventWrapper<Event2>>()
//        modelBorder1.registerReceiver(Event2::class.java) { model1Received.add(it) }
//
//        val modelBorder2 = EventBorderControl(eventBus)
//        val model2Received = mutableListOf<EventWrapper<Event1>>()
//        modelBorder2.registerReceiver(Event1::class.java) { model2Received.add(it) }
//
//        modelBorder1.sendEvent(Event1("from1"), fromMs(100))
//        modelBorder1.wait4Delivery()
//
//        assertTrue(model2Received.isEmpty())
//        assertTrue(model1Received.isEmpty())
//        modelBorder2.deliverPendingEvents()
//        assertTrue(model2Received.isNotEmpty())
//        assertTrue(model1Received.isEmpty())
//    }
//
//}
//
//class Event1(val payload: String) : Event {
//    override fun compareTo(other: Event): Int {
//        return hashCode().compareTo(other.hashCode())
//    }
//}
//
//class Event2(val payload: String) : Event {
//    override fun compareTo(other: Event): Int {
//        return hashCode().compareTo(other.hashCode())
//    }
//}