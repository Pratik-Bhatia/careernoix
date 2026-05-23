package com.example.data.remote.api

import com.example.data.remote.dto.InterviewEvaluationDto
import com.example.data.remote.dto.InterviewSubmissionDto
import retrofit2.http.Body
import retrofit2.http.POST

interface InterviewApiService {
    @POST("api/interview/submit")
    suspend fun submitInterview(@Body submission: InterviewSubmissionDto): InterviewEvaluationDto
}
