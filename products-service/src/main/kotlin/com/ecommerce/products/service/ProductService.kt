package com.ecommerce.products.service

import com.ecommerce.products.dto.ProductCreateRequest
import com.ecommerce.products.dto.ProductDTO
import com.ecommerce.products.dto.ProductStockUpdate
import com.ecommerce.products.dto.ProductUpdateRequest
import com.ecommerce.products.exception.InsufficientStockException
import com.ecommerce.products.exception.ProductNotFoundException
import com.ecommerce.products.model.Product
import com.ecommerce.products.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val messagingService: MessagingService
) {
    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    @Cacheable("products")
    fun getAllProducts(): List<ProductDTO> {
        logger.info("Fetching all products")
        return productRepository.findByActiveTrue().map { it.toDTO() }
    }

    @Cacheable("products", key = "#id")
    fun getProductById(id: Long): ProductDTO {
        logger.info("Fetching product with id: $id")
        val product = productRepository.findById(id)
            .orElseThrow { ProductNotFoundException("Product not found with id: $id") }
        return product.toDTO()
    }

    fun getProductsByCategory(category: String): List<ProductDTO> {
        logger.info("Fetching products by category: $category")
        return productRepository.findByCategoryAndActiveTrue(category).map { it.toDTO() }
    }

    fun searchProducts(searchTerm: String): List<ProductDTO> {
        logger.info("Searching products with term: $searchTerm")
        return productRepository.searchProducts(searchTerm).map { it.toDTO() }
    }

    fun getLowStockProducts(threshold: Int = 10): List<ProductDTO> {
        logger.info("Fetching low stock products with threshold: $threshold")
        return productRepository.findLowStockProducts(threshold).map { it.toDTO() }
    }

    @Transactional
    @CacheEvict("products", allEntries = true)
    fun createProduct(request: ProductCreateRequest): ProductDTO {
        logger.info("Creating new product: ${request.name}")
        val product = Product(
            name = request.name,
            description = request.description,
            price = request.price,
            stock = request.stock,
            category = request.category,
            imageUrl = request.imageUrl
        )
        val savedProduct = productRepository.save(product)

        // Send event to RabbitMQ
        messagingService.publishProductCreated(savedProduct)

        return savedProduct.toDTO()
    }

    @Transactional
    @CacheEvict("products", allEntries = true)
    fun updateProduct(id: Long, request: ProductUpdateRequest): ProductDTO {
        logger.info("Updating product with id: $id")
        val product = productRepository.findById(id)
            .orElseThrow { ProductNotFoundException("Product not found with id: $id") }

        request.name?.let { product.name = it }
        request.description?.let { product.description = it }
        request.price?.let { product.price = it }
        request.stock?.let { product.stock = it }
        request.category?.let { product.category = it }
        request.imageUrl?.let { product.imageUrl = it }
        request.active?.let { product.active = it }

        val updatedProduct = productRepository.save(product)

        // Send event to RabbitMQ
        messagingService.publishProductUpdated(updatedProduct)

        return updatedProduct.toDTO()
    }

    @Transactional
    @CacheEvict("products", allEntries = true)
    fun deleteProduct(id: Long) {
        logger.info("Deleting product with id: $id")
        val product = productRepository.findById(id)
            .orElseThrow { ProductNotFoundException("Product not found with id: $id") }

        product.active = false
        productRepository.save(product)

        // Send event to RabbitMQ
        messagingService.publishProductDeleted(product)
    }

    @Transactional
    @CacheEvict("products", allEntries = true)
    fun updateStock(stockUpdate: ProductStockUpdate): ProductDTO {
        logger.info("Updating stock for product: ${stockUpdate.productId}, quantity: ${stockUpdate.quantity}")
        val product = productRepository.findById(stockUpdate.productId)
            .orElseThrow { ProductNotFoundException("Product not found with id: ${stockUpdate.productId}") }

        val newStock = product.stock + stockUpdate.quantity

        if (newStock < 0) {
            throw InsufficientStockException("Insufficient stock for product: ${product.name}. Available: ${product.stock}, Requested: ${-stockUpdate.quantity}")
        }

        product.stock = newStock
        val updatedProduct = productRepository.save(product)

        // Send event to RabbitMQ
        messagingService.publishStockUpdated(updatedProduct)

        return updatedProduct.toDTO()
    }

    @Transactional
    fun reserveStock(productId: Long, quantity: Int): Boolean {
        logger.info("Reserving stock for product: $productId, quantity: $quantity")
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException("Product not found with id: $productId") }

        if (product.stock < quantity) {
            logger.warn("Insufficient stock for product: $productId. Available: ${product.stock}, Requested: $quantity")
            return false
        }

        product.stock -= quantity
        productRepository.save(product)
        return true
    }

    private fun Product.toDTO() = ProductDTO(
        id = id,
        name = name,
        description = description,
        price = price,
        stock = stock,
        category = category,
        imageUrl = imageUrl,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
