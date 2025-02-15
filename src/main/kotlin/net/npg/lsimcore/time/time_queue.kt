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
package net.npg.lsimcore.time

import net.npg.lsimcore.base.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.Volatile

/**
 * a TimeManagement implementation based on a priority queue
 * <img src="../../../images/timemanagement.png" />
 */
class WorkQueue(
    private val wallClock: WallClock,
    private val requestExecutor: RequestExecutor,
    private val logger: Logger = NOPLogger(),
    private val timeDefinition: TimeDefinition = msTIME
) : TimeManagement {

    private val queue: SortedQueue<WorkTask> = SortedQueueWrapper.createDefaultQueue()
    private val workers: MutableMap<Id, WorkerContext> = ConcurrentHashMap()

    @Volatile
    private var started: Boolean = false

    /**
     * time when to stop the simulation
     */
    var stopTime: Time = timeDefinition.maxTime

    /**
     * endLatch is reached if stopTime is reached
     */
    private val endLatch = CountDownLatch(1)

    override fun removeWorker(worker: Worker) {
        synchronized(queue) {
            workers.remove(worker.id) ?: return
            queue.removeIf { wt -> wt.workerId == worker.id }
        }
    }

    override fun getWallClock(): Time {
        return wallClock.time
    }

    override fun registerWorker(worker: Worker) {
        synchronized(queue) {
            require(wallClock.time.isZero())
            require(!workers.containsKey(worker.id))

            val id = worker.id
            val workerContext = WorkerContext(worker, timeDefinition)
            workers[id] = workerContext
            addAdvanceTask(timeDefinition.zeroTime, workerContext)
            addBlockTask(timeDefinition.zeroTime + worker.lookahead, workerContext)
        }
    }

    fun start() {
        synchronized(queue) {
            check(!started) { "Time management is already running!" }
            started = true
            advanceAllWaitingTasks()
        }
    }

    override fun requestAdvance(worker: Worker, nextTime: Time) {
        checkAdvanceRequest(worker, nextTime)
        requestExecutor.execute {
            synchronized(queue) {
                try {
                    requestAdvanceAsync(worker, nextTime)
                } catch (e: Exception) {
                    handleError(worker, e)
                }
            }
        }
    }

    private fun checkAdvanceRequest(worker: Worker, nextTime: Time) {
        synchronized(queue) {
            require(nextTime >= wallClock.time)
            val workerContext = workers[worker.id]
            requireNotNull(workerContext)
            require(nextTime >= workerContext.nextTime)
        }
    }

    private fun handleError(worker: Worker, e: Exception) {
        val workerContext = workers[worker.id]
        if (workerContext == null) {
            logger.log(LogLevel.WARN, "Worker ${worker.id} no longer exists, but an error happened for this worker", e)
        } else {
            logger.log(
                LogLevel.WARN,
                "While handling worker ${worker.id} an error happened, the worker will be removed",
                e
            )
            removeWorker(worker)
        }
    }

    fun wait4End() {
        endLatch.await()
    }

    /**
     * not async, but should be executed in own thread
     */
    private fun requestAdvanceAsync(worker: Worker, nextTime: Time) {
        checkAdvanceRequest(worker, nextTime)
        val workerContext = workers[worker.id]!!
        removeBlockTask(workerContext.id)
        if (checkForEnd(nextTime)) {
            return
        }
        addAdvanceTask(nextTime, workerContext)
        addBlockTask(nextTime + workerContext.lookahead, workerContext)
        advanceAllWaitingTasks()
    }

    // All Private methods must be executed in a synchronized environment

    private fun checkForEnd(nextTime: Time): Boolean {
        if (nextTime > stopTime) {
            if (queue.isEmpty()) {
                endLatch.countDown()
            }
            return true
        }
        return false
    }

    private fun removeBlockTask(workerId: Id) {
        queue.removeIf { it.workerId == workerId && it.taskType == TaskType.BLOCKING }
    }

    private fun advanceAllWaitingTasks() {
        while (queue.peek()?.taskType == TaskType.ADVANCE_GRANTED) {
            val nextTask = queue.poll()
            advanceTime(nextTask.time)
            val worker = workers[nextTask.workerId]
            if (worker != null) {
                advanceGranted(worker, nextTask.time)
            }
        }
    }

    private fun addBlockTask(nextTime: Time, worker: WorkerContext) {
        queue.add(WorkTaskImpl(nextTime, worker.id, TaskType.BLOCKING))
        worker.nextTime = nextTime
    }

    private fun addAdvanceTask(nextTime: Time, worker: WorkerContext) {
        queue.add(WorkTaskImpl(nextTime, worker.id, TaskType.ADVANCE_GRANTED))
    }

    private fun advanceGranted(worker: WorkerContext, time: Time) {
        worker.currentTime = time
        requestExecutor.execute { worker.advanceGranted(time) }
    }

    private fun advanceTime(time: Time) {
        wallClock.advanceTo(time)
    }
}

/**
 * a WorkTask can either be :
 * - blocking : The Worker hasn't finished, the time management is waiting for an advance time request
 * - non-blocking: The worker waits for execution
 */
interface WorkTask : Comparable<WorkTask> {
    val time: Time
    val workerId: Id
    val taskType: TaskType
}

enum class TaskType {
    EXTERNAL_ADVANCE,
    BLOCKING,
    ADVANCE_GRANTED,
}

open class WorkTaskImpl(
    override val time: Time,
    override val workerId: Id,
    override val taskType: TaskType
) : WorkTask {
    override fun compareTo(other: WorkTask): Int {
        // The magic, sort by time, blockTask and finally by id
        // blockTask == true has always priority, Workers can only be executed after all blockTasks have been removed!
        return if (time.compareTo(other.time) == 0) {
            if (taskType == other.taskType) {
                workerId.compareTo(other.workerId)
            } else {
                taskType.compareTo(other.taskType)
            }
        } else {
            time.compareTo(other.time)
        }
    }

    override fun toString(): String {
        return "WorkTask(time=$time, workerId=$workerId, blockTask=$taskType)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorkTask

        if (time != other.time) return false
        if (workerId != other.workerId) return false
        if (taskType != other.taskType) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(time, workerId, taskType)
    }

}

/**
 * each worker gets a context which contains the current state and context of the worker
 */
class WorkerContext(
    var lookahead: TimeSpan,
    val id: Id,
    val advanceGranted: (Time) -> Unit,
    var currentTime: Time,
    var nextTime: Time
) {
    constructor(copyFrom: Worker, timeDefinition: TimeDefinition) : this(
        copyFrom.lookahead,
        copyFrom.id,
        copyFrom::advanceGranted,
        timeDefinition.zeroTime,
        timeDefinition.zeroTime
    )
}




