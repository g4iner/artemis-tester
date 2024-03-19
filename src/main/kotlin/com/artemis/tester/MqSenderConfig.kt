package com.artemis.tester

import javax.jms.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.support.converter.MessageConverter

@Configuration
class MqSenderConfig(
    private val connectionFactory: ConnectionFactory,
    private val converter: MessageConverter
) {
    @Bean
    fun mqSender() = MqSender(connectionFactory, converter)
}