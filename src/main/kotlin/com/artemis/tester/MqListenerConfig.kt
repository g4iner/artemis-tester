package com.artemis.tester

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.config.JmsListenerEndpointRegistry
import org.springframework.jms.config.SimpleJmsListenerEndpoint
import org.springframework.jms.support.converter.MessageConverter
import java.io.Serializable
import java.lang.reflect.ParameterizedType

@Configuration
class MqListenerConfig(
    override val listenerContainerFactory: DefaultJmsListenerContainerFactory,
    override val jmsListenerEndpointRegistry: JmsListenerEndpointRegistry,
    override val messageConverter: MessageConverter,
    private val mqListeners: List<MqListener<out MqMessage>>,
) : JmsListenerConfig {
    @EventListener(ApplicationReadyEvent::class)
    fun startListeners() {
        mqListeners.forEach {
            listenToQueue(it)
        }
    }

    private fun <T : MqMessage> listenToQueue(handler: MqListener<T>) {
        handler.messageType().let {
            listen(
                listenerConfig = ListenerConfig(it.simpleName, it.simpleName, "example.queue", "1-2"),
                handler = handler,
                messageClass = it
            )
        }
    }

    private fun <T : MqMessage> MqListener<T>.messageType(): Class<T> = (javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0] as Class<T>
}

@JsonIgnoreProperties(value = ["type"])
interface MqMessage : Serializable {
    val type: String
        get() = javaClass.simpleName
}

class ListenerConfig(
    val messageType: String,
    val id: String,
    val destination: String,
    val concurrency: String
)

interface JmsListenerConfig {
    val listenerContainerFactory: DefaultJmsListenerContainerFactory
    val jmsListenerEndpointRegistry: JmsListenerEndpointRegistry
    val messageConverter: MessageConverter
}

fun <T : MqMessage> JmsListenerConfig.listen(
    listenerConfig: ListenerConfig,
    handler: MqListener<T>,
    messageClass: Class<T>
) = SimpleJmsListenerEndpoint().apply {
    id = listenerConfig.id
    destination = listenerConfig.destination
    concurrency = listenerConfig.concurrency
    selector = "_type = '${messageClass.canonicalName}'"
    messageListener = MqListenerImpl(messageConverter, handler)
}.let {
    jmsListenerEndpointRegistry.registerListenerContainer(it, listenerContainerFactory, true)
}
