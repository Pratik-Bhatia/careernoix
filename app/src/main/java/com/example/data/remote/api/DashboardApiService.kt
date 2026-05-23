package com.example.data.remote.api

import com.example.data.remote.dto.ExperienceEntryDto
import okhttp3.ResponseBody
import retrofit2.http.*

interface DashboardApiService {
    @GET("api/experience")
    suspend fun getExperiences(): List<ExperienceEntryDto>

    @POST("api/experience")
    suspend fun addExperience(@Body entry: ExperienceEntryDto): ExperienceEntryDto

    @PUT("api/experience/{id}")
    suspend fun updateExperience(@Path("id") id: Int, @Body entry: ExperienceEntryDto): ExperienceEntryDto

    @DELETE("api/experience/{id}")
    suspend fun deleteExperience(@Path("id") id: Int): ResponseBody
}
