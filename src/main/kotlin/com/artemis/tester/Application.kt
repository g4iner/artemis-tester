package com.artemis.tester

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args).apply {
        val sender = getBean(ExampleMqSender::class.java)
        sender.send(ExampleMessage("example"))
    }
}
