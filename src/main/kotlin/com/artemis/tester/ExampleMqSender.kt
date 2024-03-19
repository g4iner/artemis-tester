package com.artemis.tester

import jakarta.jms.ConnectionFactory
import org.apache.activemq.artemis.jms.client.ActiveMQDestination
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component

const val currentDestination = "example.queue"

@Component
class ExampleMqSender(
    private val sender: MqSender,
) {
    fun send(message: MqMessage) {
        sender.send(currentDestination, message)
    }
}

class MqSender(
    private val connectionFactory: ConnectionFactory,
    private val converter: MessageConverter
) {
    fun send(destination: String, message: MqMessage) {
        JmsTemplateData(
            destination,
            message,
        ).let {
            sendWithJms(it)
        }
    }

    private fun sendWithJms(data: JmsTemplateData) {
        JmsTemplate(connectionFactory).apply {
            defaultDestination = ActiveMQDestination.createQueue(data.destination)
            messageConverter = converter
            isExplicitQosEnabled = true
        }.convertAndSend(data.message)
    }
}

class JmsTemplateData(
    val destination: String,
    val message: MqMessage,
)