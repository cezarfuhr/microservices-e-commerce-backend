package com.ecommerce.notifications.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: NotificationType,

    @Column(nullable = false)
    var subject: String,

    @Column(length = 2000, nullable = false)
    var message: String,

    @Column(nullable = false)
    var sent: Boolean = false,

    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    EMAIL,
    SMS,
    PUSH,
    IN_APP
}
