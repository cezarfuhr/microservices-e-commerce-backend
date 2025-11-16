package com.ecommerce.analytics.repository

import com.ecommerce.analytics.model.Event
import com.ecommerce.analytics.model.EventType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventRepository : JpaRepository<Event, Long> {
    fun findByEventType(eventType: EventType): List<Event>
    fun findByUserId(userId: Long): List<Event>
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<Event>

    @Query("SELECT COUNT(e) FROM Event e WHERE e.eventType = :eventType")
    fun countByEventType(eventType: EventType): Long

    @Query("SELECT e FROM Event e WHERE e.createdAt >= :since ORDER BY e.createdAt DESC")
    fun findRecentEvents(since: LocalDateTime): List<Event>
}
