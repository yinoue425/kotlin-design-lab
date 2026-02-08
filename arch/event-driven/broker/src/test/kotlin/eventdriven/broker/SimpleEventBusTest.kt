package eventdriven.broker

import eventdriven.broker.bus.SimpleEventBus
import eventdriven.broker.event.OrderCancelled
import eventdriven.broker.event.OrderPlaced
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleEventBusTest {

    @Test
    fun `published event is delivered to subscriber`() {
        val bus = SimpleEventBus()
        val received = mutableListOf<String>()

        bus.subscribe(OrderPlaced::class) { event ->
            received.add(event.orderId)
        }

        bus.publish(OrderPlaced(orderId = "ORD-1", customerId = "C-1", items = listOf("A"), totalAmount = 10.toBigDecimal()))

        assertEquals(listOf("ORD-1"), received)
    }

    @Test
    fun `event is delivered to multiple subscribers`() {
        val bus = SimpleEventBus()
        val received1 = mutableListOf<String>()
        val received2 = mutableListOf<String>()

        bus.subscribe(OrderPlaced::class) { received1.add(it.orderId) }
        bus.subscribe(OrderPlaced::class) { received2.add(it.orderId) }

        bus.publish(OrderPlaced(orderId = "ORD-1", customerId = "C-1", items = listOf("A"), totalAmount = 10.toBigDecimal()))

        assertEquals(listOf("ORD-1"), received1)
        assertEquals(listOf("ORD-1"), received2)
    }

    @Test
    fun `subscriber only receives events of subscribed type`() {
        val bus = SimpleEventBus()
        val placedEvents = mutableListOf<String>()
        val cancelledEvents = mutableListOf<String>()

        bus.subscribe(OrderPlaced::class) { placedEvents.add(it.orderId) }
        bus.subscribe(OrderCancelled::class) { cancelledEvents.add(it.orderId) }

        bus.publish(OrderPlaced(orderId = "ORD-1", customerId = "C-1", items = listOf("A"), totalAmount = 10.toBigDecimal()))

        assertEquals(listOf("ORD-1"), placedEvents)
        assertEquals(emptyList(), cancelledEvents)
    }

    @Test
    fun `publishing with no subscribers does not throw`() {
        val bus = SimpleEventBus()
        bus.publish(OrderPlaced(orderId = "ORD-1", customerId = "C-1", items = listOf("A"), totalAmount = 10.toBigDecimal()))
    }
}
