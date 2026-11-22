package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jrs_weldmentor_prefs")

const val DEFAULT_GEMINI_MODEL = "gemini-3.0-flash"

enum class AppThemeMode(val code: String) {
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromCode(code: String?): AppThemeMode = if (code == "light") LIGHT else DARK
    }
}

enum class SpeechSpeedPreset(val code: String, val rateMultiplier: Float) {
    RELAXED("relaxed", 0.88f),
    NATURAL("natural", 0.97f),
    CRISP("crisp", 1.07f);

    companion object {
        fun fromCode(code: String?): SpeechSpeedPreset = entries.firstOrNull { it.code == code } ?: NATURAL
    }
}

data class UserPreferences(
    val language: AppLanguage = AppLanguage.EN,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val autoSpeakAi: Boolean = true,
    val bargeInEnabled: Boolean = true,
    val speechSpeedPreset: SpeechSpeedPreset = SpeechSpeedPreset.NATURAL,
    val geminiApiKey: String = "",
    val geminiModel: String = DEFAULT_GEMINI_MODEL,
    val traineeName: String = "Welding Trainee",
    val targetTrade: String = "6G TIG & SMAW Pipe Welder",
    val masteredFlashcardIds: Set<String> = emptySet(),
    val cachedRatesPerUsd: Map<String, Double> = emptyMap(),
    val ratesLastUpdatedMillis: Long = 0L,
    val customPairRates: Map<String, Double> = emptyMap()
)

class PreferencesManager(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTO_SPEAK_AI = booleanPreferencesKey("auto_speak_ai")
        val BARGE_IN_ENABLED = booleanPreferencesKey("barge_in_enabled")
        val SPEECH_SPEED = stringPreferencesKey("speech_speed")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val TRAINEE_NAME = stringPreferencesKey("trainee_name")
        val TARGET_TRADE = stringPreferencesKey("target_trade")
        val MASTERED_CARDS = stringPreferencesKey("mastered_cards")
        val CACHED_RATES_JSON = stringPreferencesKey("cached_rates_json")
        val RATES_UPDATED_AT = longPreferencesKey("rates_updated_at")
        val CUSTOM_RATES_JSON = stringPreferencesKey("custom_rates_json")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val masteredRaw = prefs[Keys.MASTERED_CARDS] ?: ""
        val masteredSet = if (masteredRaw.isBlank()) emptySet() else masteredRaw.split(",").filter { it.isNotBlank() }.toSet()

        val storedModel = prefs[Keys.GEMINI_MODEL].orEmpty()
        val resolvedModel = when {
            storedModel.isBlank() || storedModel == "gemini-2.5-flash" || storedModel == "gemini-2.5-pro" -> DEFAULT_GEMINI_MODEL
            else -> storedModel
        }

        UserPreferences(
            language = if (prefs[Keys.LANGUAGE] == "bn") AppLanguage.BN else AppLanguage.EN,
            themeMode = AppThemeMode.fromCode(prefs[Keys.THEME_MODE]),
            autoSpeakAi = prefs[Keys.AUTO_SPEAK_AI] ?: true,
            bargeInEnabled = prefs[Keys.BARGE_IN_ENABLED] ?: true,
            speechSpeedPreset = SpeechSpeedPreset.fromCode(prefs[Keys.SPEECH_SPEED]),
            geminiApiKey = prefs[Keys.GEMINI_API_KEY] ?: "",
            geminiModel = resolvedModel,
            traineeName = prefs[Keys.TRAINEE_NAME] ?: "Welding Trainee",
            targetTrade = prefs[Keys.TARGET_TRADE] ?: "6G TIG & SMAW Pipe Welder",
            masteredFlashcardIds = masteredSet,
            cachedRatesPerUsd = parseDoubleMap(prefs[Keys.CACHED_RATES_JSON]),
            ratesLastUpdatedMillis = prefs[Keys.RATES_UPDATED_AT] ?: 0L,
            customPairRates = parseDoubleMap(prefs[Keys.CUSTOM_RATES_JSON])
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.code }
    }

    suspend fun setVoiceSettings(
        autoSpeakAi: Boolean? = null,
        bargeInEnabled: Boolean? = null,
        speechSpeedPreset: SpeechSpeedPreset? = null
    ) {
        context.dataStore.edit { prefs ->
            autoSpeakAi?.let { prefs[Keys.AUTO_SPEAK_AI] = it }
            bargeInEnabled?.let { prefs[Keys.BARGE_IN_ENABLED] = it }
            speechSpeedPreset?.let { prefs[Keys.SPEECH_SPEED] = it.code }
        }
    }

    suspend fun setGeminiConfig(apiKey: String, model: String = DEFAULT_GEMINI_MODEL) {
        context.dataStore.edit {
            it[Keys.GEMINI_API_KEY] = apiKey.trim()
            it[Keys.GEMINI_MODEL] = model.trim().ifBlank { DEFAULT_GEMINI_MODEL }
        }
    }

    suspend fun setDetectedModel(model: String) {
        context.dataStore.edit {
            it[Keys.GEMINI_MODEL] = model.trim().ifBlank { DEFAULT_GEMINI_MODEL }
        }
    }

    suspend fun setProfile(name: String, targetTrade: String) {
        context.dataStore.edit {
            it[Keys.TRAINEE_NAME] = name.trim().ifBlank { "Welding Trainee" }
            it[Keys.TARGET_TRADE] = targetTrade.trim().ifBlank { "6G TIG & SMAW Pipe Welder" }
        }
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
        masteredCards: Set<String>?
    ) {
        context.dataStore.edit { prefs ->
            languageCode?.let { prefs[Keys.LANGUAGE] = if (it == "bn") "bn" else "en" }
            traineeName?.let { prefs[Keys.TRAINEE_NAME] = it }
            targetTrade?.let { prefs[Keys.TARGET_TRADE] = it }
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
}
