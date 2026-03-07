package com.example.supportstack.data.model

/**
 * Generic API response wrapper matching backend structure
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ErrorDetail?,
    val timestamp: String
)

data class ErrorDetail(
    val code: String?,
    val message: String,
    val details: Any?
)
