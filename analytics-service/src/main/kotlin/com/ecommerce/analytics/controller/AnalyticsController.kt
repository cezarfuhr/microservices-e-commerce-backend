package com.ecommerce.analytics.controller

import com.ecommerce.analytics.dto.AnalyticsDTO
import com.ecommerce.analytics.dto.EventDTO
import com.ecommerce.analytics.model.EventType
import com.ecommerce.analytics.service.AnalyticsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Analytics and metrics APIs")
class AnalyticsController(private val analyticsService: AnalyticsService) {

    @GetMapping("/summary")
    @Operation(summary = "Get analytics summary")
    fun getSummary(): ResponseEntity<AnalyticsDTO> {
        return ResponseEntity.ok(analyticsService.getAnalyticsSummary())
    }

    @GetMapping("/events/recent")
    @Operation(summary = "Get recent events")
    fun getRecentEvents(@RequestParam(defaultValue = "24") hours: Int): ResponseEntity<List<EventDTO>> {
        return ResponseEntity.ok(analyticsService.getRecentEvents(hours))
    }

    @GetMapping("/events/type/{eventType}")
    @Operation(summary = "Get events by type")
    fun getEventsByType(@PathVariable eventType: EventType): ResponseEntity<List<EventDTO>> {
        return ResponseEntity.ok(analyticsService.getEventsByType(eventType))
    }

    @GetMapping("/events/user/{userId}")
    @Operation(summary = "Get events by user")
    fun getEventsByUser(@PathVariable userId: Long): ResponseEntity<List<EventDTO>> {
        return ResponseEntity.ok(analyticsService.getEventsByUser(userId))
    }
}
