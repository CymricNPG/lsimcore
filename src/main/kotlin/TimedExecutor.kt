import java.util.*

class TimedExecutor {
    private val futureTaskQueue = PriorityQueue<Task>()
    fun advanceTo(nextTime: Time): Time {
        val topTask = futureTaskQueue.peek()
        if (topTask.executionTime <= nextTime) {
            futureTaskQueue.poll()
        }
        TODO()
    }

}