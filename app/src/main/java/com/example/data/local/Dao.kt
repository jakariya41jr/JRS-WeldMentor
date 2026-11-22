package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeldJobDao {
    @Query("SELECT * FROM weld_jobs ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<WeldJobEntity>>

    @Query("SELECT * FROM weld_jobs ORDER BY date DESC, createdAt DESC")
    suspend fun getAllOnce(): List<WeldJobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: WeldJobEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<WeldJobEntity>)

    @Update
    suspend fun update(job: WeldJobEntity)

    @Query("DELETE FROM weld_jobs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM weld_jobs")
    suspend fun deleteAll()
}

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks ORDER BY isDone ASC, createdAt DESC")
    fun observeAll(): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks")
    suspend fun getAllOnce(): List<DailyTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: DailyTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<DailyTaskEntity>)

    @Query("UPDATE daily_tasks SET isDone = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Query("DELETE FROM daily_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM daily_tasks")
    suspend fun deleteAll()
}

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
