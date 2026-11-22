package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        QuizAttemptEntity::class,
        ChecklistProgressEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WeldDatabase : RoomDatabase() {
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
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
