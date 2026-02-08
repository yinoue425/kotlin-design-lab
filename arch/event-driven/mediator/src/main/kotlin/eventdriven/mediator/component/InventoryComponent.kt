package eventdriven.mediator.component

import eventdriven.mediator.event.InventoryChecked

class InventoryComponent : Component {
    override val name = "Inventory"

    val checkedOrders = mutableListOf<String>()

    fun checkInventory(orderId: String, items: List<String>): InventoryChecked {
        println("  [Inventory] 在庫を確認中: $items")
        checkedOrders.add(orderId)
        val available = items.none { it == "OutOfStockItem" }
        return InventoryChecked(orderId = orderId, available = available)
    }
}
