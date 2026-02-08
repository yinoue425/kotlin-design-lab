package eventdriven.broker.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

sealed interface OrderEvent : Event {
    val orderId: String
}

data class OrderPlaced(
    override val orderId: String,
    val customerId: String,
    val items: List<String>,
    val totalAmount: BigDecimal,
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: Instant = Instant.now(),
) : OrderEvent

data class OrderCancelled(
    override val orderId: String,
    val reason: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: Instant = Instant.now(),
) : OrderEvent
