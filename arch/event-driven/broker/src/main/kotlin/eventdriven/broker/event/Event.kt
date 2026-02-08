package eventdriven.broker.event

import java.time.Instant

interface Event {
    val eventId: String
    val timestamp: Instant
}
