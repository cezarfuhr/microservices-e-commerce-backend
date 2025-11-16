package com.ecommerce.products.controller

import com.ecommerce.products.dto.ProductCreateRequest
import com.ecommerce.products.dto.ProductDTO
import com.ecommerce.products.service.ProductService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal

@WebMvcTest(ProductController::class)
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var productService: ProductService

    @Test
    fun `should get all products`() {
        val products = listOf(
            ProductDTO(1L, "Product 1", "Description", BigDecimal("10.00"), 100, "Electronics"),
            ProductDTO(2L, "Product 2", "Description", BigDecimal("20.00"), 50, "Books")
        )

        every { productService.getAllProducts() } returns products

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Product 1"))

        verify { productService.getAllProducts() }
    }

    @Test
    fun `should get product by id`() {
        val product = ProductDTO(1L, "Product 1", "Description", BigDecimal("10.00"), 100, "Electronics")

        every { productService.getProductById(1L) } returns product

        mockMvc.perform(get("/api/products/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Product 1"))
            .andExpect(jsonPath("$.price").value(10.00))

        verify { productService.getProductById(1L) }
    }

    @Test
    fun `should create product`() {
        val request = ProductCreateRequest(
            name = "New Product",
            description = "Description",
            price = BigDecimal("15.00"),
            stock = 50,
            category = "Electronics"
        )

        val createdProduct = ProductDTO(
            id = 1L,
            name = request.name,
            description = request.description,
            price = request.price,
            stock = request.stock,
            category = request.category
        )

        every { productService.createProduct(request) } returns createdProduct

        mockMvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("New Product"))
            .andExpect(jsonPath("$.price").value(15.00))

        verify { productService.createProduct(request) }
    }

    @Test
    fun `should return bad request when creating product with invalid data`() {
        val invalidRequest = mapOf(
            "name" to "",
            "price" to -10,
            "category" to ""
        )

        mockMvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should search products`() {
        val products = listOf(
            ProductDTO(1L, "Laptop", "Gaming Laptop", BigDecimal("1000.00"), 10, "Electronics")
        )

        every { productService.searchProducts("laptop") } returns products

        mockMvc.perform(get("/api/products/search").param("searchTerm", "laptop"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Laptop"))

        verify { productService.searchProducts("laptop") }
    }
}
