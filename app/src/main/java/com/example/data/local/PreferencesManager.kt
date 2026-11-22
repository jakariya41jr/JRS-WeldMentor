package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jrs_weldmentor_prefs")

data class UserPreferences(
    val language: AppLanguage = AppLanguage.EN,
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-2.5-flash",
    val traineeName: String = "Welding Trainee",
    val targetTrade: String = "6G TIG & SMAW Pipe Welder",
    val streakDays: Int = 1,
    val lastActiveDate: String = "",
    val practiceMinutesToday: Int = 0,
    val practiceMinutesDate: String = "",
    val dailyJobGoal: Int = 3,
    val dailyQuizGoal: Int = 10,
    val dailyMinutesGoal: Int = 60,
    val seededInitialTasks: Boolean = false,
    val masteredFlashcardIds: Set<String> = emptySet(),
    val cachedRatesPerUsd: Map<String, Double> = emptyMap(),
    val ratesLastUpdatedMillis: Long = 0L,
    val customPairRates: Map<String, Double> = emptyMap()
)

class PreferencesManager(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val TRAINEE_NAME = stringPreferencesKey("trainee_name")
        val TARGET_TRADE = stringPreferencesKey("target_trade")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val PRACTICE_MINUTES_TODAY = intPreferencesKey("practice_minutes_today")
        val PRACTICE_MINUTES_DATE = stringPreferencesKey("practice_minutes_date")
        val DAILY_JOB_GOAL = intPreferencesKey("daily_job_goal")
        val DAILY_QUIZ_GOAL = intPreferencesKey("daily_quiz_goal")
        val DAILY_MINUTES_GOAL = intPreferencesKey("daily_minutes_goal")
        val SEEDED_INITIAL_TASKS = booleanPreferencesKey("seeded_initial_tasks")
        val MASTERED_CARDS = stringPreferencesKey("mastered_cards")
        val CACHED_RATES_JSON = stringPreferencesKey("cached_rates_json")
        val RATES_UPDATED_AT = longPreferencesKey("rates_updated_at")
        val CUSTOM_RATES_JSON = stringPreferencesKey("custom_rates_json")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val today = todayString()
        val savedMinDate = prefs[Keys.PRACTICE_MINUTES_DATE] ?: ""
        val minutesToday = if (savedMinDate == today) prefs[Keys.PRACTICE_MINUTES_TODAY] ?: 0 else 0
        val masteredRaw = prefs[Keys.MASTERED_CARDS] ?: ""
        val masteredSet = if (masteredRaw.isBlank()) emptySet() else masteredRaw.split(",").filter { it.isNotBlank() }.toSet()

        UserPreferences(
            language = if (prefs[Keys.LANGUAGE] == "bn") AppLanguage.BN else AppLanguage.EN,
            geminiApiKey = prefs[Keys.GEMINI_API_KEY] ?: "",
            geminiModel = prefs[Keys.GEMINI_MODEL] ?: "gemini-2.5-flash",
            traineeName = prefs[Keys.TRAINEE_NAME] ?: "Welding Trainee",
            targetTrade = prefs[Keys.TARGET_TRADE] ?: "6G TIG & SMAW Pipe Welder",
            streakDays = prefs[Keys.STREAK_DAYS] ?: 1,
            lastActiveDate = prefs[Keys.LAST_ACTIVE_DATE] ?: today,
            practiceMinutesToday = minutesToday,
            practiceMinutesDate = savedMinDate,
            dailyJobGoal = prefs[Keys.DAILY_JOB_GOAL] ?: 3,
            dailyQuizGoal = prefs[Keys.DAILY_QUIZ_GOAL] ?: 10,
            dailyMinutesGoal = prefs[Keys.DAILY_MINUTES_GOAL] ?: 60,
            seededInitialTasks = prefs[Keys.SEEDED_INITIAL_TASKS] ?: false,
            masteredFlashcardIds = masteredSet,
            cachedRatesPerUsd = parseDoubleMap(prefs[Keys.CACHED_RATES_JSON]),
            ratesLastUpdatedMillis = prefs[Keys.RATES_UPDATED_AT] ?: 0L,
            customPairRates = parseDoubleMap(prefs[Keys.CUSTOM_RATES_JSON])
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setGeminiConfig(apiKey: String, model: String) {
        context.dataStore.edit {
            it[Keys.GEMINI_API_KEY] = apiKey.trim()
            it[Keys.GEMINI_MODEL] = model.ifBlank { "gemini-2.5-flash" }
        }
    }

    suspend fun setProfile(name: String, targetTrade: String) {
        context.dataStore.edit {
            it[Keys.TRAINEE_NAME] = name.trim().ifBlank { "Welding Trainee" }
            it[Keys.TARGET_TRADE] = targetTrade.trim().ifBlank { "6G TIG & SMAW Pipe Welder" }
        }
    }

    suspend fun setDailyGoals(jobGoal: Int, quizGoal: Int, minutesGoal: Int) {
        context.dataStore.edit {
            it[Keys.DAILY_JOB_GOAL] = jobGoal.coerceIn(1, 20)
            it[Keys.DAILY_QUIZ_GOAL] = quizGoal.coerceIn(1, 100)
            it[Keys.DAILY_MINUTES_GOAL] = minutesGoal.coerceIn(10, 480)
        }
    }

    suspend fun addPracticeMinutes(delta: Int) {
        val today = todayString()
        context.dataStore.edit { prefs ->
            val current = if (prefs[Keys.PRACTICE_MINUTES_DATE] == today) {
                prefs[Keys.PRACTICE_MINUTES_TODAY] ?: 0
            } else {
                0
            }
            prefs[Keys.PRACTICE_MINUTES_TODAY] = (current + delta).coerceAtLeast(0)
            prefs[Keys.PRACTICE_MINUTES_DATE] = today
        }
        recordActivityStreak()
    }

    suspend fun recordActivityStreak() {
        val today = todayString()
        val yesterday = yesterdayString()
        context.dataStore.edit { prefs ->
            val last = prefs[Keys.LAST_ACTIVE_DATE] ?: ""
            val currentStreak = prefs[Keys.STREAK_DAYS] ?: 1
            val updated = when (last) {
                today -> currentStreak
                yesterday -> currentStreak + 1
                else -> 1
            }
            prefs[Keys.STREAK_DAYS] = updated
            prefs[Keys.LAST_ACTIVE_DATE] = today
        }
    }

    suspend fun markSeededInitialTasks() {
        context.dataStore.edit { it[Keys.SEEDED_INITIAL_TASKS] = true }
    }

    suspend fun toggleMasteredFlashcard(cardId: String, mastered: Boolean) {
        context.dataStore.edit { prefs ->
            val raw = prefs[Keys.MASTERED_CARDS] ?: ""
            val set = raw.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (mastered) set.add(cardId) else set.remove(cardId)
            prefs[Keys.MASTERED_CARDS] = set.joinToString(",")
        }
    }

    suspend fun getSnapshot(): UserPreferences = preferencesFlow.first()

    suspend fun restoreFromBackup(
        languageCode: String?,
        traineeName: String?,
        targetTrade: String?,
        streakDays: Int?,
        masteredCards: Set<String>?
    ) {
        context.dataStore.edit { prefs ->
            languageCode?.let { prefs[Keys.LANGUAGE] = if (it == "bn") "bn" else "en" }
            traineeName?.let { prefs[Keys.TRAINEE_NAME] = it }
            targetTrade?.let { prefs[Keys.TARGET_TRADE] = it }
            streakDays?.let { prefs[Keys.STREAK_DAYS] = it.coerceAtLeast(1) }
            masteredCards?.let { prefs[Keys.MASTERED_CARDS] = it.joinToString(",") }
        }
    }

    suspend fun saveCachedRates(ratesPerUsd: Map<String, Double>, timestampMillis: Long) {
        val json = JSONObject()
        ratesPerUsd.forEach { (k, v) -> json.put(k, v) }
        context.dataStore.edit { prefs ->
            prefs[Keys.CACHED_RATES_JSON] = json.toString()
            prefs[Keys.RATES_UPDATED_AT] = timestampMillis
        }
    }

    suspend fun setCustomPairRate(pairKey: String, rate: Double?) {
        context.dataStore.edit { prefs ->
            val current = parseDoubleMap(prefs[Keys.CUSTOM_RATES_JSON]).toMutableMap()
            if (rate == null || rate <= 0.0) {
                current.remove(pairKey)
            } else {
                current[pairKey] = rate
            }
            val json = JSONObject()
            current.forEach { (k, v) -> json.put(k, v) }
            prefs[Keys.CUSTOM_RATES_JSON] = json.toString()
        }
    }

    private fun parseDoubleMap(raw: String?): Map<String, Double> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val result = mutableMapOf<String, Double>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = obj.optDouble(k, -1.0)
                if (v > 0.0) result[k] = v
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    companion object {
        fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        fun yesterdayString(): String {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        }
    }
}
