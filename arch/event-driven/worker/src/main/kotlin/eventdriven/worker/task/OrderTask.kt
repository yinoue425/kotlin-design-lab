package eventdriven.worker.task

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderTask(
    val orderId: String,
    val customerId: String,
    val items: List<String>,
    val totalAmount: BigDecimal,
    override val taskId: String = UUID.randomUUID().toString(),
    override val createdAt: Instant = Instant.now(),
) : Task
