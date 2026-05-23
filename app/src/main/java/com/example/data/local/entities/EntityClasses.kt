package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_entity")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val email: String,
    val fullName: String,
    val xp: Int,
    val level: Int,
    val targetRole: String,
    val createdAt: Long = System.currentTimeMillis(),
    val onboardingCompleted: Boolean,
    val isLoggedIn: Boolean,
    val resumeScore: Int,
    val atsScore: Int,
    val jobReadiness: Int,
    val unlockedBadges: String
)

@Entity(tableName = "resume_entity")
data class ResumeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 1,
    val fileName: String,
    val parsedText: String,
    val atsScore: Int,
    val uploadedAt: Long,
    val criticalWeakness: String,
    val suggestions: String,
    val matchingScore: Int
)

@Entity(tableName = "skill_entity")
data class SkillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 1,
    val skillName: String,
    val completed: Boolean,
    val progressPercent: Int,
    val roleName: String
)

@Entity(tableName = "interview_entity")
data class InterviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 1,
    val targetRole: String,
    val timestamp: Long,
    val confidenceScore: Int,
    val communicationScore: Int,
    val technicalScore: Int,
    val overallScore: Int,
    val feedback: String
)

@Entity(tableName = "badge_entity")
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 1,
    val badgeName: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "experience_entity")
data class ExperienceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val company: String,
    val period: String,
    val description: String
)
