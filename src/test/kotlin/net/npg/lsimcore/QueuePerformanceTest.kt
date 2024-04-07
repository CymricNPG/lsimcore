package net.npg.lsimcore

import net.npg.lsimcore.base.Id
import net.npg.lsimcore.time.Time
import net.npg.lsimcore.time.WorkTaskImpl
import net.npg.lsimcore.time.fromMs
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.random.Random

@Deprecated(message = "to remove just a playground")
class QueuePerformanceTest {
    @Test
    fun testQueue() {
        val random = Random(1243234)
        val size = 100
        val values = ArrayList<MutableWorkTaskImpl>(size)
        for (i in 0 until size) {
            values.add(
                MutableWorkTaskImpl(
                    fromMs(i + 100 + random.nextLong() % 50),
                    Id.create(),
                    random.nextLong() % 50 > 25
                )
            )
        }

        //    val queue = TreeSet<MutableWorkTaskImpl>(MutableWorkTaskImpl::compareTo)
        val queue = ConcurrentSkipListSet(MutableWorkTaskImpl::compareTo)

        values.forEach { queue.add(it) }
        for (run in 0..5) {
            val start = System.currentTimeMillis()
            val iterations = 1000000
            for (i in 0..iterations) {
                val index = i % (size - 1)
                val remove = values[index]
                queue.removeIf {
                    it.workerId == remove.workerId && it.blockTask == remove.blockTask
                }
                val minElement = queue.first()
                assert(minElement != null)
//                assert(minElement.isPresent)
                val newValue = MutableWorkTaskImpl(
                    fromMs(i + 100 + size + random.nextLong() % 50),
                    Id.create(),
                    random.nextInt() % 50 > 25
                )
                values[index] = newValue
                queue.add(newValue)
            }
            val end = System.currentTimeMillis()
            val time = end - start
            println("$time ms")
        }

    }


}

class MutableWorkTaskImpl(
    override var time: Time,
    override var workerId: Id,
    override var blockTask: Boolean
) : WorkTaskImpl(time, workerId, blockTask)