package eventdriven.worker.task

import java.time.Instant

interface Task {
    val taskId: String
    val createdAt: Instant
}
