package com.ecommerce.users.service

import com.ecommerce.users.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {

    @Value("\${jwt.secret:my-secret-key-for-jwt-authentication-that-is-at-least-256-bits-long}")
    private lateinit var secret: String

    @Value("\${jwt.expiration:86400000}") // 24 hours
    private var expiration: Long = 86400000

    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(user: User): String {
        val claims = mutableMapOf<String, Any>()
        claims["userId"] = user.id!!
        claims["email"] = user.email
        claims["role"] = user.role.name

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.email)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun extractEmail(token: String): String {
        return extractClaims(token).subject
    }

    fun extractUserId(token: String): Long {
        return extractClaims(token)["userId"].toString().toLong()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = extractClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun extractClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
    }
}
