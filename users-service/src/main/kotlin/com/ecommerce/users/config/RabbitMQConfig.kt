package com.ecommerce.users.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    companion object {
        const val EXCHANGE = "ecommerce.exchange"
        const val USER_REGISTERED_QUEUE = "user.registered.queue"
        const val USER_UPDATED_QUEUE = "user.updated.queue"
        const val USER_DELETED_QUEUE = "user.deleted.queue"
    }

    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange(EXCHANGE)
    }

    @Bean
    fun userRegisteredQueue(): Queue {
        return QueueBuilder.durable(USER_REGISTERED_QUEUE).build()
    }

    @Bean
    fun userUpdatedQueue(): Queue {
        return QueueBuilder.durable(USER_UPDATED_QUEUE).build()
    }

    @Bean
    fun userDeletedQueue(): Queue {
        return QueueBuilder.durable(USER_DELETED_QUEUE).build()
    }

    @Bean
    fun userRegisteredBinding(userRegisteredQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(userRegisteredQueue).to(exchange).with("user.registered")
    }

    @Bean
    fun userUpdatedBinding(userUpdatedQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(userUpdatedQueue).to(exchange).with("user.updated")
    }

    @Bean
    fun userDeletedBinding(userDeletedQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(userDeletedQueue).to(exchange).with("user.deleted")
    }

    @Bean
    fun messageConverter(): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = messageConverter()
        return template
    }
}
