package com.ecommerce.orders.repository

import com.ecommerce.orders.model.Order
import com.ecommerce.orders.model.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByUserId(userId: Long): List<Order>
    fun findByStatus(status: OrderStatus): List<Order>
    fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order>

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate")
    fun findRecentOrders(startDate: java.time.LocalDateTime): List<Order>
}
