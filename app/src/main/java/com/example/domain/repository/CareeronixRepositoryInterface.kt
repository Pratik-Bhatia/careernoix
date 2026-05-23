package com.example.domain.repository

import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

interface CareeronixRepositoryInterface {
    val profileFlow: Flow<UserProfileDomain?>
    val resumeHistoryFlow: Flow<List<ResumeRecordDomain>>
    val interviewHistoryFlow: Flow<List<InterviewRecordDomain>>
    val allExperiencesFlow: Flow<List<ExperienceRecordDomain>>

    fun getSkillsForRole(role: String): Flow<List<SkillRecordDomain>>
    suspend fun insertExperience(record: ExperienceRecordDomain): Long
    suspend fun updateExperience(record: ExperienceRecordDomain)
    suspend fun deleteExperienceById(id: Int)
    suspend fun saveProfile(profile: UserProfileDomain)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setLoggedIn(loggedIn: Boolean)
    suspend fun updateXpAndLevel(xp: Int, level: Int)
    suspend fun addResumeScan(record: ResumeRecordDomain)
    suspend fun addInterviewRecord(record: InterviewRecordDomain)
    suspend fun updateSkillProgress(skillId: Int, completed: Boolean, progress: Int)
    suspend fun updateTargetRole(role: String)
    suspend fun populateDefaultsIfEmpty()
}
