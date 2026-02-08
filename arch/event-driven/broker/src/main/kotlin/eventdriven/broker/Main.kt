package eventdriven.broker

import eventdriven.broker.bus.SimpleEventBus
import eventdriven.broker.event.OrderCancelled
import eventdriven.broker.publisher.OrderPublisher
import eventdriven.broker.subscriber.AnalyticsSubscriber
import eventdriven.broker.subscriber.InventorySubscriber
import eventdriven.broker.subscriber.NotificationSubscriber

fun main() {
    val bus = SimpleEventBus()

    // 購読者を登録（順序不問、互いに独立）
    InventorySubscriber(bus)
    NotificationSubscriber(bus)
    AnalyticsSubscriber(bus)

    // 注文を発行
    val publisher = OrderPublisher(bus)
    publisher.placeOrder("ORD-001", "CUST-42", listOf("Widget", "Gadget"), 99.99.toBigDecimal())

    println()

    // キャンセルイベントを発行
    println("[Publisher] OrderCancelled を発行: ORD-001")
    bus.publish(OrderCancelled(orderId = "ORD-001", reason = "顧客都合"))
}
