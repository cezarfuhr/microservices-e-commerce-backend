package com.ecommerce.orders.service

import com.ecommerce.orders.model.Order
import com.ecommerce.orders.model.OrderStatus
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
        const val ORDER_CREATED_ROUTING_KEY = "order.created"
        const val ORDER_STATUS_UPDATED_ROUTING_KEY = "order.status.updated"
        const val ORDER_CANCELLED_ROUTING_KEY = "order.cancelled"
    }

    fun publishOrderCreated(order: Order) {
        try {
            val message = mapOf(
                "eventType" to "ORDER_CREATED",
                "orderId" to order.id,
                "userId" to order.userId,
                "totalAmount" to order.totalAmount,
                "status" to order.status,
                "itemCount" to order.items.size,
                "timestamp" to System.currentTimeMillis()
            )
            rabbitTemplate.convertAndSend(EXCHANGE, ORDER_CREATED_ROUTING_KEY, objectMapper.writeValueAsString(message))
            logger.info("Published order created event for order: ${order.id}")
        } catch (e: Exception) {
            logger.error("Error publishing order created event", e)
        }
    }

    fun publishOrderStatusUpdated(order: Order, oldStatus: OrderStatus) {
        try {
            val message = mapOf(
                "eventType" to "ORDER_STATUS_UPDATED",
                "orderId" to order.id,
                "userId" to order.userId,
                "oldStatus" to oldStatus,
                "newStatus" to order.status,
                "timestamp" to System.currentTimeMillis()
            )
            rabbitTemplate.convertAndSend(EXCHANGE, ORDER_STATUS_UPDATED_ROUTING_KEY, objectMapper.writeValueAsString(message))
            logger.info("Published order status updated event for order: ${order.id}")
        } catch (e: Exception) {
            logger.error("Error publishing order status updated event", e)
        }
    }

    fun publishOrderCancelled(order: Order) {
        try {
            val message = mapOf(
                "eventType" to "ORDER_CANCELLED",
                "orderId" to order.id,
                "userId" to order.userId,
                "timestamp" to System.currentTimeMillis()
            )
            rabbitTemplate.convertAndSend(EXCHANGE, ORDER_CANCELLED_ROUTING_KEY, objectMapper.writeValueAsString(message))
            logger.info("Published order cancelled event for order: ${order.id}")
        } catch (e: Exception) {
            logger.error("Error publishing order cancelled event", e)
        }
    }
}
