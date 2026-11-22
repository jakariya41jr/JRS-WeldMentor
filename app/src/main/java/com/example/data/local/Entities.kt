package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weld_jobs")
data class WeldJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val process: String,
    val position: String,
    val baseMaterial: String,
    val jointType: String,
    val rodFillerAndSize: String,
    val tigCup: String,
    val tigColletBody: String,
    val tigBackCap: String,
    val tigTungsten: String,
    val amperage: String,
    val voltage: String,
    val gasFlow: String,
    val measuringTools: String, // comma-separated
    val ppeChecked: Boolean,
    val defectTags: String, // comma-separated
    val trainerFeedback: String,
    val status: String, // "Pass" or "Needs Improvement"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleEn: String,
    val titleBn: String,
    val category: String, // PRACTICAL, VIVA, SAFETY
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

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
