package com.ecommerce.notifications.listener

import com.ecommerce.notifications.service.NotificationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class EventListener(
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(EventListener::class.java)

    @RabbitListener(queues = ["order.created.queue"])
    fun handleOrderCreated(message: String) {
        try {
            logger.info("Received order created event: $message")
            val event = objectMapper.readValue(message, Map::class.java)

            val orderId = (event["orderId"] as Number).toLong()
            val userId = (event["userId"] as Number).toLong()
            val totalAmount = event["totalAmount"].toString()

            notificationService.sendOrderConfirmation(userId, orderId, totalAmount)
        } catch (e: Exception) {
            logger.error("Error processing order created event", e)
        }
    }

    @RabbitListener(queues = ["order.status.updated.queue"])
    fun handleOrderStatusUpdated(message: String) {
        try {
            logger.info("Received order status updated event: $message")
            val event = objectMapper.readValue(message, Map::class.java)

            val orderId = (event["orderId"] as Number).toLong()
            val userId = (event["userId"] as Number).toLong()
            val oldStatus = event["oldStatus"].toString()
            val newStatus = event["newStatus"].toString()

            notificationService.sendOrderStatusUpdate(userId, orderId, oldStatus, newStatus)
        } catch (e: Exception) {
            logger.error("Error processing order status updated event", e)
        }
    }

    @RabbitListener(queues = ["user.registered.queue"])
    fun handleUserRegistered(message: String) {
        try {
            logger.info("Received user registered event: $message")
            val event = objectMapper.readValue(message, Map::class.java)

            val userId = (event["userId"] as Number).toLong()
            val fullName = event["fullName"].toString()
            val email = event["email"].toString()

            notificationService.sendWelcomeEmail(userId, fullName, email)
        } catch (e: Exception) {
            logger.error("Error processing user registered event", e)
        }
    }

    @RabbitListener(queues = ["product.stock.updated.queue"])
    fun handleStockUpdated(message: String) {
        try {
            logger.info("Received stock updated event: $message")
            val event = objectMapper.readValue(message, Map::class.java)

            val productId = (event["productId"] as Number).toLong()
            val stock = (event["stock"] as Number).toInt()

            // Send alert if stock is low (less than 10 units)
            if (stock < 10) {
                notificationService.sendProductStockAlert(productId, "Product #$productId", stock)
            }
        } catch (e: Exception) {
            logger.error("Error processing stock updated event", e)
        }
    }
}
