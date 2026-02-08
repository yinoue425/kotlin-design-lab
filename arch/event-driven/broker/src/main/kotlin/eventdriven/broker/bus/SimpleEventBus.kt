package eventdriven.broker.bus

import eventdriven.broker.event.Event
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

class SimpleEventBus : EventBus {
    private val subscribers = ConcurrentHashMap<KClass<*>, MutableList<(Event) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event> subscribe(eventType: KClass<T>, handler: (T) -> Unit) {
        subscribers.getOrPut(eventType) { CopyOnWriteArrayList() }
            .add { event -> handler(event as T) }
    }

    override fun publish(event: Event) {
        subscribers[event::class]?.forEach { handler -> handler(event) }
    }
}
