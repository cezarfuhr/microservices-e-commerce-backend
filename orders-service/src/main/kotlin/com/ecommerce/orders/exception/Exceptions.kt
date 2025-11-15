package com.ecommerce.orders.exception

class OrderNotFoundException(message: String) : RuntimeException(message)
class InvalidOrderException(message: String) : RuntimeException(message)
class ProductNotFoundException(message: String) : RuntimeException(message)
class InsufficientStockException(message: String) : RuntimeException(message)
