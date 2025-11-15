package com.ecommerce.users.service

import com.ecommerce.users.dto.*
import com.ecommerce.users.exception.InvalidCredentialsException
import com.ecommerce.users.exception.UserAlreadyExistsException
import com.ecommerce.users.exception.UserNotFoundException
import com.ecommerce.users.model.User
import com.ecommerce.users.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val messagingService: MessagingService
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    @Transactional
    fun registerUser(request: UserRegistrationRequest): UserDTO {
        logger.info("Registering new user: ${request.email}")

        if (userRepository.existsByEmail(request.email)) {
            throw UserAlreadyExistsException("User already exists with email: ${request.email}")
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            phone = request.phone,
            address = request.address
        )

        val savedUser = userRepository.save(user)

        // Send event to RabbitMQ
        messagingService.publishUserRegistered(savedUser)

        return savedUser.toDTO()
    }

    fun login(request: LoginRequest): LoginResponse {
        logger.info("User login attempt: ${request.email}")

        val user = userRepository.findByEmail(request.email)
            .orElseThrow { InvalidCredentialsException("Invalid email or password") }

        if (!user.active) {
            throw InvalidCredentialsException("User account is inactive")
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidCredentialsException("Invalid email or password")
        }

        val token = jwtService.generateToken(user)

        return LoginResponse(token = token, user = user.toDTO())
    }

    fun getUserById(id: Long): UserDTO {
        logger.info("Fetching user with id: $id")
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User not found with id: $id") }
        return user.toDTO()
    }

    fun getUserByEmail(email: String): UserDTO {
        logger.info("Fetching user with email: $email")
        val user = userRepository.findByEmail(email)
            .orElseThrow { UserNotFoundException("User not found with email: $email") }
        return user.toDTO()
    }

    fun getAllActiveUsers(): List<UserDTO> {
        logger.info("Fetching all active users")
        return userRepository.findByActiveTrue().map { it.toDTO() }
    }

    @Transactional
    fun updateUser(id: Long, request: UserUpdateRequest): UserDTO {
        logger.info("Updating user with id: $id")
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User not found with id: $id") }

        request.fullName?.let { user.fullName = it }
        request.phone?.let { user.phone = it }
        request.address?.let { user.address = it }

        val updatedUser = userRepository.save(user)

        // Send event to RabbitMQ
        messagingService.publishUserUpdated(updatedUser)

        return updatedUser.toDTO()
    }

    @Transactional
    fun changePassword(id: Long, request: PasswordChangeRequest) {
        logger.info("Changing password for user with id: $id")
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User not found with id: $id") }

        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            throw InvalidCredentialsException("Current password is incorrect")
        }

        user.password = passwordEncoder.encode(request.newPassword)
        userRepository.save(user)
    }

    @Transactional
    fun deleteUser(id: Long) {
        logger.info("Deleting user with id: $id")
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User not found with id: $id") }

        user.active = false
        userRepository.save(user)

        // Send event to RabbitMQ
        messagingService.publishUserDeleted(user)
    }

    private fun User.toDTO() = UserDTO(
        id = id,
        email = email,
        fullName = fullName,
        phone = phone,
        address = address,
        role = role,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
