# メディエータパターン

## 概念

メディエータが **ワークフロー全体を制御** する。各コンポーネントはメディエータからの指示で処理を実行し、結果をメディエータに返す。処理の順序・分岐判断はすべてメディエータが担う。

```
                    ┌─ 1. checkInventory ──> InventoryComponent
                    │        ↓ (結果)
OrderSubmitted ──> [Mediator] ─ 2. processPayment ──> PaymentComponent
                    │        ↓ (結果)
                    └─ 3. arrangeShipping ──> ShippingComponent
```

## ディレクトリ構成

```
mediator/src/main/kotlin/eventdriven/mediator/
├── event/
│   ├── Event.kt              # 基底インターフェース
│   └── OrderEvent.kt         # OrderSubmitted, InventoryChecked,
│                              # PaymentProcessed, ShippingArranged
├── mediator/
│   ├── EventMediator.kt              # registerComponent / processEvent
│   └── OrderProcessingMediator.kt    # ワークフロー制御
├── component/
│   ├── Component.kt              # 基底インターフェース
│   ├── InventoryComponent.kt     # 在庫確認
│   ├── PaymentComponent.kt       # 決済処理
│   └── ShippingComponent.kt      # 配送手配
└── Main.kt                       # デモ実行
```

## 核となるコード

### OrderProcessingMediator（ワークフロー制御）

```kotlin
private fun handleOrderSubmitted(order: OrderSubmitted) {
    println("[Mediator] 注文を処理開始: ${order.orderId}")

    // Step 1: 在庫確認
    val inventoryResult = inventory.checkInventory(order.orderId, order.items)
    if (!inventoryResult.available) {
        println("[Mediator] 注文 ${order.orderId} 失敗: 在庫切れ")
        return  // ← 後続ステップをスキップ
    }

    // Step 2: 決済処理（在庫ありの場合のみ）
    val paymentResult = payment.processPayment(order.orderId, order.totalAmount)
    if (!paymentResult.success) {
        println("[Mediator] 注文 ${order.orderId} 失敗: 決済エラー")
        return  // ← 配送をスキップ
    }

    // Step 3: 配送手配（決済成功の場合のみ）
    val shippingResult = shipping.arrangeShipping(order.orderId)
    println("[Mediator] 注文 ${order.orderId} 完了: 追跡番号=${shippingResult.trackingNumber}")
}
```

- 各ステップの結果に基づいて **次のステップに進むかどうかを判断**
- ブローカーと違い、処理の流れがメディエータの中に明示的に記述されている

### コンポーネント（例: InventoryComponent）

```kotlin
class InventoryComponent : Component {
    override val name = "Inventory"

    fun checkInventory(orderId: String, items: List<String>): InventoryChecked {
        val available = items.none { it == "OutOfStockItem" }
        return InventoryChecked(orderId = orderId, available = available)
    }
}
```

- コンポーネントは自分の責務だけを実行し、結果イベントを返す
- 他のコンポーネントの存在を知らない（メディエータのみが全体を知る）

## 実行結果

```
[Mediator] 注文を処理開始: ORD-001
  [Inventory] 在庫を確認中: [Widget, Gadget]
  [Payment] 決済処理中: 金額=99.99, 注文=ORD-001
  [Shipping] 配送を手配中: 注文=ORD-001
[Mediator] 注文 ORD-001 完了: 追跡番号=TRACK-ORD-001

[Mediator] 注文を処理開始: ORD-002
  [Inventory] 在庫を確認中: [OutOfStockItem]
[Mediator] 注文 ORD-002 失敗: 在庫切れ        ← 決済・配送はスキップ

[Mediator] 注文を処理開始: ORD-003
  [Inventory] 在庫を確認中: [ExpensiveItem]
  [Payment] 決済処理中: 金額=15000, 注文=ORD-003
[Mediator] 注文 ORD-003 失敗: 決済エラー      ← 配送はスキップ
```

## ポイント

1. **順序制御**: 在庫確認 → 決済 → 配送 の順序がメディエータにより保証される
2. **条件分岐**: 前ステップの結果に応じて後続を実行するかメディエータが判断（短絡評価）
3. **ワークフローの可視性**: `handleOrderSubmitted` を読めば処理フロー全体が把握できる
4. **変更の集中**: 新ステップ（例: 不正検知）の追加はメディエータの変更が必要
5. **テスト容易性**: 各コンポーネントの `processedOrders` リストで実行有無を検証可能

## ブローカーとの決定的な違い

ブローカーでは `OrderPlaced` イベントに対して3つの購読者が **独立に・順序不定で** 反応した。メディエータでは `OrderSubmitted` に対して **在庫→決済→配送の順で、前ステップの成否を見て** 次に進む。

この違いが、それぞれのパターンの適用場面を決める:
- 独立した副作用の並列実行 → **ブローカー**
- 順序と条件分岐のあるワークフロー → **メディエータ**
