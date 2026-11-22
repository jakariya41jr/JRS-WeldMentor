package com.example.data.repository

import android.content.Context
import com.example.data.backup.BackupManager
import com.example.data.content.VivaEvalResult
import com.example.data.content.VivaKnowledgeBase
import com.example.data.local.AppThemeMode
import com.example.data.local.ChecklistProgressEntity
import com.example.data.local.DEFAULT_GEMINI_MODEL
import com.example.data.local.PreferencesManager
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.SpeechSpeedPreset
import com.example.data.local.UserPreferences
import com.example.data.local.WeldDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.QuizQuestion
import com.example.data.remote.ApiKeyStatus
import com.example.data.remote.ExchangeRateService
import com.example.data.remote.GeminiRestClient
import com.example.data.remote.NetworkMonitor
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow

class WeldRepository(context: Context) {
    private val db = WeldDatabase.getInstance(context)
    private val quizDao = db.quizAttemptDao()
    private val checklistDao = db.checklistDao()

    private val prefsManager = PreferencesManager(context)
    private val geminiClient = GeminiRestClient()
    private val exchangeRateService = ExchangeRateService()
    private val networkMonitor = NetworkMonitor(context)

    val quizAttemptsFlow: Flow<List<QuizAttemptEntity>> = quizDao.observeAll()
    val checklistsFlow: Flow<List<ChecklistProgressEntity>> = checklistDao.observeAll()
    val preferencesFlow: Flow<UserPreferences> = prefsManager.preferencesFlow
    val isOnlineFlow: Flow<Boolean> = networkMonitor.isOnlineFlow

    suspend fun recordQuizAttempt(
        questionId: String,
        topicEn: String,
        topicBn: String,
        isCorrect: Boolean
    ) {
        quizDao.insert(
            QuizAttemptEntity(
                questionId = questionId,
                topicEn = topicEn,
                topicBn = topicBn,
                isCorrect = isCorrect
            )
        )
    }

    suspend fun resetQuizStats() = quizDao.deleteAll()

    suspend fun toggleChecklist(itemId: String, groupName: String, checked: Boolean) {
        checklistDao.upsert(
            ChecklistProgressEntity(
                itemId = itemId,
                groupName = groupName,
                isChecked = checked
            )
        )
    }

    suspend fun setLanguage(language: AppLanguage) = prefsManager.setLanguage(language)

    suspend fun setThemeMode(mode: AppThemeMode) = prefsManager.setThemeMode(mode)

    suspend fun setVoiceSettings(
        autoSpeakAi: Boolean? = null,
        bargeInEnabled: Boolean? = null,
        speechSpeedPreset: SpeechSpeedPreset? = null
    ) = prefsManager.setVoiceSettings(autoSpeakAi, bargeInEnabled, speechSpeedPreset)

    suspend fun setGeminiConfig(apiKey: String, model: String = DEFAULT_GEMINI_MODEL) =
        prefsManager.setGeminiConfig(apiKey, model)

    suspend fun validateAndActivateGeminiKey(apiKeyOverride: String? = null): ApiKeyStatus {
        val prefs = prefsManager.getSnapshot()
        val key = (apiKeyOverride ?: prefs.geminiApiKey).trim()
        if (key.isEmpty()) {
            return ApiKeyStatus.Missing
        }
        if (!networkMonitor.isOnlineNow()) {
            return ApiKeyStatus.Offline(prefs.geminiModel.ifBlank { DEFAULT_GEMINI_MODEL })
        }
        val status = geminiClient.detectAndValidateApiKey(key, prefs.language)
        if (status is ApiKeyStatus.Active) {
            prefsManager.setDetectedModel(status.modelId)
        }
        return status
    }

    suspend fun setProfile(name: String, targetTrade: String) =
        prefsManager.setProfile(name, targetTrade)

    suspend fun toggleMasteredFlashcard(cardId: String, mastered: Boolean) =
        prefsManager.toggleMasteredFlashcard(cardId, mastered)

    suspend fun exportJsonBackup(): String {
        val attempts = quizDao.getAllOnce()
        val checks = checklistDao.getAllOnce()
        val prefs = prefsManager.getSnapshot()
        return BackupManager.exportToJson(attempts, checks, prefs)
    }

    suspend fun importBackupContent(raw: String): Result<String> {
        return try {
            val payload = BackupManager.importBackup(raw)
            if (payload.quizAttempts.isNotEmpty()) {
                quizDao.insertAll(payload.quizAttempts)
            }
            if (payload.checklists.isNotEmpty()) {
                checklistDao.upsertAll(payload.checklists)
            }
            prefsManager.restoreFromBackup(
                languageCode = payload.languageCode,
                traineeName = payload.traineeName,
                targetTrade = payload.targetTrade,
                masteredCards = payload.masteredCards
            )
            Result.success("Imported ${payload.quizAttempts.size} viva quiz records & ${payload.checklists.size} guide items.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun askAiMentor(
        history: List<ChatMessage>,
        userPrompt: String
    ): Result<String> {
        val prefs = prefsManager.getSnapshot()
        if (!networkMonitor.isOnlineNow()) {
            return Result.failure(
                IllegalStateException(
                    if (prefs.language == AppLanguage.BN) {
                        "ইন্টারনেট সংযোগ নেই। AI মেন্টর ব্যবহার করতে অনলাইন হোন।"
                    } else {
                        "No internet connection. Connect to the internet to use AI Mentor."
                    }
                )
            )
        }
        return geminiClient.sendMessage(
            apiKey = prefs.geminiApiKey,
            modelName = prefs.geminiModel,
            language = prefs.language,
            history = history,
            userPrompt = userPrompt
        )
    }

    suspend fun refreshLiveExchangeRates(): Result<Long> {
        val prefs = prefsManager.getSnapshot()
        if (!networkMonitor.isOnlineNow()) {
            return Result.failure(
                IllegalStateException(
                    if (prefs.language == AppLanguage.BN) {
                        "ইন্টারনেট সংযোগ নেই। অফলাইন ডিফল্ট/ক্যাশড রেট ব্যবহার হচ্ছে।"
                    } else {
                        "No internet connection. Using offline cached/default rates."
                    }
                )
            )
        }
        return exchangeRateService.fetchLiveRatesUsd().mapCatching { res ->
            prefsManager.saveCachedRates(res.ratesPerUsd, res.fetchedAtMillis)
            res.fetchedAtMillis
        }
    }

    suspend fun setCustomExchangeRate(fromCode: String, toCode: String, rate: Double?) {
        prefsManager.setCustomPairRate("${fromCode.uppercase()}_${toCode.uppercase()}", rate)
    }

    suspend fun evaluateDelegateAnswer(
        question: QuizQuestion,
        userAnswer: String
    ): VivaEvalResult {
        val prefs = prefsManager.getSnapshot()
        val lang = prefs.language
        if (prefs.geminiApiKey.isNotBlank() && networkMonitor.isOnlineNow()) {
            val aiResult = geminiClient.evaluateVivaDelegateAnswer(
                apiKey = prefs.geminiApiKey,
                modelName = prefs.geminiModel,
                language = lang,
                questionText = question.question.get(lang),
                expectedAnswer = question.options[question.correctIndex].get(lang),
                officialExplanation = question.explanation.get(lang),
                userAnswer = userAnswer
            )
            if (aiResult.isSuccess) {
                return aiResult.getOrThrow()
            }
        }
        return VivaKnowledgeBase.evaluateOfflineDelegateAnswer(
            question = question,
            userAnswer = userAnswer,
            language = lang
        )
    }
}
