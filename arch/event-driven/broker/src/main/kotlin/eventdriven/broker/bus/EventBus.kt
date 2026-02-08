package eventdriven.broker.bus

import eventdriven.broker.event.Event
import kotlin.reflect.KClass

interface EventBus {
    fun <T : Event> subscribe(eventType: KClass<T>, handler: (T) -> Unit)
    fun publish(event: Event)
}
