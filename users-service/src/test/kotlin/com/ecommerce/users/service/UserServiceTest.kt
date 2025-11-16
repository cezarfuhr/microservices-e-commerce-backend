package com.ecommerce.users.service

import com.ecommerce.users.dto.LoginRequest
import com.ecommerce.users.dto.PasswordChangeRequest
import com.ecommerce.users.dto.UserRegistrationRequest
import com.ecommerce.users.dto.UserUpdateRequest
import com.ecommerce.users.exception.InvalidCredentialsException
import com.ecommerce.users.exception.UserAlreadyExistsException
import com.ecommerce.users.exception.UserNotFoundException
import com.ecommerce.users.model.User
import com.ecommerce.users.repository.UserRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtService: JwtService
    private lateinit var messagingService: MessagingService
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        passwordEncoder = mockk()
        jwtService = mockk()
        messagingService = mockk(relaxed = true)
        userService = UserService(userRepository, passwordEncoder, jwtService, messagingService)
    }

    @Test
    fun `should register new user successfully`() {
        val request = UserRegistrationRequest(
            email = "test@example.com",
            password = "password123",
            fullName = "Test User"
        )

        every { userRepository.existsByEmail(request.email) } returns false
        every { passwordEncoder.encode(request.password) } returns "hashedPassword"
        every { userRepository.save(any()) } answers { firstArg() }

        val result = userService.registerUser(request)

        assertEquals("test@example.com", result.email)
        assertEquals("Test User", result.fullName)
        verify { userRepository.save(any()) }
        verify { messagingService.publishUserRegistered(any()) }
    }

    @Test
    fun `should throw exception when user already exists`() {
        val request = UserRegistrationRequest(
            email = "existing@example.com",
            password = "password123",
            fullName = "Test User"
        )

        every { userRepository.existsByEmail(request.email) } returns true

        assertThrows<UserAlreadyExistsException> {
            userService.registerUser(request)
        }

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should login successfully with correct credentials`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        val user = User(
            id = 1L,
            email = "test@example.com",
            password = "hashedPassword",
            fullName = "Test User"
        )

        every { userRepository.findByEmail(request.email) } returns Optional.of(user)
        every { passwordEncoder.matches(request.password, user.password) } returns true
        every { jwtService.generateToken(user) } returns "jwt-token"

        val result = userService.login(request)

        assertEquals("jwt-token", result.token)
        assertEquals("test@example.com", result.user.email)
    }

    @Test
    fun `should throw exception on login with invalid password`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        val user = User(
            id = 1L,
            email = "test@example.com",
            password = "hashedPassword",
            fullName = "Test User"
        )

        every { userRepository.findByEmail(request.email) } returns Optional.of(user)
        every { passwordEncoder.matches(request.password, user.password) } returns false

        assertThrows<InvalidCredentialsException> {
            userService.login(request)
        }
    }

    @Test
    fun `should get user by id`() {
        val user = User(
            id = 1L,
            email = "test@example.com",
            password = "hashedPassword",
            fullName = "Test User"
        )

        every { userRepository.findById(1L) } returns Optional.of(user)

        val result = userService.getUserById(1L)

        assertEquals("test@example.com", result.email)
        assertEquals("Test User", result.fullName)
    }

    @Test
    fun `should throw exception when user not found`() {
        every { userRepository.findById(999L) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            userService.getUserById(999L)
        }
    }

    @Test
    fun `should update user successfully`() {
        val user = User(
            id = 1L,
            email = "test@example.com",
            password = "hashedPassword",
            fullName = "Old Name"
        )

        val updateRequest = UserUpdateRequest(
            fullName = "New Name",
            phone = "1234567890"
        )

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { firstArg() }

        val result = userService.updateUser(1L, updateRequest)

        assertEquals("New Name", result.fullName)
        assertEquals("1234567890", result.phone)
        verify { messagingService.publishUserUpdated(any()) }
    }

    @Test
    fun `should change password successfully`() {
        val user = User(
            id = 1L,
            email = "test@example.com",
            password = "oldHashedPassword",
            fullName = "Test User"
        )

        val changeRequest = PasswordChangeRequest(
            currentPassword = "oldPassword",
            newPassword = "newPassword"
        )

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { passwordEncoder.matches("oldPassword", user.password) } returns true
        every { passwordEncoder.encode("newPassword") } returns "newHashedPassword"
        every { userRepository.save(any()) } answers { firstArg() }

        userService.changePassword(1L, changeRequest)

        verify { userRepository.save(match { it.password == "newHashedPassword" }) }
    }

    @Test
    fun `should throw exception when changing password with wrong current password`() {
        val user = User(
            id = 1L,
            email = "test@example.com",
            password = "hashedPassword",
            fullName = "Test User"
        )

        val changeRequest = PasswordChangeRequest(
            currentPassword = "wrongPassword",
            newPassword = "newPassword"
        )

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { passwordEncoder.matches("wrongPassword", user.password) } returns false

        assertThrows<InvalidCredentialsException> {
            userService.changePassword(1L, changeRequest)
        }

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should delete user (soft delete)`() {
        val user = User(
            id = 1L,
            email = "test@example.com",
            password = "hashedPassword",
            fullName = "Test User",
            active = true
        )

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { firstArg() }

        userService.deleteUser(1L)

        verify { userRepository.save(match { !it.active }) }
        verify { messagingService.publishUserDeleted(any()) }
    }
}
