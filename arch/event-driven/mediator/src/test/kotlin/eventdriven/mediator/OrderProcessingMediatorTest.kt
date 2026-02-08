package eventdriven.mediator

import eventdriven.mediator.component.InventoryComponent
import eventdriven.mediator.component.PaymentComponent
import eventdriven.mediator.component.ShippingComponent
import eventdriven.mediator.event.OrderSubmitted
import eventdriven.mediator.mediator.OrderProcessingMediator
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderProcessingMediatorTest {

    private fun createMediator(): Triple<OrderProcessingMediator, InventoryComponent, Triple<PaymentComponent, ShippingComponent, Unit>> {
        val mediator = OrderProcessingMediator()
        val inventory = InventoryComponent()
        val payment = PaymentComponent()
        val shipping = ShippingComponent()
        mediator.registerComponent(inventory)
        mediator.registerComponent(payment)
        mediator.registerComponent(shipping)
        return Triple(mediator, inventory, Triple(payment, shipping, Unit))
    }

    @Test
    fun `successful order completes all three steps`() {
        val (mediator, inventory, rest) = createMediator()
        val (payment, shipping) = rest

        mediator.processEvent(
            OrderSubmitted(orderId = "ORD-1", customerId = "C-1", items = listOf("Widget"), totalAmount = 100.toBigDecimal())
        )

        assertEquals(listOf("ORD-1"), inventory.checkedOrders)
        assertEquals(listOf("ORD-1"), payment.processedOrders)
        assertEquals(listOf("ORD-1"), shipping.shippedOrders)
    }

    @Test
    fun `out of stock order stops before payment`() {
        val (mediator, inventory, rest) = createMediator()
        val (payment, shipping) = rest

        mediator.processEvent(
            OrderSubmitted(orderId = "ORD-2", customerId = "C-2", items = listOf("OutOfStockItem"), totalAmount = 50.toBigDecimal())
        )

        assertEquals(listOf("ORD-2"), inventory.checkedOrders)
        assertEquals(emptyList(), payment.processedOrders)
        assertEquals(emptyList(), shipping.shippedOrders)
    }

    @Test
    fun `payment failure stops before shipping`() {
        val (mediator, inventory, rest) = createMediator()
        val (payment, shipping) = rest

        mediator.processEvent(
            OrderSubmitted(orderId = "ORD-3", customerId = "C-3", items = listOf("ExpensiveItem"), totalAmount = 15_000.toBigDecimal())
        )

        assertEquals(listOf("ORD-3"), inventory.checkedOrders)
        assertEquals(listOf("ORD-3"), payment.processedOrders)
        assertEquals(emptyList(), shipping.shippedOrders)
    }
}
