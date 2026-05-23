package com.example.domain.model

data class UserProfileDomain(
    val id: Int = 1,
    val name: String,
    val email: String,
    val targetRole: String,
    val onboardingCompleted: Boolean,
    val isLoggedIn: Boolean,
    val resumeScore: Int,
    val atsScore: Int,
    val jobReadiness: Int,
    val xp: Int,
    val level: Int,
    val unlockedBadges: String
)

data class ResumeRecordDomain(
    val id: Int = 0,
    val fileName: String,
    val timestamp: Long,
    val atsScore: Int,
    val criticalWeakness: String,
    val suggestions: String,
    val matchingScore: Int
)

data class InterviewRecordDomain(
    val id: Int = 0,
    val role: String,
    val timestamp: Long,
    val confidenceScore: Int,
    val communicationScore: Int,
    val technicalScore: Int,
    val overallScore: Int,
    val feedback: String
)

data class SkillRecordDomain(
    val id: Int = 0,
    val roleName: String,
    val skillName: String,
    val isCompleted: Boolean,
    val progressPercent: Int
)

data class ExperienceRecordDomain(
    val id: Int = 0,
    val title: String,
    val company: String,
    val period: String,
    val description: String
)

data class BadgeRecordDomain(
    val id: Int = 0,
    val userId: Int = 1,
    val badgeName: String,
    val unlockedAt: Long
)
