package com.artemis.tester

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.activemq.artemis.api.jms.ActiveMQJMSConstants
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.config.JmsListenerEndpointRegistry
import org.springframework.jms.connection.CachingConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory
import org.springframework.jms.support.converter.MappingJackson2MessageConverter
import org.springframework.jms.support.converter.MessageType

@Configuration
@ConfigurationProperties(prefix = "mq")
@EnableJms
class MqConnectionConfig {
    var front: MqConfig = MqConfig()
    var back: MqConfig? = null
    var frontConnectionCacheSize: Int = 10

    @Bean
    @Primary
    fun frontConnectionFactory(): CachingConnectionFactory =
        CachingConnectionFactory(createFactory(front)).apply {
            sessionCacheSize = frontConnectionCacheSize
        }

    @Bean
    fun backConnectionFactory(): SingleConnectionFactory =
        SingleConnectionFactory(createFactory(back ?: front)).apply {
            setReconnectOnException(true)
        }

    private fun createFactory(config: MqConfig) =
        ActiveMQConnectionFactory(config.url).apply {
            user = config.user
            password = config.password
        }

    @Bean
    fun jmsListenerContainerFactory(): DefaultJmsListenerContainerFactory =
        DefaultJmsListenerContainerFactory().apply {
            setConnectionFactory(backConnectionFactory())
            setMessageConverter(jacksonJmsMessageConverter())
            setSessionAcknowledgeMode(ActiveMQJMSConstants.INDIVIDUAL_ACKNOWLEDGE)
        }

    @Bean
    fun jmsListenerEndpointRegistry(): JmsListenerEndpointRegistry = JmsListenerEndpointRegistry()

    @Bean
    fun jacksonJmsMessageConverter() = MappingJackson2MessageConverter().apply {
        setTargetType(MessageType.TEXT)
        setTypeIdPropertyName("_type")
        setObjectMapper(jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        })
    }
}

class MqConfig {
    var url: String = "tcp://localhost:61616"
    var user: String? = null
    var password: String? = null
}
