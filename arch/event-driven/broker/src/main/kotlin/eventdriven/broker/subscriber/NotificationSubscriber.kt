package eventdriven.broker.subscriber

import eventdriven.broker.bus.EventBus
import eventdriven.broker.event.OrderCancelled
import eventdriven.broker.event.OrderPlaced

class NotificationSubscriber(eventBus: EventBus) {
    init {
        eventBus.subscribe(OrderPlaced::class) { event ->
            println("[Notification] 注文確認メールを送信: 顧客=${event.customerId}")
        }
        eventBus.subscribe(OrderCancelled::class) { event ->
            println("[Notification] キャンセル通知を送信: 注文=${event.orderId}, 理由=${event.reason}")
        }
    }
}
