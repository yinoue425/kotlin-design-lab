package eventdriven.worker.queue

import eventdriven.worker.task.Task
import java.util.concurrent.LinkedBlockingQueue

class InMemoryTaskQueue<T : Task> : TaskQueue<T> {
    private val queue = LinkedBlockingQueue<T>()

    override fun submit(task: T) {
        queue.put(task)
    }

    override fun take(): T = queue.take()

    override fun shutdown() {
        queue.clear()
    }
}
