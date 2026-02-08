package eventdriven.mediator.mediator

import eventdriven.mediator.component.Component
import eventdriven.mediator.event.Event

interface EventMediator {
    fun registerComponent(component: Component)
    fun processEvent(event: Event)
}
