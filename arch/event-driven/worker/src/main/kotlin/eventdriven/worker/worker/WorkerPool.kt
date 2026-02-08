package eventdriven.worker.worker

import eventdriven.worker.queue.TaskQueue
import eventdriven.worker.task.Task

class WorkerPool<T : Task>(
    private val queue: TaskQueue<T>,
    private val workers: List<Worker<T>>,
) {
    private val threads = mutableListOf<Thread>()

    fun start() {
        for (worker in workers) {
            val thread = Thread({
                try {
                    while (!Thread.currentThread().isInterrupted) {
                        val task = queue.take()
                        worker.process(task)
                    }
                } catch (_: InterruptedException) {
                    // シャットダウン時の正常終了
                }
            }, worker.name)
            thread.start()
            threads.add(thread)
        }
    }

    fun shutdown() {
        threads.forEach { it.interrupt() }
        threads.forEach { it.join() }
        queue.shutdown()
    }
}
