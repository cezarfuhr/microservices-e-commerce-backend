package com.ecommerce.products.service

import com.ecommerce.products.model.Product
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class MessagingService(
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(MessagingService::class.java)

    companion object {
        const val EXCHANGE = "ecommerce.exchange"
        const val PRODUCT_CREATED_ROUTING_KEY = "product.created"
        const val PRODUCT_UPDATED_ROUTING_KEY = "product.updated"
        const val PRODUCT_DELETED_ROUTING_KEY = "product.deleted"
        const val STOCK_UPDATED_ROUTING_KEY = "product.stock.updated"
    }

    fun publishProductCreated(product: Product) {
        try {
            val message = mapOf(
                "eventType" to "PRODUCT_CREATED",
                "productId" to product.id,
                "name" to product.name,
                "price" to product.price,
                "category" to product.category,
                "timestamp" to System.currentTimeMillis()
            )
            rabbitTemplate.convertAndSend(EXCHANGE, PRODUCT_CREATED_ROUTING_KEY, objectMapper.writeValueAsString(message))
            logger.info("Published product created event for product: ${product.id}")
        } catch (e: Exception) {
            logger.error("Error publishing product created event", e)
        }
    }

    fun publishProductUpdated(product: Product) {
        try {
            val message = mapOf(
                "eventType" to "PRODUCT_UPDATED",
                "productId" to product.id,
                "name" to product.name,
                "price" to product.price,
                "stock" to product.stock,
                "category" to product.category,
                "active" to product.active,
                "timestamp" to System.currentTimeMillis()
            )
            rabbitTemplate.convertAndSend(EXCHANGE, PRODUCT_UPDATED_ROUTING_KEY, objectMapper.writeValueAsString(message))
            logger.info("Published product updated event for product: ${product.id}")
        } catch (e: Exception) {
            logger.error("Error publishing product updated event", e)
        }
    }

    fun publishProductDeleted(product: Product) {
        try {
            val message = mapOf(
                "eventType" to "PRODUCT_DELETED",
                "productId" to product.id,
                "name" to product.name,
                "timestamp" to System.currentTimeMillis()
            )
            rabbitTemplate.convertAndSend(EXCHANGE, PRODUCT_DELETED_ROUTING_KEY, objectMapper.writeValueAsString(message))
            logger.info("Published product deleted event for product: ${product.id}")
        } catch (e: Exception) {
            logger.error("Error publishing product deleted event", e)
        }
    }

    fun publishStockUpdated(product: Product) {
        try {
            val message = mapOf(
                "eventType" to "STOCK_UPDATED",
                "productId" to product.id,
                "stock" to product.stock,
                "timestamp" to System.currentTimeMillis()
            )
            rabbitTemplate.convertAndSend(EXCHANGE, STOCK_UPDATED_ROUTING_KEY, objectMapper.writeValueAsString(message))
            logger.info("Published stock updated event for product: ${product.id}")
        } catch (e: Exception) {
            logger.error("Error publishing stock updated event", e)
        }
    }
}
