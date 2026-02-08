package eventdriven.mediator.component

import eventdriven.mediator.event.ShippingArranged

class ShippingComponent : Component {
    override val name = "Shipping"

    val shippedOrders = mutableListOf<String>()

    fun arrangeShipping(orderId: String): ShippingArranged {
        println("  [Shipping] 配送を手配中: 注文=$orderId")
        shippedOrders.add(orderId)
        return ShippingArranged(orderId = orderId, trackingNumber = "TRACK-$orderId")
    }
}
