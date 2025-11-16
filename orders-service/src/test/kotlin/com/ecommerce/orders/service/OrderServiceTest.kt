package com.ecommerce.orders.service

import com.ecommerce.orders.dto.CreateOrderItemRequest
import com.ecommerce.orders.dto.CreateOrderRequest
import com.ecommerce.orders.dto.UpdateOrderStatusRequest
import com.ecommerce.orders.exception.InsufficientStockException
import com.ecommerce.orders.exception.InvalidOrderException
import com.ecommerce.orders.exception.OrderNotFoundException
import com.ecommerce.orders.model.Order
import com.ecommerce.orders.model.OrderStatus
import com.ecommerce.orders.repository.OrderRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

class OrderServiceTest {

    private lateinit var orderRepository: OrderRepository
    private lateinit var productServiceClient: ProductServiceClient
    private lateinit var messagingService: MessagingService
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setup() {
        orderRepository = mockk()
        productServiceClient = mockk()
        messagingService = mockk(relaxed = true)
        orderService = OrderService(orderRepository, productServiceClient, messagingService)
    }

    @Test
    fun `should get all orders`() {
        val orders = listOf(
            Order(id = 1L, userId = 1L),
            Order(id = 2L, userId = 2L)
        )

        every { orderRepository.findAll() } returns orders

        val result = orderService.getAllOrders()

        assertEquals(2, result.size)
        verify { orderRepository.findAll() }
    }

    @Test
    fun `should get order by id`() {
        val order = Order(id = 1L, userId = 1L, status = OrderStatus.CONFIRMED)

        every { orderRepository.findById(1L) } returns Optional.of(order)

        val result = orderService.getOrderById(1L)

        assertEquals(1L, result.id)
        assertEquals(OrderStatus.CONFIRMED, result.status)
    }

    @Test
    fun `should throw exception when order not found`() {
        every { orderRepository.findById(999L) } returns Optional.empty()

        assertThrows<OrderNotFoundException> {
            orderService.getOrderById(999L)
        }
    }

    @Test
    fun `should get orders by user id`() {
        val orders = listOf(
            Order(id = 1L, userId = 1L),
            Order(id = 2L, userId = 1L)
        )

        every { orderRepository.findByUserId(1L) } returns orders

        val result = orderService.getOrdersByUserId(1L)

        assertEquals(2, result.size)
        assertTrue(result.all { it.userId == 1L })
    }

    @Test
    fun `should create order successfully`() {
        val product = ProductInfo(
            id = 1L,
            name = "Test Product",
            price = BigDecimal("99.99"),
            stock = 10
        )

        val request = CreateOrderRequest(
            userId = 1L,
            items = listOf(CreateOrderItemRequest(productId = 1L, quantity = 2)),
            shippingAddress = "123 Main St",
            paymentMethod = "credit_card"
        )

        every { productServiceClient.getProduct(1L) } returns product
        every { productServiceClient.reserveStock(1L, 2) } returns true
        every { orderRepository.save(any()) } answers { firstArg<Order>().copy(id = 1L) }

        val result = orderService.createOrder(request)

        assertEquals(1L, result.userId)
        assertEquals(OrderStatus.CONFIRMED, result.status)
        assertEquals(1, result.items.size)
        verify { messagingService.publishOrderCreated(any()) }
    }

    @Test
    fun `should throw exception when creating order with empty items`() {
        val request = CreateOrderRequest(
            userId = 1L,
            items = emptyList(),
            shippingAddress = "123 Main St"
        )

        assertThrows<InvalidOrderException> {
            orderService.createOrder(request)
        }

        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `should throw exception when product has insufficient stock`() {
        val product = ProductInfo(
            id = 1L,
            name = "Test Product",
            price = BigDecimal("99.99"),
            stock = 1
        )

        val request = CreateOrderRequest(
            userId = 1L,
            items = listOf(CreateOrderItemRequest(productId = 1L, quantity = 5))
        )

        every { productServiceClient.getProduct(1L) } returns product

        assertThrows<InsufficientStockException> {
            orderService.createOrder(request)
        }

        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `should update order status`() {
        val order = Order(id = 1L, userId = 1L, status = OrderStatus.CONFIRMED)

        every { orderRepository.findById(1L) } returns Optional.of(order)
        every { orderRepository.save(any()) } answers { firstArg() }

        val request = UpdateOrderStatusRequest(status = OrderStatus.SHIPPED)
        val result = orderService.updateOrderStatus(1L, request)

        assertEquals(OrderStatus.SHIPPED, result.status)
        verify { messagingService.publishOrderStatusUpdated(any(), OrderStatus.CONFIRMED) }
    }

    @Test
    fun `should cancel order successfully`() {
        val order = Order(id = 1L, userId = 1L, status = OrderStatus.CONFIRMED)

        every { orderRepository.findById(1L) } returns Optional.of(order)
        every { orderRepository.save(any()) } answers { firstArg() }

        orderService.cancelOrder(1L)

        verify { orderRepository.save(match { it.status == OrderStatus.CANCELLED }) }
        verify { messagingService.publishOrderCancelled(any()) }
    }

    @Test
    fun `should throw exception when cancelling shipped order`() {
        val order = Order(id = 1L, userId = 1L, status = OrderStatus.SHIPPED)

        every { orderRepository.findById(1L) } returns Optional.of(order)

        assertThrows<InvalidOrderException> {
            orderService.cancelOrder(1L)
        }

        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `should get orders by status`() {
        val orders = listOf(
            Order(id = 1L, userId = 1L, status = OrderStatus.PENDING),
            Order(id = 2L, userId = 2L, status = OrderStatus.PENDING)
        )

        every { orderRepository.findByStatus(OrderStatus.PENDING) } returns orders

        val result = orderService.getOrdersByStatus(OrderStatus.PENDING)

        assertEquals(2, result.size)
        assertTrue(result.all { it.status == OrderStatus.PENDING })
    }
}
