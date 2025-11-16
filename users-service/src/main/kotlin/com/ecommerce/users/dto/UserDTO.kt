package com.ecommerce.users.dto

import com.ecommerce.users.model.Address
import com.ecommerce.users.model.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UserDTO(
    val id: Long?,
    val email: String,
    val fullName: String,
    val phone: String?,
    val address: Address?,
    val role: UserRole,
    val active: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

data class UserRegistrationRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,

    @field:NotBlank(message = "Full name is required")
    val fullName: String,

    val phone: String? = null,
    val address: Address? = null
)

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserDTO
)

data class UserUpdateRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val address: Address? = null
)

data class PasswordChangeRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val newPassword: String
)
