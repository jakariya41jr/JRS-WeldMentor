package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WeldJobEntity::class,
        DailyTaskEntity::class,
        QuizAttemptEntity::class,
        ChecklistProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WeldDatabase : RoomDatabase() {
    abstract fun weldJobDao(): WeldJobDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun checklistDao(): ChecklistDao

    companion object {
        @Volatile
        private var INSTANCE: WeldDatabase? = null

        fun getInstance(context: Context): WeldDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeldDatabase::class.java,
                    "jrs_weldmentor_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
