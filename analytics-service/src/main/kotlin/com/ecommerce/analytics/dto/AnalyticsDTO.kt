package com.ecommerce.analytics.dto

import com.ecommerce.analytics.model.EventType
import java.math.BigDecimal
import java.time.LocalDateTime

data class AnalyticsDTO(
    val totalOrders: Long,
    val totalRevenue: BigDecimal,
    val totalUsers: Long,
    val totalProducts: Long,
    val lastUpdated: LocalDateTime
)

data class EventDTO(
    val id: Long?,
    val eventType: EventType,
    val entityId: Long?,
    val userId: Long?,
    val metadata: String?,
    val createdAt: LocalDateTime
)
