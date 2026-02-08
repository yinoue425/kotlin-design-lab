package eventdriven.worker.worker

import eventdriven.worker.task.Task

interface Worker<T : Task> {
    val name: String
    fun process(task: T)
}
