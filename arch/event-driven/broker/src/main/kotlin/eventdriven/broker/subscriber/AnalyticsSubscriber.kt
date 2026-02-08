package eventdriven.broker.subscriber

import eventdriven.broker.bus.EventBus
import eventdriven.broker.event.OrderPlaced

class AnalyticsSubscriber(eventBus: EventBus) {
    init {
        eventBus.subscribe(OrderPlaced::class) { event ->
            println("[Analytics] 売上を記録: 金額=${event.totalAmount}")
        }
    }
}
