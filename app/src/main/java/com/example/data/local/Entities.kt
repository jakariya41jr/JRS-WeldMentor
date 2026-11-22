package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: String,
    val topicEn: String,
    val topicBn: String,
    val isCorrect: Boolean,
    val attemptedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "checklist_progress")
data class ChecklistProgressEntity(
    @PrimaryKey val itemId: String,
    val groupName: String,
    val isChecked: Boolean
)
