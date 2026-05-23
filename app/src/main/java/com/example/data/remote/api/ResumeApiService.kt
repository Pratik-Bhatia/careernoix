package com.example.data.remote.api

import com.example.data.remote.dto.AtsScanResultDto
import okhttp3.MultipartBody
import retrofit2.http.*

interface ResumeApiService {
    @Multipart
    @POST("api/resume/upload")
    suspend fun uploadResume(
        @Part file: MultipartBody.Part,
        @Query("role") role: String
    ): AtsScanResultDto
}
