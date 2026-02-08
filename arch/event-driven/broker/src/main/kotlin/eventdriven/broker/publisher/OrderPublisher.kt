package eventdriven.broker.publisher

import eventdriven.broker.bus.EventBus
import eventdriven.broker.event.OrderPlaced
import java.math.BigDecimal

class OrderPublisher(private val eventBus: EventBus) {
    fun placeOrder(orderId: String, customerId: String, items: List<String>, total: BigDecimal) {
        val event = OrderPlaced(
            orderId = orderId,
            customerId = customerId,
            items = items,
            totalAmount = total,
        )
        println("[Publisher] OrderPlaced を発行: $orderId")
        eventBus.publish(event)
    }
}
