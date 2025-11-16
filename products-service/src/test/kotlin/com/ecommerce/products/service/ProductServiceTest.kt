package com.ecommerce.products.service

import com.ecommerce.products.dto.ProductCreateRequest
import com.ecommerce.products.exception.InsufficientStockException
import com.ecommerce.products.exception.ProductNotFoundException
import com.ecommerce.products.model.Product
import com.ecommerce.products.repository.ProductRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

class ProductServiceTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var messagingService: MessagingService
    private lateinit var productService: ProductService

    @BeforeEach
    fun setup() {
        productRepository = mockk()
        messagingService = mockk(relaxed = true)
        productService = ProductService(productRepository, messagingService)
    }

    @Test
    fun `should get all active products`() {
        val products = listOf(
            Product(1L, "Product 1", "Description 1", BigDecimal("10.00"), 100, "Electronics"),
            Product(2L, "Product 2", "Description 2", BigDecimal("20.00"), 50, "Books")
        )

        every { productRepository.findByActiveTrue() } returns products

        val result = productService.getAllProducts()

        assertEquals(2, result.size)
        assertEquals("Product 1", result[0].name)
        verify { productRepository.findByActiveTrue() }
    }

    @Test
    fun `should get product by id`() {
        val product = Product(1L, "Product 1", "Description 1", BigDecimal("10.00"), 100, "Electronics")

        every { productRepository.findById(1L) } returns Optional.of(product)

        val result = productService.getProductById(1L)

        assertEquals("Product 1", result.name)
        assertEquals(BigDecimal("10.00"), result.price)
        verify { productRepository.findById(1L) }
    }

    @Test
    fun `should throw exception when product not found`() {
        every { productRepository.findById(999L) } returns Optional.empty()

        assertThrows<ProductNotFoundException> {
            productService.getProductById(999L)
        }

        verify { productRepository.findById(999L) }
    }

    @Test
    fun `should create product`() {
        val request = ProductCreateRequest(
            name = "New Product",
            description = "New Description",
            price = BigDecimal("15.00"),
            stock = 50,
            category = "Electronics"
        )

        val savedProduct = Product(
            id = 1L,
            name = request.name,
            description = request.description,
            price = request.price,
            stock = request.stock,
            category = request.category
        )

        every { productRepository.save(any()) } returns savedProduct

        val result = productService.createProduct(request)

        assertEquals("New Product", result.name)
        assertEquals(BigDecimal("15.00"), result.price)
        verify { productRepository.save(any()) }
        verify { messagingService.publishProductCreated(savedProduct) }
    }

    @Test
    fun `should reserve stock successfully`() {
        val product = Product(1L, "Product 1", "Description", BigDecimal("10.00"), 100, "Electronics")

        every { productRepository.findById(1L) } returns Optional.of(product)
        every { productRepository.save(any()) } returns product

        val result = productService.reserveStock(1L, 10)

        assertTrue(result)
        verify { productRepository.save(any()) }
    }

    @Test
    fun `should not reserve stock when insufficient`() {
        val product = Product(1L, "Product 1", "Description", BigDecimal("10.00"), 5, "Electronics")

        every { productRepository.findById(1L) } returns Optional.of(product)

        val result = productService.reserveStock(1L, 10)

        assertFalse(result)
        verify(exactly = 0) { productRepository.save(any()) }
    }

    @Test
    fun `should search products`() {
        val products = listOf(
            Product(1L, "Laptop", "Gaming Laptop", BigDecimal("1000.00"), 10, "Electronics")
        )

        every { productRepository.searchProducts("laptop") } returns products

        val result = productService.searchProducts("laptop")

        assertEquals(1, result.size)
        assertEquals("Laptop", result[0].name)
        verify { productRepository.searchProducts("laptop") }
    }
}
