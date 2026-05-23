package com.example.data.local.database

import android.content.Context
import androidx.room.*
import com.example.data.local.converters.Converters
import com.example.data.local.dao.*
import com.example.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        ResumeEntity::class,
        SkillEntity::class,
        InterviewEntity::class,
        BadgeEntity::class,
        ExperienceEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CareeronixLocalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun resumeDao(): ResumeDao
    abstract fun skillDao(): SkillDao
    abstract fun interviewDao(): InterviewDao
    abstract fun badgeDao(): BadgeDao
    abstract fun experienceEntityDao(): ExperienceEntityDao

    companion object {
        @Volatile
        private var INSTANCE: CareeronixLocalDatabase? = null

        fun getDatabase(context: Context): CareeronixLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CareeronixLocalDatabase::class.java,
                    "careeronix_local_db"
                )
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
                context.deleteDatabase("careeronix_local_db")
            }
        }
    }
}
