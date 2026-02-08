package eventdriven.mediator

import eventdriven.mediator.component.InventoryComponent
import eventdriven.mediator.component.PaymentComponent
import eventdriven.mediator.component.ShippingComponent
import eventdriven.mediator.event.OrderSubmitted
import eventdriven.mediator.mediator.OrderProcessingMediator

fun main() {
    val mediator = OrderProcessingMediator()

    // コンポーネントをメディエータに登録
    mediator.registerComponent(InventoryComponent())
    mediator.registerComponent(PaymentComponent())
    mediator.registerComponent(ShippingComponent())

    // 成功ケース: 通常の注文
    mediator.processEvent(
        OrderSubmitted(
            orderId = "ORD-001",
            customerId = "CUST-42",
            items = listOf("Widget", "Gadget"),
            totalAmount = 99.99.toBigDecimal(),
        )
    )

    println()

    // 失敗ケース: 在庫切れ
    mediator.processEvent(
        OrderSubmitted(
            orderId = "ORD-002",
            customerId = "CUST-99",
            items = listOf("OutOfStockItem"),
            totalAmount = 50.00.toBigDecimal(),
        )
    )

    println()

    // 失敗ケース: 高額注文（決済エラー）
    mediator.processEvent(
        OrderSubmitted(
            orderId = "ORD-003",
            customerId = "CUST-77",
            items = listOf("ExpensiveItem"),
            totalAmount = 15_000.toBigDecimal(),
        )
    )
}
