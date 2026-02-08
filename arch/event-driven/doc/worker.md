# ワーカーパターン（Competing Consumers）

## 概念

タスクキューに投入されたジョブを、**複数のワーカーが競合取得** して処理する。各タスクは **1つのワーカーだけ** が処理する（ブローカーのfan-outと対照的）。ワーカーは常駐スレッドとして **継続的にキューをポーリング** する。

```
                    ┌── take ──> Worker-1 ──> process
submit ──> [Queue] ─┼── take ──> Worker-2 ──> process
                    └── take ──> Worker-3 ──> process
```

## ディレクトリ構成

```
worker/src/main/kotlin/eventdriven/worker/
├── task/
│   ├── Task.kt                # タスク基底インターフェース
│   └── OrderTask.kt           # 注文処理タスク
├── queue/
│   ├── TaskQueue.kt           # キューインターフェース（submit / take）
│   └── InMemoryTaskQueue.kt   # LinkedBlockingQueueによるインメモリ実装
├── worker/
│   ├── Worker.kt              # ワーカーインターフェース
│   ├── OrderWorker.kt         # 注文処理ワーカー
│   └── WorkerPool.kt          # ワーカースレッド管理
└── Main.kt                    # デモ実行
```

## 核となるコード

### TaskQueue インターフェース

```kotlin
interface TaskQueue<T : Task> {
    fun submit(task: T)
    fun take(): T            // ブロッキングで1件取得（competing consumers）
    fun shutdown()
}
```

- `submit`: タスクをキューに投入
- `take`: ワーカーが1件取得（複数ワーカーが呼んでも **1つだけ** が取得）

### InMemoryTaskQueue 実装

```kotlin
class InMemoryTaskQueue<T : Task> : TaskQueue<T> {
    private val queue = LinkedBlockingQueue<T>()

    override fun submit(task: T) { queue.put(task) }
    override fun take(): T = queue.take()
    override fun shutdown() { queue.clear() }
}
```

- `LinkedBlockingQueue.take()` はキューが空の場合ワーカーをブロック
- 複数スレッドが `take()` を呼んでも、各要素は1スレッドにしか渡されない（Competing Consumers）

### WorkerPool（スレッド管理）

```kotlin
class WorkerPool<T : Task>(
    private val queue: TaskQueue<T>,
    private val workers: List<Worker<T>>,
) {
    fun start() {
        for (worker in workers) {
            Thread({
                while (!Thread.currentThread().isInterrupted) {
                    val task = queue.take()
                    worker.process(task)
                }
            }, worker.name).start()
        }
    }

    fun shutdown() {
        threads.forEach { it.interrupt() }
        threads.forEach { it.join() }
    }
}
```

- 各ワーカーに1スレッドを割り当て、`take()` → `process()` をループ
- `shutdown()` で `interrupt` を送り、`take()` のブロックを解除して停止

### OrderWorker（注文処理）

```kotlin
class OrderWorker(override val name: String) : Worker<OrderTask> {
    override fun process(task: OrderTask) {
        // 在庫確認 → 決済 → 配送 をシミュレート
        println("[$name] 注文 ${task.orderId} を処理中...")
        // ... Thread.sleep で処理時間を表現
        println("[$name] 注文 ${task.orderId} 完了")
    }
}
```

## 実行結果

```
=== ワーカーパターン: 3ワーカー × 5タスク ===

[Queue] 注文 ORD-001 をキューに投入
[Queue] 注文 ORD-002 をキューに投入
[Queue] 注文 ORD-003 をキューに投入
[Queue] 注文 ORD-004 をキューに投入
[Queue] 注文 ORD-005 をキューに投入
[Worker-1] 注文 ORD-001 を処理中...
[Worker-2] 注文 ORD-002 を処理中...
[Worker-3] 注文 ORD-003 を処理中...
[Worker-1] 注文 ORD-001 完了
[Worker-1] 注文 ORD-004 を処理中...    ← Worker-1が空いたので次を取得
[Worker-3] 注文 ORD-003 完了
[Worker-3] 注文 ORD-005 を処理中...
[Worker-2] 注文 ORD-002 完了
[Worker-1] 注文 ORD-004 完了
[Worker-3] 注文 ORD-005 完了
```

## ポイント

1. **競合取得**: 同じタスクは1つのワーカーだけが処理する（`LinkedBlockingQueue` が保証）
2. **負荷分散**: 処理が速いワーカーが多くのタスクを取得する（自然なロードバランシング）
3. **スケーラビリティ**: ワーカー数を増やすだけで処理能力が向上
4. **常駐**: ワーカーはキューが空でも待機し続け、新しいタスクが来たら即座に処理

## ブローカー・メディエータとの決定的な違い

ブローカーではイベントが **全購読者に配信** される（fan-out）。メディエータでは **メディエータが順序を制御** して逐次処理する。ワーカーパターンでは、キューに投入されたタスクを **複数ワーカーが競合取得** し、各タスクは1回だけ処理される。

この違いが適用場面を決める:
- 独立した副作用の並列実行 → **ブローカー**
- 順序と条件分岐のあるワークフロー → **メディエータ**
- 大量タスクの並列分散処理 → **ワーカー**

## 実世界の例

| 技術 | 説明 |
|---|---|
| Sidekiq (Ruby) | Redis キューから複数ワーカースレッドがジョブを競合取得 |
| Celery (Python) | RabbitMQ/Redis をブローカーとして分散タスク処理 |
| AWS SQS + Lambda | SQS キューからメッセージを取得して Lambda で処理 |
| Java ExecutorService | スレッドプールがタスクキューから取得して実行 |
