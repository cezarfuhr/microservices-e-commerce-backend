package com.ecommerce.products.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductDTO(
    val id: Long? = null,

    @field:NotBlank(message = "Product name is required")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val name: String,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    val price: BigDecimal,

    @field:Min(value = 0, message = "Stock cannot be negative")
    val stock: Int = 0,

    @field:NotBlank(message = "Category is required")
    val category: String,

    val imageUrl: String? = null,
    val active: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class ProductCreateRequest(
    @field:NotBlank(message = "Product name is required")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val name: String,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    val price: BigDecimal,

    @field:Min(value = 0, message = "Stock cannot be negative")
    val stock: Int = 0,

    @field:NotBlank(message = "Category is required")
    val category: String,

    val imageUrl: String? = null
)

data class ProductUpdateRequest(
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val name: String? = null,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,

    @field:DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    val price: BigDecimal? = null,

    @field:Min(value = 0, message = "Stock cannot be negative")
    val stock: Int? = null,

    val category: String? = null,
    val imageUrl: String? = null,
    val active: Boolean? = null
)

data class ProductStockUpdate(
    @field:NotNull(message = "Product ID is required")
    val productId: Long,

    @field:NotNull(message = "Quantity is required")
    val quantity: Int
)
