package eventdriven.mediator.event

import java.time.Instant

interface Event {
    val eventId: String
    val timestamp: Instant
}
