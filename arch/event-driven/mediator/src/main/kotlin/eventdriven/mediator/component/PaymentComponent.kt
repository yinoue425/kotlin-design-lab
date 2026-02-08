package eventdriven.mediator.component

import eventdriven.mediator.event.PaymentProcessed
import java.math.BigDecimal

class PaymentComponent : Component {
    override val name = "Payment"

    val processedOrders = mutableListOf<String>()

    fun processPayment(orderId: String, amount: BigDecimal): PaymentProcessed {
        println("  [Payment] 決済処理中: 金額=$amount, 注文=$orderId")
        processedOrders.add(orderId)
        val success = amount < BigDecimal(10_000)
        return PaymentProcessed(orderId = orderId, success = success)
    }
}
