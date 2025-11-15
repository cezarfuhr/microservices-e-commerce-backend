package com.ecommerce.orders.config

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
        const val ORDER_CREATED_QUEUE = "order.created.queue"
        const val ORDER_STATUS_UPDATED_QUEUE = "order.status.updated.queue"
        const val ORDER_CANCELLED_QUEUE = "order.cancelled.queue"
    }

    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange(EXCHANGE)
    }

    @Bean
    fun orderCreatedQueue(): Queue {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build()
    }

    @Bean
    fun orderStatusUpdatedQueue(): Queue {
        return QueueBuilder.durable(ORDER_STATUS_UPDATED_QUEUE).build()
    }

    @Bean
    fun orderCancelledQueue(): Queue {
        return QueueBuilder.durable(ORDER_CANCELLED_QUEUE).build()
    }

    @Bean
    fun orderCreatedBinding(orderCreatedQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(orderCreatedQueue).to(exchange).with("order.created")
    }

    @Bean
    fun orderStatusUpdatedBinding(orderStatusUpdatedQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(orderStatusUpdatedQueue).to(exchange).with("order.status.updated")
    }

    @Bean
    fun orderCancelledBinding(orderCancelledQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(orderCancelledQueue).to(exchange).with("order.cancelled")
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
