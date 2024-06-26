package net.npg.lsimcore

import net.npg.lsimcore.base.Id
import net.npg.lsimcore.time.WorkTask
import net.npg.lsimcore.time.WorkTaskImpl
import net.npg.lsimcore.time.fromMs
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class WorkTaskTest {

    @Test
    fun compareTo() {
        val id1 = Id.create()
        val id2 = Id.create()
        val list = mutableListOf(
            WorkTaskImpl(fromMs(1000), id2, false),
            WorkTaskImpl(fromMs(500), id2, false),
            WorkTaskImpl(fromMs(1000), id1, true),
            WorkTaskImpl(fromMs(500), id1, true),
        )
        Assertions.assertThat(list[0]).isGreaterThan(list[2])
        Assertions.assertThat(list[0]).isGreaterThan(list[3])
        Assertions.assertThat(list[1]).isLessThan(list[2])
        Assertions.assertThat(list[1]).isGreaterThan(list[3])

        list.sort()
        assertEquals(fromMs(500), list[0].time)
        assertEquals(fromMs(500), list[1].time)
        assertEquals(fromMs(1000), list[2].time)
        assertEquals(fromMs(1000), list[3].time)

        assertEquals(true, list[0].blockTask, list.joinToString(","))
        assertEquals(false, list[1].blockTask, list.joinToString(","))
        assertEquals(true, list[2].blockTask, list.joinToString(","))
        assertEquals(false, list[3].blockTask, list.joinToString(","))
    }

    @Test
    fun queue() {
        val id1 = Id.create()
        val id2 = Id.create()
        val list = mutableListOf(
            WorkTaskImpl(fromMs(0), id2, false),
            WorkTaskImpl(fromMs(100), id2, true),
            WorkTaskImpl(fromMs(0), id1, false),
            WorkTaskImpl(fromMs(50), id1, true),
        )
        val queue = PriorityQueue<WorkTask>()
        list.stream().forEach(queue::add)
        assertEquals(fromMs(0), queue.poll().time)
        assertEquals(fromMs(0), queue.poll().time)
        assertEquals(fromMs(50), queue.poll().time)
        assertEquals(fromMs(100), queue.poll().time)
    }

}