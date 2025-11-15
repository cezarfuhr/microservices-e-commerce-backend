package com.ecommerce.orders.service

import com.ecommerce.orders.dto.*
import com.ecommerce.orders.exception.InsufficientStockException
import com.ecommerce.orders.exception.InvalidOrderException
import com.ecommerce.orders.exception.OrderNotFoundException
import com.ecommerce.orders.exception.ProductNotFoundException
import com.ecommerce.orders.model.Order
import com.ecommerce.orders.model.OrderItem
import com.ecommerce.orders.model.OrderStatus
import com.ecommerce.orders.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productServiceClient: ProductServiceClient,
    private val messagingService: MessagingService
) {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    fun getAllOrders(): List<OrderDTO> {
        logger.info("Fetching all orders")
        return orderRepository.findAll().map { it.toDTO() }
    }

    fun getOrderById(id: Long): OrderDTO {
        logger.info("Fetching order with id: $id")
        val order = orderRepository.findById(id)
            .orElseThrow { OrderNotFoundException("Order not found with id: $id") }
        return order.toDTO()
    }

    fun getOrdersByUserId(userId: Long): List<OrderDTO> {
        logger.info("Fetching orders for user: $userId")
        return orderRepository.findByUserId(userId).map { it.toDTO() }
    }

    fun getOrdersByStatus(status: OrderStatus): List<OrderDTO> {
        logger.info("Fetching orders with status: $status")
        return orderRepository.findByStatus(status).map { it.toDTO() }
    }

    @Transactional
    fun createOrder(request: CreateOrderRequest): OrderDTO {
        logger.info("Creating new order for user: ${request.userId}")

        if (request.items.isEmpty()) {
            throw InvalidOrderException("Order must contain at least one item")
        }

        val order = Order(
            userId = request.userId,
            shippingAddress = request.shippingAddress,
            paymentMethod = request.paymentMethod
        )

        // Process each item
        request.items.forEach { itemRequest ->
            val product = try {
                productServiceClient.getProduct(itemRequest.productId)
            } catch (e: Exception) {
                logger.error("Failed to fetch product ${itemRequest.productId}", e)
                throw ProductNotFoundException("Product not found: ${itemRequest.productId}")
            }

            // Check stock availability
            if (product.stock < itemRequest.quantity) {
                throw InsufficientStockException(
                    "Insufficient stock for product: ${product.name}. Available: ${product.stock}, Requested: ${itemRequest.quantity}"
                )
            }

            val orderItem = OrderItem(
                productId = product.id!!,
                productName = product.name,
                price = product.price,
                quantity = itemRequest.quantity
            )
            orderItem.calculateSubtotal()
            orderItem.order = order
            order.items.add(orderItem)
        }

        order.calculateTotal()

        // Reserve stock for each product
        request.items.forEach { itemRequest ->
            val reserved = try {
                productServiceClient.reserveStock(itemRequest.productId, itemRequest.quantity)
            } catch (e: Exception) {
                logger.error("Failed to reserve stock for product ${itemRequest.productId}", e)
                throw InvalidOrderException("Failed to reserve stock for product: ${itemRequest.productId}")
            }

            if (!reserved) {
                throw InsufficientStockException("Failed to reserve stock for product: ${itemRequest.productId}")
            }
        }

        order.status = OrderStatus.CONFIRMED
        val savedOrder = orderRepository.save(order)

        // Send event to RabbitMQ
        messagingService.publishOrderCreated(savedOrder)

        return savedOrder.toDTO()
    }

    @Transactional
    fun updateOrderStatus(id: Long, request: UpdateOrderStatusRequest): OrderDTO {
        logger.info("Updating status for order: $id to ${request.status}")
        val order = orderRepository.findById(id)
            .orElseThrow { OrderNotFoundException("Order not found with id: $id") }

        val oldStatus = order.status
        order.status = request.status

        val updatedOrder = orderRepository.save(order)

        // Send event to RabbitMQ
        messagingService.publishOrderStatusUpdated(updatedOrder, oldStatus)

        return updatedOrder.toDTO()
    }

    @Transactional
    fun cancelOrder(id: Long) {
        logger.info("Cancelling order: $id")
        val order = orderRepository.findById(id)
            .orElseThrow { OrderNotFoundException("Order not found with id: $id") }

        if (order.status in listOf(OrderStatus.SHIPPED, OrderStatus.DELIVERED)) {
            throw InvalidOrderException("Cannot cancel order with status: ${order.status}")
        }

        order.status = OrderStatus.CANCELLED
        orderRepository.save(order)

        // Send event to RabbitMQ
        messagingService.publishOrderCancelled(order)
    }

    private fun Order.toDTO() = OrderDTO(
        id = id,
        userId = userId,
        items = items.map { it.toDTO() },
        totalAmount = totalAmount,
        status = status,
        shippingAddress = shippingAddress,
        paymentMethod = paymentMethod,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun OrderItem.toDTO() = OrderItemDTO(
        id = id,
        productId = productId,
        productName = productName,
        price = price,
        quantity = quantity,
        subtotal = subtotal
    )
}

// Data class for product information from Products Service
data class ProductInfo(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val stock: Int
)
