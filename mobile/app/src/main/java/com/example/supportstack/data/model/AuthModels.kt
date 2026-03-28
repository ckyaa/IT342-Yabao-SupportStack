package com.example.supportstack.data.model

/**
 * Response data from registration endpoint
 */
data class RegisterResponse(
    val id: Long,
    val name: String,
    val email: String
)
