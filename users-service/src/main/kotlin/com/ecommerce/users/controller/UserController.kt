package com.ecommerce.users.controller

import com.ecommerce.users.dto.*
import com.ecommerce.users.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
class UserController(private val userService: UserService) {

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    fun register(@Valid @RequestBody request: UserRegistrationRequest): ResponseEntity<UserDTO> {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request))
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(userService.login(request))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserDTO> {
        return ResponseEntity.ok(userService.getUserById(id))
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<UserDTO> {
        return ResponseEntity.ok(userService.getUserByEmail(email))
    }

    @GetMapping
    @Operation(summary = "Get all active users")
    fun getAllActiveUsers(): ResponseEntity<List<UserDTO>> {
        return ResponseEntity.ok(userService.getAllActiveUsers())
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserDTO> {
        return ResponseEntity.ok(userService.updateUser(id, request))
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Change user password")
    fun changePassword(
        @PathVariable id: Long,
        @Valid @RequestBody request: PasswordChangeRequest
    ): ResponseEntity<Void> {
        userService.changePassword(id, request)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user (soft delete)")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}
