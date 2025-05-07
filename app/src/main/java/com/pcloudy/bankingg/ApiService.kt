package com.pcloudy.bankingg

import com.android.volley.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/biometric-login")
    suspend fun biometricLogin(): Response<LoginResponse>
}