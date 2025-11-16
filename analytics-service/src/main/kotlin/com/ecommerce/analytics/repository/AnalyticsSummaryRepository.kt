package com.ecommerce.analytics.repository

import com.ecommerce.analytics.model.AnalyticsSummary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AnalyticsSummaryRepository : JpaRepository<AnalyticsSummary, Long> {
    fun findFirstByOrderByUpdatedAtDesc(): AnalyticsSummary?
}
