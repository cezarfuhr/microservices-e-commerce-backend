package com.ecommerce.analytics.listener

import com.ecommerce.analytics.model.EventType
import com.ecommerce.analytics.service.AnalyticsService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class EventListener(
    private val analyticsService: AnalyticsService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(EventListener::class.java)

    @RabbitListener(queues = ["product.created.queue"])
    fun handleProductCreated(message: String) {
        try {
            logger.info("Received product created event: $message")
            val event = objectMapper.readValue(message, Map::class.java)

            val productId = (event["productId"] as Number?)?.toLong()

            analyticsService.trackEvent(EventType.PRODUCT_CREATED, productId, null, message)
            analyticsService.incrementProducts()
        } catch (e: Exception) {
            logger.error("Error processing product created event", e)
        }
    }

    @RabbitListener(queues = ["product.updated.queue"])
    fun handleProductUpdated(message: String) {
        try {
            logger.info("Received product updated event: $message")
            val event = objectMapper.readValue(message, Map::class.java)

            val productId = (event["productId"] as Number?)?.toLong()

            analyticsService.trackEvent(EventType.PRODUCT_UPDATED, productId, null, message)
        } catch (e: Exception) {
            logger.error("Error processing product updated event", e)
        }
    }

    @RabbitListener(queues = ["user.registered.queue"])
    fun handleUserRegistered(message: String) {
        try {
            logger.info("Received user registered event: $message")
            val event = objectMapper.readValue(message, Map::class.java)

            val userId = (event["userId"] as Number?)?.toLong()

            analyticsService.trackEvent(EventType.USER_REGISTERED, userId, userId, message)
            analyticsService.incrementUsers()
        } catch (e: Exception) {
            logger.error("Error processing user registered event", e)
        }
    }

    @RabbitListener(queues = ["order.created.queue"])
    fun handleOrderCreated(message: String) {
        try {
            logger.info("Received order created event: $message")
            val event = objectMapper.readValue(message, Map::class.java)

            val orderId = (event["orderId"] as Number?)?.toLong()
            val userId = (event["userId"] as Number?)?.toLong()
            val totalAmount = event["totalAmount"]?.toString()?.let { BigDecimal(it) } ?: BigDecimal.ZERO

            analyticsService.trackEvent(EventType.ORDER_CREATED, orderId, userId, message)
            analyticsService.incrementOrders(totalAmount)
        } catch (e: Exception) {
            logger.error("Error processing order created event", e)
        }
    }

    @RabbitListener(queues = ["order.status.updated.queue"])
    fun handleOrderStatusUpdated(message: String) {
        try {
            logger.info("Received order status updated event: $message")
            val event = objectMapper.readValue(message, Map::class.java)

            val orderId = (event["orderId"] as Number?)?.toLong()
            val userId = (event["userId"] as Number?)?.toLong()

            analyticsService.trackEvent(EventType.ORDER_STATUS_UPDATED, orderId, userId, message)
        } catch (e: Exception) {
            logger.error("Error processing order status updated event", e)
        }
    }
}
