package eventdriven.broker

import eventdriven.broker.bus.KafkaEventBus
import eventdriven.broker.event.OrderCancelled
import eventdriven.broker.publisher.OrderPublisher
import eventdriven.broker.subscriber.AnalyticsSubscriber
import eventdriven.broker.subscriber.InventorySubscriber
import eventdriven.broker.subscriber.NotificationSubscriber

/**
 * Kafka版ブローカーデモ。
 *
 * 事前準備: docker compose up -d
 * 実行: ./gradlew :broker:run -PmainClass=eventdriven.broker.KafkaMainKt
 */
fun main() {
    val bus = KafkaEventBus()

    // SimpleEventBus版と全く同じコードで購読者を登録
    InventorySubscriber(bus)
    NotificationSubscriber(bus)
    AnalyticsSubscriber(bus)

    // コンシューマのパーティション割り当て完了を待つ
    // （Consumer Groupのリバランスに数秒かかる）
    Thread.sleep(5000)

    // 注文を発行（Kafkaトピックに送信される）
    val publisher = OrderPublisher(bus)
    publisher.placeOrder("ORD-001", "CUST-42", listOf("Widget", "Gadget"), 99.99.toBigDecimal())

    println()

    // キャンセルイベントを発行
    println("[Publisher] OrderCancelled を発行: ORD-001")
    bus.publish(OrderCancelled(orderId = "ORD-001", reason = "顧客都合"))

    // コンシューマがメッセージを受信するのを待つ
    Thread.sleep(3000)

    bus.close()
}
