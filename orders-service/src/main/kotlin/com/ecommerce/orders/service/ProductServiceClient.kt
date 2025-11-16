package com.ecommerce.orders.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject

@Service
class ProductServiceClient(
    private val restTemplate: RestTemplate,
    @Value("\${services.products.url:http://localhost:8081}") private val productsServiceUrl: String
) {
    private val logger = LoggerFactory.getLogger(ProductServiceClient::class.java)

    fun getProduct(productId: Long): ProductInfo {
        logger.info("Fetching product $productId from Products Service")
        return restTemplate.getForObject("$productsServiceUrl/api/products/$productId")
            ?: throw RuntimeException("Product not found")
    }

    fun reserveStock(productId: Long, quantity: Int): Boolean {
        logger.info("Reserving stock for product $productId, quantity: $quantity")
        val response = restTemplate.postForObject<Map<String, Boolean>>(
            "$productsServiceUrl/api/products/$productId/reserve?quantity=$quantity",
            null
        )
        return response?.get("reserved") ?: false
    }
}
