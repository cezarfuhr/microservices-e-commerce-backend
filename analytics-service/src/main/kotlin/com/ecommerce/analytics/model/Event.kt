package com.ecommerce.analytics.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "events")
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var eventType: EventType,

    @Column(name = "entity_id")
    var entityId: Long? = null,

    @Column(name = "user_id")
    var userId: Long? = null,

    @Column(length = 2000)
    var metadata: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class EventType {
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED,
    STOCK_UPDATED,
    USER_REGISTERED,
    USER_UPDATED,
    ORDER_CREATED,
    ORDER_STATUS_UPDATED,
    ORDER_CANCELLED
}

@Entity
@Table(name = "analytics_summary")
data class AnalyticsSummary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "total_orders")
    var totalOrders: Long = 0,

    @Column(name = "total_revenue", precision = 15, scale = 2)
    var totalRevenue: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_users")
    var totalUsers: Long = 0,

    @Column(name = "total_products")
    var totalProducts: Long = 0,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
