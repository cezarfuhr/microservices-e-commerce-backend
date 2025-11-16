package com.ecommerce.products.exception

class ProductNotFoundException(message: String) : RuntimeException(message)
class InsufficientStockException(message: String) : RuntimeException(message)
class InvalidProductDataException(message: String) : RuntimeException(message)
