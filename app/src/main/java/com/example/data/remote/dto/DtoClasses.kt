package com.example.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val name: String
)

data class AuthResponseDto(
    val token: String,
    val name: String,
    val email: String,
    val targetRole: String,
    val level: Int,
    val xp: Int,
    val jobReadiness: Int
)

data class ExperienceEntryDto(
    val id: Int,
    val title: String,
    val company: String,
    val period: String,
    val description: String
)

data class AtsScanResultDto(
    val atsScore: Int,
    val matchedSkills: List<String>,
    val missingSkills: List<String>,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val suggestions: List<String>
)

data class InterviewEvaluationDto(
    val overallScore: Int,
    val confidenceScore: Int,
    val communicationScore: Int,
    val technicalScore: Int,
    val feedback: String
)

data class InterviewSubmissionDto(
    val role: String,
    val question: String,
    val answer: String
)
