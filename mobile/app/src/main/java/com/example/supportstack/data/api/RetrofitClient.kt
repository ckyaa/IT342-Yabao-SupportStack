package com.example.supportstack.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object to provide Retrofit instance for API calls
 */
object RetrofitClient {
    
    /**
     * Lazy-initialized Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        // Logging interceptor for debugging (logs HTTP request/response)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // OkHttp client with interceptors and timeouts
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
        
        Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Lazy-initialized API service
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
