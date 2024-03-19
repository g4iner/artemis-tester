package com.artemis.tester

import jakarta.jms.Message
import jakarta.jms.MessageListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component

@Component
class ExampleMqListener : MqListener<ExampleMessage> {
    override fun handleMessage(message: ExampleMessage): Boolean {
        return runBlocking {
            LoggerFactory.getLogger(ExampleMqListener::class.java).info("Incoming ExampleMessage: $message")
            true
        }
    }
}

data class ExampleMessage(val data: String) : MqMessage

class MqListenerImpl<T : MqMessage>(private val messageConverter: MessageConverter, private val handler: MqListener<T>) : MessageListener {
    override fun onMessage(jmsMessage: Message) {
        val message = messageConverter.fromMessage(jmsMessage) as? T ?: error("Unexpected message type")
        runCatching {
            handler.handleMessage(message)
        }.getOrElse {
            LoggerFactory.getLogger(MqListenerImpl::class.java).error("Unable to handle message", it)
        }
        jmsMessage.acknowledge()
    }
}

interface MqListener<T> {
    fun handleMessage(message: T): Boolean
}
