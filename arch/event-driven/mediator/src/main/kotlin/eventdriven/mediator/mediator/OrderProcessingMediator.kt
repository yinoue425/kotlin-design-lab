package eventdriven.mediator.mediator

import eventdriven.mediator.component.Component
import eventdriven.mediator.component.InventoryComponent
import eventdriven.mediator.component.PaymentComponent
import eventdriven.mediator.component.ShippingComponent
import eventdriven.mediator.event.Event
import eventdriven.mediator.event.OrderSubmitted

class OrderProcessingMediator : EventMediator {
    private lateinit var inventory: InventoryComponent
    private lateinit var payment: PaymentComponent
    private lateinit var shipping: ShippingComponent

    override fun registerComponent(component: Component) {
        when (component) {
            is InventoryComponent -> inventory = component
            is PaymentComponent -> payment = component
            is ShippingComponent -> shipping = component
        }
    }

    override fun processEvent(event: Event) {
        when (event) {
            is OrderSubmitted -> handleOrderSubmitted(event)
            else -> println("[Mediator] 未対応のイベント: ${event::class.simpleName}")
        }
    }

    private fun handleOrderSubmitted(order: OrderSubmitted) {
        println("[Mediator] 注文を処理開始: ${order.orderId}")

        // Step 1: 在庫確認
        val inventoryResult = inventory.checkInventory(order.orderId, order.items)
        if (!inventoryResult.available) {
            println("[Mediator] 注文 ${order.orderId} 失敗: 在庫切れ")
            return
        }

        // Step 2: 決済処理（在庫ありの場合のみ）
        val paymentResult = payment.processPayment(order.orderId, order.totalAmount)
        if (!paymentResult.success) {
            println("[Mediator] 注文 ${order.orderId} 失敗: 決済エラー")
            return
        }

        // Step 3: 配送手配（決済成功の場合のみ）
        val shippingResult = shipping.arrangeShipping(order.orderId)
        println("[Mediator] 注文 ${order.orderId} 完了: 追跡番号=${shippingResult.trackingNumber}")
    }
}
