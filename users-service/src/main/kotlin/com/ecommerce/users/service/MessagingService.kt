package com.ecommerce.users.service

import com.ecommerce.users.model.User
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
        const val USER_REGISTERED_ROUTING_KEY = "user.registered"
        const val USER_UPDATED_ROUTING_KEY = "user.updated"
        const val USER_DELETED_ROUTING_KEY = "user.deleted"
    }

    fun publishUserRegistered(user: User) {
        try {
            val message = mapOf(
                "eventType" to "USER_REGISTERED",
                "userId" to user.id,
                "email" to user.email,
                "fullName" to user.fullName,
                "timestamp" to System.currentTimeMillis()
            )
            rabbitTemplate.convertAndSend(EXCHANGE, USER_REGISTERED_ROUTING_KEY, objectMapper.writeValueAsString(message))
            logger.info("Published user registered event for user: ${user.id}")
        } catch (e: Exception) {
            logger.error("Error publishing user registered event", e)
        }
    }

    fun publishUserUpdated(user: User) {
        try {
            val message = mapOf(
                "eventType" to "USER_UPDATED",
                "userId" to user.id,
                "email" to user.email,
                "fullName" to user.fullName,
                "timestamp" to System.currentTimeMillis()
            )
            rabbitTemplate.convertAndSend(EXCHANGE, USER_UPDATED_ROUTING_KEY, objectMapper.writeValueAsString(message))
            logger.info("Published user updated event for user: ${user.id}")
        } catch (e: Exception) {
            logger.error("Error publishing user updated event", e)
        }
    }

    fun publishUserDeleted(user: User) {
        try {
            val message = mapOf(
                "eventType" to "USER_DELETED",
                "userId" to user.id,
                "email" to user.email,
                "timestamp" to System.currentTimeMillis()
            )
            rabbitTemplate.convertAndSend(EXCHANGE, USER_DELETED_ROUTING_KEY, objectMapper.writeValueAsString(message))
            logger.info("Published user deleted event for user: ${user.id}")
        } catch (e: Exception) {
            logger.error("Error publishing user deleted event", e)
        }
    }
}
