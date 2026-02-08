package eventdriven.broker.bus

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import eventdriven.broker.event.Event
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.Properties
import java.util.UUID
import kotlin.reflect.KClass

class KafkaEventBus(private val bootstrapServers: String = "localhost:9092") : EventBus, AutoCloseable {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())

    private val producer: KafkaProducer<String, String> = KafkaProducer(producerProps())

    private val consumerThreads = mutableListOf<Thread>()
    private val consumers = mutableListOf<KafkaConsumer<String, String>>()

    @Volatile
    private var running = true

    private fun topicName(eventType: KClass<*>): String = eventType.simpleName!!

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event> subscribe(eventType: KClass<T>, handler: (T) -> Unit) {
        val topic = topicName(eventType)
        val consumer = KafkaConsumer<String, String>(consumerProps())
        consumer.subscribe(listOf(topic))
        consumers.add(consumer)

        val thread = Thread {
            consumer.use { c ->
                while (running) {
                    val records = c.poll(Duration.ofMillis(100))
                    for (record in records) {
                        val event = objectMapper.readValue(record.value(), eventType.java)
                        handler(event)
                    }
                }
            }
        }
        thread.isDaemon = true
        thread.name = "kafka-consumer-$topic-${UUID.randomUUID().toString().take(8)}"
        thread.start()
        consumerThreads.add(thread)
    }

    override fun publish(event: Event) {
        val topic = topicName(event::class)
        val json = objectMapper.writeValueAsString(event)
        producer.send(ProducerRecord(topic, event.eventId, json)).get()
    }

    override fun close() {
        running = false
        consumerThreads.forEach { it.join(3000) }
        producer.close()
    }

    private fun producerProps() = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
    }

    private fun consumerProps() = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        // 購読者ごとに別グループ → 全員がすべてのメッセージを受信（ファンアウト）
        put(ConsumerConfig.GROUP_ID_CONFIG, "subscriber-${UUID.randomUUID()}")
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
    }
}
