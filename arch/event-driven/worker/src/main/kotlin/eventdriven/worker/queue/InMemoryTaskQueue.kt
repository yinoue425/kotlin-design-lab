package eventdriven.worker.queue

import eventdriven.worker.task.Task
import kotlinx.coroutines.channels.Channel

class InMemoryTaskQueue<T : Task>(capacity: Int = Channel.UNLIMITED) : TaskQueue<T> {
    private val channel = Channel<T>(capacity)

    override suspend fun submit(task: T) {
        channel.send(task)
    }

    override suspend fun take(): T = channel.receive()

    override fun shutdown() {
        channel.close()
    }
}
