package com.example.supportstack.data.model

/**
 * Request body for user registration
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

/**
 * Response data from registration endpoint
 */
data class RegisterResponse(
    val id: Long,
    val name: String,
    val email: String
)
