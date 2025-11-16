package com.ecommerce.products.controller

import com.ecommerce.products.dto.ProductCreateRequest
import com.ecommerce.products.dto.ProductDTO
import com.ecommerce.products.dto.ProductStockUpdate
import com.ecommerce.products.dto.ProductUpdateRequest
import com.ecommerce.products.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management APIs")
class ProductController(private val productService: ProductService) {

    @GetMapping
    @Operation(summary = "Get all active products")
    fun getAllProducts(): ResponseEntity<List<ProductDTO>> {
        return ResponseEntity.ok(productService.getAllProducts())
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    fun getProductById(@PathVariable id: Long): ResponseEntity<ProductDTO> {
        return ResponseEntity.ok(productService.getProductById(id))
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category")
    fun getProductsByCategory(@PathVariable category: String): ResponseEntity<List<ProductDTO>> {
        return ResponseEntity.ok(productService.getProductsByCategory(category))
    }

    @GetMapping("/search")
    @Operation(summary = "Search products")
    fun searchProducts(@RequestParam searchTerm: String): ResponseEntity<List<ProductDTO>> {
        return ResponseEntity.ok(productService.searchProducts(searchTerm))
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products")
    fun getLowStockProducts(@RequestParam(defaultValue = "10") threshold: Int): ResponseEntity<List<ProductDTO>> {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold))
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    fun createProduct(@Valid @RequestBody request: ProductCreateRequest): ResponseEntity<ProductDTO> {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductUpdateRequest
    ): ResponseEntity<ProductDTO> {
        return ResponseEntity.ok(productService.updateProduct(id, request))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product (soft delete)")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/stock")
    @Operation(summary = "Update product stock")
    fun updateStock(@Valid @RequestBody stockUpdate: ProductStockUpdate): ResponseEntity<ProductDTO> {
        return ResponseEntity.ok(productService.updateStock(stockUpdate))
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Reserve product stock")
    fun reserveStock(
        @PathVariable id: Long,
        @RequestParam quantity: Int
    ): ResponseEntity<Map<String, Boolean>> {
        val reserved = productService.reserveStock(id, quantity)
        return ResponseEntity.ok(mapOf("reserved" to reserved))
    }
}
