package com.example.supportstack.data.api

import com.example.supportstack.data.model.ApiResponse
import com.example.supportstack.data.model.RegisterRequest
import com.example.supportstack.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API service interface for SupportStack backend
 */
interface ApiService {
    
    /**
     * Register a new user account
     * POST /api/v1/auth/register
     */
    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<RegisterResponse>>
    
    // Additional endpoints will be added here (login, tickets, etc.)
}
