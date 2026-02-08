package eventdriven.worker

import eventdriven.worker.queue.InMemoryTaskQueue
import eventdriven.worker.task.OrderTask
import eventdriven.worker.worker.OrderWorker
import eventdriven.worker.worker.WorkerPool
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkerPoolTest {

    @Test
    fun `all tasks are processed exactly once`() {
        val queue = InMemoryTaskQueue<OrderTask>()
        val workers = listOf(
            OrderWorker("Worker-1"),
            OrderWorker("Worker-2"),
            OrderWorker("Worker-3"),
        )
        val pool = WorkerPool(queue, workers)
        pool.start()

        val orderIds = (1..10).map { "ORD-$it" }
        for (id in orderIds) {
            queue.submit(
                OrderTask(orderId = id, customerId = "C-1", items = listOf("A"), totalAmount = 10.toBigDecimal())
            )
        }

        // 全タスク処理完了を待つ
        Thread.sleep(5_000)
        pool.shutdown()

        val allProcessed = workers.flatMap { it.processedOrders }.sorted()
        assertEquals(orderIds.sorted(), allProcessed, "全タスクが1回ずつ処理されること")
    }

    @Test
    fun `no task is processed by more than one worker`() {
        val queue = InMemoryTaskQueue<OrderTask>()
        val workers = listOf(
            OrderWorker("Worker-A"),
            OrderWorker("Worker-B"),
        )
        val pool = WorkerPool(queue, workers)
        pool.start()

        val orderIds = (1..6).map { "ORD-$it" }
        for (id in orderIds) {
            queue.submit(
                OrderTask(orderId = id, customerId = "C-1", items = listOf("X"), totalAmount = 5.toBigDecimal())
            )
        }

        Thread.sleep(3_000)
        pool.shutdown()

        // 各ワーカーの処理リストに重複がないことを確認
        val workerA = workers[0].processedOrders.toSet()
        val workerB = workers[1].processedOrders.toSet()
        assertTrue(workerA.intersect(workerB).isEmpty(), "同じタスクが複数ワーカーで処理されないこと")

        // 全タスクが処理されていること
        assertEquals(orderIds.toSet(), workerA + workerB)
    }

    @Test
    fun `pool shuts down gracefully when no tasks`() {
        val queue = InMemoryTaskQueue<OrderTask>()
        val workers = listOf(OrderWorker("Worker-1"))
        val pool = WorkerPool(queue, workers)
        pool.start()

        // タスクを投入せずにシャットダウン
        pool.shutdown()

        assertTrue(workers[0].processedOrders.isEmpty())
    }
}
