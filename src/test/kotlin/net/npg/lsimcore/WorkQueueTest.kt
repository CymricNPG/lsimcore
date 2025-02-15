package net.npg.lsimcore

import net.npg.lsimcore.base.Id
import net.npg.lsimcore.base.SimpleRequestExecutor
import net.npg.lsimcore.base.StdoutLogger
import net.npg.lsimcore.time.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit

internal class WorkQueueTest {

    private val zeroClock: Time = msTIME.zeroTime

    private val nextTime50 = fromMs(50)
    private val nextTime100 = fromMs(100)
    private val nextTime150 = fromMs(150)
    private val nextTime200 = fromMs(200)

    @Test
    fun requestAdvance() {

        val wallClock = initWallClock()
        val queue = getQueue(wallClock)
        val worker = getWorker(queue, 100)

        assertEquals(0, worker.invoked)

        assertEquals(zeroClock, wallClock.time)
        assertEquals(zeroClock, worker.localTime)

        queue.start()

        assertEquals(fromMs(0), wallClock.time)
        assertEquals(zeroClock, worker.localTime)
        assertEquals(1, worker.invoked)

        queue.requestAdvance(worker, nextTime100)
        assertEquals(worker.localTime, nextTime100)
        assertEquals(nextTime100, wallClock.time)
        assertEquals(2, worker.invoked)
    }

    @Test
    fun requestAdvance2() {
        val wallClock = initWallClock()
        val queue = getQueue(wallClock)

        val worker100 = getWorker(queue, 100)
        val worker50 = getWorker(queue, 50)

        queue.start()

        queue.requestAdvance(worker50, nextTime50)
        assertEquals(nextTime50, wallClock.time)
        queue.requestAdvance(worker100, nextTime100)
        assertEquals(nextTime50, wallClock.time)
        queue.requestAdvance(worker50, nextTime100)
        assertEquals(nextTime100, wallClock.time)
        assertEquals(2, worker100.invoked)
        assertEquals(3, worker50.invoked)
    }

    @Test
    fun requestAdvanceWithRemove() {
        val wallClock = initWallClock()
        val queue = getQueue(wallClock)


        val worker100 = getWorker(queue, 100)
        val worker50 = getWorker(queue, 50)

        queue.start()

        queue.requestAdvance(worker50, nextTime50)
        queue.requestAdvance(worker50, nextTime100)
        queue.requestAdvance(worker100, nextTime100)
        queue.removeWorker(worker50)
        assertThrows<IllegalArgumentException> {
            queue.requestAdvance(worker50, nextTime150)
        }
        queue.requestAdvance(worker100, nextTime200)
        assertEquals(nextTime200, wallClock.time)
        assertEquals(3, worker100.invoked)
        assertEquals(3, worker50.invoked)
    }

    @Test
    fun requestAdvance3() {
        val wallClock = initWallClock()
        val queue = getQueue(wallClock)


        val worker100 = getWorker(queue, 100)
        val worker50 = getWorker(queue, 50)

        queue.start()

        queue.requestAdvance(worker50, nextTime50)
        queue.requestAdvance(worker50, nextTime100)
        queue.requestAdvance(worker100, nextTime100)
        queue.requestAdvance(worker50, nextTime150)

        assertEquals(nextTime150, wallClock.time)
        assertEquals(2, worker100.invoked)
        assertEquals(4, worker50.invoked)
    }


    private fun initWallClock(): WallClock {
        return createWallClock(zeroClock)
    }

    private fun getWorker(queue: WorkQueue, lookahead: Long): WorkerImpl {
        val worker = WorkerImpl(lookahead)
        queue.registerWorker(worker)
        return worker
    }

    private fun advance(queue: WorkQueue, worker: WorkerImpl, time: Long) {
        queue.requestAdvance(worker, fromMs(time))
    }

    fun advanceBlocking(queue: WorkQueue, worker: Worker, time: Long) {
        queue.requestAdvance(worker, fromMs(time))

    }

    private fun wait(worker: WorkerImpl) {
        if (!worker.semaphore.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            throw RuntimeException("Timeout")
        }
    }

    @Test
    fun requestAdvanceSame1() {
        val wallClock = initWallClock()
        val queue = getQueue(wallClock)

        val worker1 = getWorker(queue, 100)
        val worker2 = getWorker(queue, 100)

        advance(queue, worker1, 100)
        advance(queue, worker2, 100)

        wait(worker1)
        wait(worker2)

        assertEquals(2, worker1.invoked)
        assertEquals(2, worker2.invoked)
    }

    @Test
    fun requestAdvanceSame2() {
        val wallClock = initWallClock()
        val queue = getQueue(wallClock)

        val worker1 = getWorker(queue, 100)
        val worker2 = getWorker(queue, 100)

        queue.start()
        assertEquals(0, wallClock.time.value)
        advance(queue, worker1, 100)
        advance(queue, worker2, 100)

        wait(worker1)
        wait(worker2)

        assertEquals(2, worker1.invoked)
        assertEquals(2, worker2.invoked)
        advance(queue, worker1, 200)
        advance(queue, worker2, 200)

        wait(worker2)
        wait(worker1)

        assertEquals(3, worker1.invoked)
        assertEquals(3, worker2.invoked)
    }

    private fun getQueue(wallClock: WallClock) = WorkQueue(wallClock, SimpleRequestExecutor(), StdoutLogger())

}

class WorkerImpl(val _lookahead: Long) : Worker {
    override val id = Id.create()

    @Volatile
    var invoked: Int = 0
    var semaphore = java.util.concurrent.Semaphore(0)
    override fun advanceGranted(time: Time) {
        Assertions.assertThat(time).isGreaterThanOrEqualTo(localTime)
        localTime = time
        println("Invoked Worker 1 at $time")
        invoked++
        semaphore.release()
    }

    var localTime: Time = fromMs(0)

    override val lookahead: TimeSpan
        get() = timeSpanFromMs(_lookahead)
}
