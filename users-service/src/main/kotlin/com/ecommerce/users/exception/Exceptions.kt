package com.ecommerce.users.exception

class UserNotFoundException(message: String) : RuntimeException(message)
class UserAlreadyExistsException(message: String) : RuntimeException(message)
class InvalidCredentialsException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String) : RuntimeException(message)
