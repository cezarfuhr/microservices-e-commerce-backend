package com.ecommerce.products.config

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
        const val PRODUCT_CREATED_QUEUE = "product.created.queue"
        const val PRODUCT_UPDATED_QUEUE = "product.updated.queue"
        const val PRODUCT_DELETED_QUEUE = "product.deleted.queue"
        const val STOCK_UPDATED_QUEUE = "product.stock.updated.queue"
    }

    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange(EXCHANGE)
    }

    @Bean
    fun productCreatedQueue(): Queue {
        return QueueBuilder.durable(PRODUCT_CREATED_QUEUE).build()
    }

    @Bean
    fun productUpdatedQueue(): Queue {
        return QueueBuilder.durable(PRODUCT_UPDATED_QUEUE).build()
    }

    @Bean
    fun productDeletedQueue(): Queue {
        return QueueBuilder.durable(PRODUCT_DELETED_QUEUE).build()
    }

    @Bean
    fun stockUpdatedQueue(): Queue {
        return QueueBuilder.durable(STOCK_UPDATED_QUEUE).build()
    }

    @Bean
    fun productCreatedBinding(productCreatedQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(productCreatedQueue).to(exchange).with("product.created")
    }

    @Bean
    fun productUpdatedBinding(productUpdatedQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(productUpdatedQueue).to(exchange).with("product.updated")
    }

    @Bean
    fun productDeletedBinding(productDeletedQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(productDeletedQueue).to(exchange).with("product.deleted")
    }

    @Bean
    fun stockUpdatedBinding(stockUpdatedQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(stockUpdatedQueue).to(exchange).with("product.stock.updated")
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
