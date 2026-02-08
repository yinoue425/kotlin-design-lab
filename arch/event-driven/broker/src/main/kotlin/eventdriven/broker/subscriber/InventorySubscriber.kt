package eventdriven.broker.subscriber

import eventdriven.broker.bus.EventBus
import eventdriven.broker.event.OrderCancelled
import eventdriven.broker.event.OrderPlaced

class InventorySubscriber(eventBus: EventBus) {
    init {
        eventBus.subscribe(OrderPlaced::class) { event ->
            println("[Inventory] 在庫を確保: 注文=${event.orderId}, 商品=${event.items}")
        }
        eventBus.subscribe(OrderCancelled::class) { event ->
            println("[Inventory] 在庫を解放: 注文=${event.orderId}")
        }
    }
}
