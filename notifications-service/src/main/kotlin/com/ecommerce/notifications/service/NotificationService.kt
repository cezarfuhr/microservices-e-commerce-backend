package com.ecommerce.notifications.service

import com.ecommerce.notifications.model.Notification
import com.ecommerce.notifications.model.NotificationType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NotificationService(
    private val emailService: EmailService
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    fun sendOrderConfirmation(userId: Long, orderId: Long, totalAmount: String) {
        logger.info("Sending order confirmation to user: $userId for order: $orderId")

        val subject = "Order Confirmation #$orderId"
        val message = """
            Thank you for your order!

            Order ID: $orderId
            Total Amount: $totalAmount

            Your order has been confirmed and will be processed soon.

            Best regards,
            E-Commerce Team
        """.trimIndent()

        emailService.sendEmail(userId, subject, message)
    }

    fun sendOrderStatusUpdate(userId: Long, orderId: Long, oldStatus: String, newStatus: String) {
        logger.info("Sending order status update to user: $userId for order: $orderId")

        val subject = "Order Status Update #$orderId"
        val message = """
            Your order status has been updated.

            Order ID: $orderId
            Previous Status: $oldStatus
            New Status: $newStatus

            Thank you for your patience.

            Best regards,
            E-Commerce Team
        """.trimIndent()

        emailService.sendEmail(userId, subject, message)
    }

    fun sendWelcomeEmail(userId: Long, fullName: String, email: String) {
        logger.info("Sending welcome email to user: $userId")

        val subject = "Welcome to Our E-Commerce Platform!"
        val message = """
            Hello $fullName,

            Welcome to our e-commerce platform! We're excited to have you on board.

            Your account has been successfully created with email: $email

            Start exploring our products and enjoy shopping!

            Best regards,
            E-Commerce Team
        """.trimIndent()

        emailService.sendEmail(userId, subject, message)
    }

    fun sendProductStockAlert(productId: Long, productName: String, stock: Int) {
        logger.info("Sending low stock alert for product: $productId")

        val subject = "Low Stock Alert: $productName"
        val message = """
            ALERT: Low stock detected

            Product: $productName (ID: $productId)
            Current Stock: $stock units

            Please restock this product soon.

            E-Commerce System
        """.trimIndent()

        // Send to admin (userId = 1 for simplicity)
        emailService.sendEmail(1L, subject, message)
    }
}
