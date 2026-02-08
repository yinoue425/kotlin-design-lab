package eventdriven.worker.worker

import eventdriven.worker.task.OrderTask

class OrderWorker(override val name: String) : Worker<OrderTask> {

    val processedOrders = mutableListOf<String>()

    override fun process(task: OrderTask) {
        println("[$name] 注文 ${task.orderId} を処理中...")

        // Step 1: 在庫確認
        println("  [$name] 在庫を確認中: ${task.items}")
        Thread.sleep(100)

        // Step 2: 決済処理
        println("  [$name] 決済処理中: 金額=${task.totalAmount}")
        Thread.sleep(100)

        // Step 3: 配送手配
        println("  [$name] 配送を手配中: 注文=${task.orderId}")
        Thread.sleep(100)

        processedOrders.add(task.orderId)
        println("[$name] 注文 ${task.orderId} 完了")
    }
}
