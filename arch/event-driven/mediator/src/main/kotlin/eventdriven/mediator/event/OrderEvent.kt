package eventdriven.mediator.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderSubmitted(
    val orderId: String,
    val customerId: String,
    val items: List<String>,
    val totalAmount: BigDecimal,
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: Instant = Instant.now(),
) : Event

data class InventoryChecked(
    val orderId: String,
    val available: Boolean,
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: Instant = Instant.now(),
) : Event

data class PaymentProcessed(
    val orderId: String,
    val success: Boolean,
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: Instant = Instant.now(),
) : Event

data class ShippingArranged(
    val orderId: String,
    val trackingNumber: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: Instant = Instant.now(),
) : Event
