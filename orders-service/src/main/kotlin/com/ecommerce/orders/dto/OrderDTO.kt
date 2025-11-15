package com.ecommerce.orders.dto

import com.ecommerce.orders.model.OrderStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderDTO(
    val id: Long?,
    val userId: Long,
    val items: List<OrderItemDTO>,
    val totalAmount: BigDecimal,
    val status: OrderStatus,
    val shippingAddress: String?,
    val paymentMethod: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

data class OrderItemDTO(
    val id: Long?,
    val productId: Long,
    val productName: String,
    val price: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal
)

data class CreateOrderRequest(
    @field:NotNull(message = "User ID is required")
    val userId: Long,

    @field:NotEmpty(message = "Order must have at least one item")
    val items: List<CreateOrderItemRequest>,

    val shippingAddress: String? = null,
    val paymentMethod: String? = null
)

data class CreateOrderItemRequest(
    @field:NotNull(message = "Product ID is required")
    val productId: Long,

    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int
)

data class UpdateOrderStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: OrderStatus
)
