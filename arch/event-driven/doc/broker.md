# ブローカーパターン

## 概念

ブローカー（イベントバス）はイベントの **ルーティングだけ** を行う。発行者と購読者は互いの存在を知らず、完全に疎結合。ブローカー自体にビジネスロジックはない。

```
Publisher ──publish──> [EventBus] ──deliver──> Subscriber A
                                  ──deliver──> Subscriber B
                                  ──deliver──> Subscriber C
```

## ディレクトリ構成

```
broker/src/main/kotlin/eventdriven/broker/
├── event/
│   ├── Event.kt              # 基底インターフェース（eventId, timestamp）
│   └── OrderEvent.kt         # OrderPlaced, OrderCancelled
├── bus/
│   ├── EventBus.kt           # subscribe / publish インターフェース
│   └── SimpleEventBus.kt     # インメモリ実装
├── publisher/
│   └── OrderPublisher.kt     # イベント発行者
├── subscriber/
│   ├── InventorySubscriber.kt     # 在庫確保/解放
│   ├── NotificationSubscriber.kt  # メール通知
│   └── AnalyticsSubscriber.kt     # 売上記録
└── Main.kt                   # デモ実行
```

## 核となるコード

### EventBus インターフェース

```kotlin
interface EventBus {
    fun <T : Event> subscribe(eventType: KClass<T>, handler: (T) -> Unit)
    fun publish(event: Event)
}
```

- `subscribe`: イベント型を指定してハンドラを登録
- `publish`: イベントを全該当購読者へ配信

### SimpleEventBus 実装

```kotlin
class SimpleEventBus : EventBus {
    private val subscribers = ConcurrentHashMap<KClass<*>, MutableList<(Event) -> Unit>>()

    override fun <T : Event> subscribe(eventType: KClass<T>, handler: (T) -> Unit) {
        subscribers.getOrPut(eventType) { CopyOnWriteArrayList() }
            .add { event -> handler(event as T) }
    }

    override fun publish(event: Event) {
        subscribers[event::class]?.forEach { handler -> handler(event) }
    }
}
```

- `ConcurrentHashMap` + `CopyOnWriteArrayList` でスレッドセーフ
- イベントの実行時型（`event::class`）をキーにルーティング
- ビジネスロジックは **一切含まない**

### 購読者の登録と発行の流れ

```kotlin
fun main() {
    val bus = SimpleEventBus()

    // 購読者を登録（順序不問、互いに独立）
    InventorySubscriber(bus)
    NotificationSubscriber(bus)
    AnalyticsSubscriber(bus)

    // 注文を発行 → 3つの購読者が独立に反応
    val publisher = OrderPublisher(bus)
    publisher.placeOrder("ORD-001", "CUST-42", listOf("Widget", "Gadget"), 99.99.toBigDecimal())
}
```

## 実行結果

```
[Publisher] OrderPlaced を発行: ORD-001
[Inventory] 在庫を確保: 注文=ORD-001, 商品=[Widget, Gadget]
[Notification] 注文確認メールを送信: 顧客=CUST-42
[Analytics] 売上を記録: 金額=99.99

[Publisher] OrderCancelled を発行: ORD-001
[Inventory] 在庫を解放: 注文=ORD-001
[Notification] キャンセル通知を送信: 注文=ORD-001, 理由=顧客都合
```

## ポイント

1. **疎結合**: `OrderPublisher` は `InventorySubscriber` などの存在を知らない
2. **拡張が容易**: 新しい購読者（例: `LoyaltyPointsSubscriber`）を追加しても既存コード変更不要
3. **処理順序の保証なし**: 購読者の実行順序はバスの実装依存であり、業務的な順序制約には不向き
4. **エラーの独立性**: ある購読者の失敗が他の購読者に影響しない（本実装では例外が伝播するが、実運用ではtry-catchで隔離する）
