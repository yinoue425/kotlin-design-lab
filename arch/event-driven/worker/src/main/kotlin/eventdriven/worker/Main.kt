package eventdriven.worker

import eventdriven.worker.queue.InMemoryTaskQueue
import eventdriven.worker.task.OrderTask
import eventdriven.worker.worker.OrderWorker
import eventdriven.worker.worker.WorkerPool

fun main() {
    val queue = InMemoryTaskQueue<OrderTask>()

    // ワーカー3台を起動
    val workers = listOf(
        OrderWorker("Worker-1"),
        OrderWorker("Worker-2"),
        OrderWorker("Worker-3"),
    )
    val pool = WorkerPool(queue, workers)
    pool.start()

    // 注文5件をキューに投入
    val orders = listOf(
        OrderTask(orderId = "ORD-001", customerId = "CUST-42", items = listOf("Widget", "Gadget"), totalAmount = 99.99.toBigDecimal()),
        OrderTask(orderId = "ORD-002", customerId = "CUST-15", items = listOf("Sprocket"), totalAmount = 45.00.toBigDecimal()),
        OrderTask(orderId = "ORD-003", customerId = "CUST-88", items = listOf("Gizmo", "Doohickey"), totalAmount = 150.00.toBigDecimal()),
        OrderTask(orderId = "ORD-004", customerId = "CUST-33", items = listOf("Thingamajig"), totalAmount = 29.99.toBigDecimal()),
        OrderTask(orderId = "ORD-005", customerId = "CUST-77", items = listOf("Widget", "Sprocket", "Gizmo"), totalAmount = 249.99.toBigDecimal()),
    )

    println("=== ワーカーパターン: 3ワーカー × 5タスク ===")
    println()

    for (order in orders) {
        println("[Queue] 注文 ${order.orderId} をキューに投入")
        queue.submit(order)
    }

    // 全タスクの処理完了を待つ
    Thread.sleep(3_000)

    pool.shutdown()

    println()
    println("=== 処理結果 ===")
    for (worker in workers) {
        println("${worker.name}: ${worker.processedOrders}")
    }
}
