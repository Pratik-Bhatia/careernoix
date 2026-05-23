package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// --- ROOM ENTITIES ---

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
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
    val unlockedBadges: String // Comma separated: e.g. "Resume Pro,Interview Champ"
)

@Entity(tableName = "resume_records")
data class ResumeRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val timestamp: Long,
    val atsScore: Int,
    val criticalWeakness: String,
    val suggestions: String, // Comma separated suggestions
    val matchingScore: Int
)

@Entity(tableName = "interview_records")
data class InterviewRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String,
    val timestamp: Long,
    val confidenceScore: Int,
    val communicationScore: Int,
    val technicalScore: Int,
    val overallScore: Int,
    val feedback: String
)

@Entity(tableName = "skill_records")
data class SkillRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roleName: String,
    val skillName: String,
    val isCompleted: Boolean,
    val progressPercent: Int
)

@Entity(tableName = "experience_records")
data class ExperienceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val company: String,
    val period: String,
    val description: String
)


// --- ROOM DAOS ---

@Dao
interface ExperienceDao {
    @Query("SELECT * FROM experience_records ORDER BY id DESC")
    fun getAllExperiencesFlow(): Flow<List<ExperienceRecord>>

    @Query("SELECT * FROM experience_records ORDER BY id DESC")
    fun getExperiencesSync(): List<ExperienceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperience(record: ExperienceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExperienceSync(record: ExperienceRecord): Long

    @Update
    suspend fun updateExperience(record: ExperienceRecord)

    @Update
    fun updateExperienceSync(record: ExperienceRecord)

    @Query("DELETE FROM experience_records WHERE id = :id")
    suspend fun deleteExperienceById(id: Int)

    @Query("DELETE FROM experience_records WHERE id = :id")
    fun deleteExperienceByIdSync(id: Int)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)

    @Query("UPDATE user_profile SET onboardingCompleted = :completed WHERE id = 1")
    suspend fun updateOnboarding(completed: Boolean)

    @Query("UPDATE user_profile SET isLoggedIn = :loggedIn WHERE id = 1")
    suspend fun updateLogin(loggedIn: Boolean)

    @Query("UPDATE user_profile SET xp = :newXp, level = :newLevel WHERE id = 1")
    suspend fun updateXpAndLevel(newXp: Int, newLevel: Int)

    @Query("UPDATE user_profile SET targetRole = :role WHERE id = 1")
    suspend fun updateTargetRole(role: String)
}

@Dao
interface ResumeRecordDao {
    @Query("SELECT * FROM resume_records ORDER BY timestamp DESC")
    fun getAllResumes(): Flow<List<ResumeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(record: ResumeRecord)

    @Query("DELETE FROM resume_records")
    suspend fun clearAll()
}

@Dao
interface InterviewRecordDao {
    @Query("SELECT * FROM interview_records ORDER BY timestamp DESC")
    fun getAllInterviews(): Flow<List<InterviewRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterview(record: InterviewRecord)

    @Query("DELETE FROM interview_records")
    suspend fun clearAll()
}

@Dao
interface SkillRecordDao {
    @Query("SELECT * FROM skill_records WHERE roleName = :role")
    fun getSkillsForRole(role: String): Flow<List<SkillRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillRecord>)

    @Query("UPDATE skill_records SET isCompleted = :completed, progressPercent = :progress WHERE id = :id")
    suspend fun updateSkillProgress(id: Int, completed: Boolean, progress: Int)
}


// --- ROOM DATABASE ---

@Database(
    entities = [UserProfile::class, ResumeRecord::class, InterviewRecord::class, SkillRecord::class, ExperienceRecord::class],
    version = 3,
    exportSchema = false
)
abstract class CareeronixDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun resumeRecordDao(): ResumeRecordDao
    abstract fun interviewRecordDao(): InterviewRecordDao
    abstract fun skillRecordDao(): SkillRecordDao
    abstract fun experienceDao(): ExperienceDao

    companion object {
        @Volatile
        private var INSTANCE: CareeronixDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): CareeronixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CareeronixDatabase::class.java,
                    "careeronix_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun resetDatabase(context: Context) {
            synchronized(this) {
                try {
                    INSTANCE?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                INSTANCE = null
                context.deleteDatabase("careeronix_database")
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
    }
}


// --- REPOSITORY IMPLEMENTATION ---

