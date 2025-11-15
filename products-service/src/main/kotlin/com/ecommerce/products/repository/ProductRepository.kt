package com.ecommerce.products.repository

import com.ecommerce.products.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByCategory(category: String): List<Product>
    fun findByActiveTrue(): List<Product>
    fun findByCategoryAndActiveTrue(category: String): List<Product>

    @Query("SELECT p FROM Product p WHERE p.stock <= :threshold AND p.active = true")
    fun findLowStockProducts(threshold: Int = 10): List<Product>

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    fun searchProducts(searchTerm: String): List<Product>
}
