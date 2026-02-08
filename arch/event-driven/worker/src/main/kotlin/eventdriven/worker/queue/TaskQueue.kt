package eventdriven.worker.queue

import eventdriven.worker.task.Task

interface TaskQueue<T : Task> {
    fun submit(task: T)
    fun take(): T
    fun shutdown()
}
