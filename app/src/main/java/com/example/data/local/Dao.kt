package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts ORDER BY attemptedAt DESC")
    fun observeAll(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts")
    suspend fun getAllOnce(): List<QuizAttemptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: QuizAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attempts: List<QuizAttemptEntity>)

    @Query("DELETE FROM quiz_attempts")
    suspend fun deleteAll()
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_progress")
    fun observeAll(): Flow<List<ChecklistProgressEntity>>

    @Query("SELECT * FROM checklist_progress")
    suspend fun getAllOnce(): List<ChecklistProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ChecklistProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ChecklistProgressEntity>)

    @Query("DELETE FROM checklist_progress")
    suspend fun deleteAll()
}