class CareeronixRepository(
    private val db: CareeronixDatabase
) {
    val profileFlow: Flow<UserProfile?> = db.userProfileDao().getProfile()
    val resumeHistoryFlow: Flow<List<ResumeRecord>> = db.resumeRecordDao().getAllResumes()
    val interviewHistoryFlow: Flow<List<InterviewRecord>> = db.interviewRecordDao().getAllInterviews()
    val allExperiencesFlow: Flow<List<ExperienceRecord>> = db.experienceDao().getAllExperiencesFlow()

    fun getSkillsForRole(role: String): Flow<List<SkillRecord>> = db.skillRecordDao().getSkillsForRole(role)

    suspend fun insertExperience(record: ExperienceRecord): Long = db.experienceDao().insertExperience(record)
    suspend fun updateExperience(record: ExperienceRecord) = db.experienceDao().updateExperience(record)
    suspend fun deleteExperienceById(id: Int) = db.experienceDao().deleteExperienceById(id)

    suspend fun saveProfile(profile: UserProfile) {
        db.userProfileDao().insertOrUpdate(profile)
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        db.userProfileDao().updateOnboarding(completed)
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        db.userProfileDao().updateLogin(loggedIn)
    }

    suspend fun updateXpAndLevel(xp: Int, level: Int) {
        db.userProfileDao().updateXpAndLevel(xp, level)
    }

    suspend fun addResumeScan(record: ResumeRecord) {
        db.resumeRecordDao().insertResume(record)
    }

    suspend fun addInterviewRecord(record: InterviewRecord) {
        db.interviewRecordDao().insertInterview(record)
    }

    suspend fun updateSkillProgress(skillId: Int, completed: Boolean, progress: Int) {
        db.skillRecordDao().updateSkillProgress(skillId, completed, progress)
    }

    suspend fun updateTargetRole(role: String) {
         db.userProfileDao().updateTargetRole(role)
    }

    suspend fun populateDefaultsIfEmpty() {
        if (db.userProfileDao().getProfileDirect() == null) {
            val userProfileDao = db.userProfileDao()
            val resumeDao = db.resumeRecordDao()
            val interviewDao = db.interviewRecordDao()
            val skillDao = db.skillRecordDao()

            // Seed user profile
            userProfileDao.insertOrUpdate(
                UserProfile(
                    id = 1,
                    name = "Pratik Bhatia",
                    email = "pratikbhatiahp@gmail.com",
                    targetRole = "Frontend Developer",
                    onboardingCompleted = false,
                    isLoggedIn = false,
                    resumeScore = 72,
                    atsScore = 65,
                    jobReadiness = 58,
                    xp = 240,
                    level = 1,
                    unlockedBadges = "Early Starter,First Mock"
                )
            )

            // Seed default resume scan records
            resumeDao.insertResume(
                ResumeRecord(
                    fileName = "Pratik_Bhatia_Resume_v1.pdf",
                    timestamp = System.currentTimeMillis() - 86400000 * 2,
                    atsScore = 65,
                    criticalWeakness = "Missing essential JavaScript keywords (Promises, Async/Await), sparse description of college project impacts.",
                    suggestions = "Add Async/Await,List metrics like page performance,Convert paragraphs to bullet points",
                    matchingScore = 62
                )
            )

            // Seed default interview mock records
            interviewDao.insertInterview(
                InterviewRecord(
                    role = "Frontend Developer",
                    timestamp = System.currentTimeMillis() - 86400000,
                    confidenceScore = 80,
                    communicationScore = 75,
                    technicalScore = 60,
                    overallScore = 71,
                    feedback = "Excellent confidence. Communication is crisp and visual. Technical rounds show gap in React Hooks (useEffect dependency optimization) and CSS Flexbox alignment rules. Practice the React questions in the tracker."
                )
            )

            // Seed default roadmap skills across roles
            val defaultSkills = listOf(
                // Frontend Developer
                SkillRecord(roleName = "Frontend Developer", skillName = "HTML5 & CSS3 layout architecture", isCompleted = true, progressPercent = 100),
                SkillRecord(roleName = "Frontend Developer", skillName = "JavaScript (ES6+) Foundations", isCompleted = true, progressPercent = 85),
                SkillRecord(roleName = "Frontend Developer", skillName = "React.js State Management (Redux/Zustand)", isCompleted = false, progressPercent = 40),
                SkillRecord(roleName = "Frontend Developer", skillName = "Tailwind CSS & Responsive Layout styles", isCompleted = true, progressPercent = 100),
                SkillRecord(roleName = "Frontend Developer", skillName = "Modern Build Tools (Vite, Webpack)", isCompleted = false, progressPercent = 10),
                SkillRecord(roleName = "Frontend Developer", skillName = "ATS Resume keywords & GitHub Portfolio builders", isCompleted = false, progressPercent = 20),

                // Data Analyst
                SkillRecord(roleName = "Data Analyst", skillName = "Advanced SQL Queries & Joins", isCompleted = false, progressPercent = 0),
                SkillRecord(roleName = "Data Analyst", skillName = "Python Pandas & NumPy pipelines", isCompleted = false, progressPercent = 0),
                SkillRecord(roleName = "Data Analyst", skillName = "Tableau / PowerBI Interactive Dashboards", isCompleted = false, progressPercent = 0),
                SkillRecord(roleName = "Data Analyst", skillName = "Statistics & Hypothesis Testing models", isCompleted = false, progressPercent = 0),

                // UI/UX Designer
                SkillRecord(roleName = "UI/UX Designer", skillName = "Figma Design Systems & Components", isCompleted = false, progressPercent = 0),
                SkillRecord(roleName = "UI/UX Designer", skillName = "User Research & Information Architecture", isCompleted = false, progressPercent = 0),
                SkillRecord(roleName = "UI/UX Designer", skillName = "Interactive Prototyping & Motion specs", isCompleted = false, progressPercent = 0),
                SkillRecord(roleName = "UI/UX Designer", skillName = "Typography & Spacing balance", isCompleted = false, progressPercent = 0),

                // AI Engineer
                SkillRecord(roleName = "AI Engineer", skillName = "Python & PyTorch neural architectures", isCompleted = false, progressPercent = 0),
                SkillRecord(roleName = "AI Engineer", skillName = "Large Language Models & Prompt Engineering", isCompleted = false, progressPercent = 0),
                SkillRecord(roleName = "AI Engineer", skillName = "Gemini API integrations", isCompleted = false, progressPercent = 0),
                SkillRecord(roleName = "AI Engineer", skillName = "Vector Databases & Retrieval-Augmented Generation (RAG)", isCompleted = false, progressPercent = 0)
            )
            skillDao.insertSkills(defaultSkills)
        }
    }
}
