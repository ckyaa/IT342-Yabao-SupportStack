package com.example.supportstack.data.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)
