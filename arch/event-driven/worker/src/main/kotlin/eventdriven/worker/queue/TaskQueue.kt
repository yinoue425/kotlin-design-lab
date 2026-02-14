package eventdriven.worker.queue

import eventdriven.worker.task.Task

interface TaskQueue<T : Task> {
    suspend fun submit(task: T)
    suspend fun take(): T
    fun shutdown()
}
