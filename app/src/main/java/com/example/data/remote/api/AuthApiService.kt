package com.example.data.remote.api

import com.example.data.remote.dto.LoginRequestDto
import com.example.data.remote.dto.AuthResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto
}
