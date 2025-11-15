package com.ecommerce.notifications.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EmailService {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendEmail(userId: Long, subject: String, message: String) {
        // In a real implementation, this would use JavaMailSender or an email service provider
        // For this demo, we'll just log the email
        logger.info("""
            ==================== EMAIL ====================
            To: User ID $userId
            Subject: $subject
            Message:
            $message
            ===============================================
        """.trimIndent())

        // Simulate email sending delay
        Thread.sleep(100)

        logger.info("Email sent successfully to user: $userId")
    }
}
