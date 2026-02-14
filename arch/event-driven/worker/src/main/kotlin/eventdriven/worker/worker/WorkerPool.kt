package eventdriven.worker.worker

import eventdriven.worker.queue.TaskQueue
import eventdriven.worker.task.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

class WorkerPool<T : Task>(
    private val queue: TaskQueue<T>,
    private val workers: List<Worker<T>>,
) {
    private val jobs = mutableListOf<Job>()

    fun start(scope: CoroutineScope) {
        for (worker in workers) {
            val job = scope.launch {
                while (true) {
                    val task = queue.take()
                    worker.process(task)
                }
            }
            jobs.add(job)
        }
    }

    suspend fun shutdown() {
        jobs.forEach { it.cancelAndJoin() }
        queue.shutdown()
    }
}
