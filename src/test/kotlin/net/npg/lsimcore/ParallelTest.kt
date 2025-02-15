package net.npg.lsimcore

import net.npg.lsimcore.base.Id
import net.npg.lsimcore.base.SimpleRequestExecutor
import net.npg.lsimcore.base.ThreadRequestExecutor
import net.npg.lsimcore.time.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.ForkJoinPool
import java.util.stream.LongStream
import kotlin.random.Random
import kotlin.test.assertEquals

class ParallelTest {
    private val zeroClock: Time = fromMs(0)

    @Test
    fun requestAdvanceSimple() {
        val wallClock = initWallClock()
        val queue = WorkQueue(wallClock, SimpleRequestExecutor())
        val worker = getWorker(queue, 100)
        queue.stopTime = fromMs(1000)
        queue.start()
        queue.wait4End()
        assertEquals(11, worker.invoked)
    }

    @Test
    fun requestAdvance() {
        val wallClock = initWallClock()
        val queue = WorkQueue(wallClock, ThreadRequestExecutor(ForkJoinPool.commonPool()))
        val worker = getWorker(queue, 100)
        queue.stopTime = fromMs(1000)
        queue.start()
        queue.wait4End()
        assertEquals(11, worker.invoked)
    }

    @Test
    fun requestAdvanceMultiple() {
        val wallClock = initWallClock()
        val queue = WorkQueue(wallClock, ThreadRequestExecutor(ForkJoinPool.commonPool()))
        val worker50 = getWorker(queue, 50)
        val worker100 = getWorker(queue, 100)
        val worker120 = getWorker(queue, 120)
        queue.stopTime = fromMs(10000)
        queue.start()
        queue.wait4End()
        assertEquals(201, worker50.invoked)
        assertEquals(101, worker100.invoked)
        assertEquals(84, worker120.invoked)
    }

    @Test
    fun requestAdvanceRandom() {
        val wallClock = initWallClock()
        val queue = WorkQueue(wallClock, ThreadRequestExecutor(ForkJoinPool.commonPool()))
        val random = Random(System.currentTimeMillis())
        val workers = LongStream.range(0, 1000)
            .map { p -> random.nextLong() % 50L + 100L }
            .mapToObj { r -> getWorker(queue, r) }
            .toList()

        val maxTime = 10000L
        queue.stopTime = fromMs(maxTime)
        queue.start()
        queue.wait4End()
        workers.forEach {
            assertEquals(maxTime / it.lookahead.timeSpan + 1L, it.invoked)
        }
    }


    fun getWorker(queue: WorkQueue, lookahead: Long): FastWorker {
        val worker = FastWorker(lookahead, queue)
        queue.registerWorker(worker)
        return worker
    }

    fun initWallClock(): WallClock {
        return createWallClock(zeroClock)
    }
}

class FastWorker(val _lookahead: Long, val queue: WorkQueue) : Worker {
    override val id = Id.create()

    @Volatile
    var invoked: Long = 0
    var semaphore = java.util.concurrent.Semaphore(0)
    override fun advanceGranted(time: Time) {
        Assertions.assertThat(time).isGreaterThanOrEqualTo(localTime)
        localTime = time
        invoked++
        semaphore.release()
        queue.requestAdvance(this, time + lookahead)
    }

    var localTime: Time = fromMs(0)

    override val lookahead: TimeSpan
        get() = timeSpanFromMs(_lookahead)
}