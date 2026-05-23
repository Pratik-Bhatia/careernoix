package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_entity WHERE id = 1")
    fun observeUser(): Flow<UserEntity?>

    @Query("SELECT * FROM user_entity WHERE id = 1")
    suspend fun getUserDirect(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE user_entity SET onboardingCompleted = :completed WHERE id = 1")
    suspend fun updateOnboarding(completed: Boolean)

    @Query("UPDATE user_entity SET isLoggedIn = :loggedIn WHERE id = 1")
    suspend fun updateLogin(loggedIn: Boolean)

    @Query("UPDATE user_entity SET xp = :xp, level = :level WHERE id = 1")
    suspend fun updateXpAndLevel(xp: Int, level: Int)

    @Query("UPDATE user_entity SET targetRole = :role WHERE id = 1")
    suspend fun updateTargetRole(role: String)

    @Transaction
    suspend fun upgradeUserLevel(xpToAdd: Int, pointsPerLevel: Int) {
        val user = getUserDirect() ?: return
        val nextXp = user.xp + xpToAdd
        val nextLevel = (nextXp / pointsPerLevel) + 1
        updateXpAndLevel(nextXp, nextLevel)
    }
}

@Dao
interface ResumeDao {
    @Query("SELECT * FROM resume_entity ORDER BY uploadedAt DESC")
    fun observeAllResumes(): Flow<List<ResumeEntity>>

    @Query("SELECT * FROM resume_entity WHERE id = :id")
    suspend fun getResumeById(id: Int): ResumeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: ResumeEntity): Long

    @Delete
    suspend fun deleteResume(resume: ResumeEntity)

    @Query("DELETE FROM resume_entity")
    suspend fun clearAll()
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skill_entity WHERE roleName = :role")
    fun observeSkillsForRole(role: String): Flow<List<SkillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Query("UPDATE skill_entity SET completed = :completed, progressPercent = :progress WHERE id = :id")
    suspend fun updateSkillProgress(id: Int, completed: Boolean, progress: Int)

    @Query("DELETE FROM skill_entity")
    suspend fun clearAll()
}

@Dao
interface InterviewDao {
    @Query("SELECT * FROM interview_entity ORDER BY timestamp DESC")
    fun observeAllInterviews(): Flow<List<InterviewEntity>>

    @Query("SELECT * FROM interview_entity WHERE id = :id")
    suspend fun getInterviewById(id: Int): InterviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterview(interview: InterviewEntity)

    @Query("DELETE FROM interview_entity")
    suspend fun clearAll()
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badge_entity ORDER BY unlockedAt DESC")
    fun observeAllBadges(): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeEntity)

    @Query("DELETE FROM badge_entity")
    suspend fun clearAll()
}

@Dao
interface ExperienceEntityDao {
    @Query("SELECT * FROM experience_entity ORDER BY id DESC")
    fun observeAllExperiences(): Flow<List<ExperienceEntity>>

    @Query("SELECT * FROM experience_entity ORDER BY id DESC")
    fun getExperiencesSync(): List<ExperienceEntity>

    @Query("SELECT * FROM experience_entity WHERE id = :id")
    suspend fun getExperienceById(id: Int): ExperienceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperience(rec: ExperienceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExperienceSync(rec: ExperienceEntity): Long

    @Update
    suspend fun updateExperience(rec: ExperienceEntity)

    @Update
    fun updateExperienceSync(rec: ExperienceEntity)

    @Query("DELETE FROM experience_entity WHERE id = :id")
    suspend fun deleteExperienceById(id: Int)

    @Query("DELETE FROM experience_entity WHERE id = :id")
    fun deleteExperienceByIdSync(id: Int)
}
