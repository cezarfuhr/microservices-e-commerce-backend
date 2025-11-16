package com.ecommerce.analytics.service

import com.ecommerce.analytics.dto.AnalyticsDTO
import com.ecommerce.analytics.dto.EventDTO
import com.ecommerce.analytics.model.AnalyticsSummary
import com.ecommerce.analytics.model.Event
import com.ecommerce.analytics.model.EventType
import com.ecommerce.analytics.repository.AnalyticsSummaryRepository
import com.ecommerce.analytics.repository.EventRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class AnalyticsService(
    private val eventRepository: EventRepository,
    private val summaryRepository: AnalyticsSummaryRepository
) {
    private val logger = LoggerFactory.getLogger(AnalyticsService::class.java)

    @Transactional
    fun trackEvent(eventType: EventType, entityId: Long?, userId: Long?, metadata: String?) {
        logger.info("Tracking event: $eventType for entity: $entityId, user: $userId")

        val event = Event(
            eventType = eventType,
            entityId = entityId,
            userId = userId,
            metadata = metadata
        )

        eventRepository.save(event)
    }

    fun getAnalyticsSummary(): AnalyticsDTO {
        logger.info("Fetching analytics summary")

        val summary = summaryRepository.findFirstByOrderByUpdatedAtDesc()
            ?: AnalyticsSummary()

        return AnalyticsDTO(
            totalOrders = summary.totalOrders,
            totalRevenue = summary.totalRevenue,
            totalUsers = summary.totalUsers,
            totalProducts = summary.totalProducts,
            lastUpdated = summary.updatedAt
        )
    }

    fun getRecentEvents(hours: Int = 24): List<EventDTO> {
        logger.info("Fetching events from last $hours hours")

        val since = LocalDateTime.now().minusHours(hours.toLong())
        return eventRepository.findRecentEvents(since).map { it.toDTO() }
    }

    fun getEventsByType(eventType: EventType): List<EventDTO> {
        logger.info("Fetching events of type: $eventType")
        return eventRepository.findByEventType(eventType).map { it.toDTO() }
    }

    fun getEventsByUser(userId: Long): List<EventDTO> {
        logger.info("Fetching events for user: $userId")
        return eventRepository.findByUserId(userId).map { it.toDTO() }
    }

    @Transactional
    fun updateSummary(totalOrders: Long? = null, revenue: BigDecimal? = null, totalUsers: Long? = null, totalProducts: Long? = null) {
        logger.info("Updating analytics summary")

        val summary = summaryRepository.findFirstByOrderByUpdatedAtDesc()
            ?: AnalyticsSummary()

        totalOrders?.let { summary.totalOrders = it }
        revenue?.let { summary.totalRevenue = summary.totalRevenue.add(it) }
        totalUsers?.let { summary.totalUsers = it }
        totalProducts?.let { summary.totalProducts = it }
        summary.updatedAt = LocalDateTime.now()

        summaryRepository.save(summary)
    }

    @Transactional
    fun incrementOrders(revenue: BigDecimal) {
        val summary = summaryRepository.findFirstByOrderByUpdatedAtDesc()
            ?: AnalyticsSummary()

        summary.totalOrders += 1
        summary.totalRevenue = summary.totalRevenue.add(revenue)
        summary.updatedAt = LocalDateTime.now()

        summaryRepository.save(summary)
    }

    @Transactional
    fun incrementUsers() {
        val summary = summaryRepository.findFirstByOrderByUpdatedAtDesc()
            ?: AnalyticsSummary()

        summary.totalUsers += 1
        summary.updatedAt = LocalDateTime.now()

        summaryRepository.save(summary)
    }

    @Transactional
    fun incrementProducts() {
        val summary = summaryRepository.findFirstByOrderByUpdatedAtDesc()
            ?: AnalyticsSummary()

        summary.totalProducts += 1
        summary.updatedAt = LocalDateTime.now()

        summaryRepository.save(summary)
    }

    private fun Event.toDTO() = EventDTO(
        id = id,
        eventType = eventType,
        entityId = entityId,
        userId = userId,
        metadata = metadata,
        createdAt = createdAt
    )
}
