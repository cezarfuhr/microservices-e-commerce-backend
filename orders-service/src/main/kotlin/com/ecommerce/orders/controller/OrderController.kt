package com.ecommerce.orders.controller

import com.ecommerce.orders.dto.CreateOrderRequest
import com.ecommerce.orders.dto.OrderDTO
import com.ecommerce.orders.dto.UpdateOrderStatusRequest
import com.ecommerce.orders.model.OrderStatus
import com.ecommerce.orders.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management APIs")
class OrderController(private val orderService: OrderService) {

    @GetMapping
    @Operation(summary = "Get all orders")
    fun getAllOrders(): ResponseEntity<List<OrderDTO>> {
        return ResponseEntity.ok(orderService.getAllOrders())
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    fun getOrderById(@PathVariable id: Long): ResponseEntity<OrderDTO> {
        return ResponseEntity.ok(orderService.getOrderById(id))
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user ID")
    fun getOrdersByUserId(@PathVariable userId: Long): ResponseEntity<List<OrderDTO>> {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId))
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status")
    fun getOrdersByStatus(@PathVariable status: OrderStatus): ResponseEntity<List<OrderDTO>> {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status))
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    fun createOrder(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<OrderDTO> {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request))
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status")
    fun updateOrderStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<OrderDTO> {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel order")
    fun cancelOrder(@PathVariable id: Long): ResponseEntity<Void> {
        orderService.cancelOrder(id)
        return ResponseEntity.noContent().build()
    }
}
