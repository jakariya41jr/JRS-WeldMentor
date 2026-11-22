package com.example.data.remote

import com.example.data.content.VivaEvalResult
import com.example.data.local.DEFAULT_GEMINI_MODEL
import com.example.data.model.ChatMessage
import com.example.i18n.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed interface ApiKeyStatus {
    data object Missing : ApiKeyStatus
    data object Validating : ApiKeyStatus
    data class Active(val modelId: String, val detail: String) : ApiKeyStatus
    data class Invalid(val detail: String) : ApiKeyStatus
    data class QuotaLimited(val modelId: String, val detail: String) : ApiKeyStatus
    data class Offline(val modelId: String) : ApiKeyStatus
}

class GeminiRestClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    companion object {
        const val DEFAULT_MODEL = DEFAULT_GEMINI_MODEL

        const val STRICT_SYSTEM_INSTRUCTION =
            "You are the official AI Mentor for JRS WeldMentor, developed by Md Jakariya Hasan (Jakariya41jr.xyz). " +
                "You must ONLY answer questions related to welding (SMAW, GTAW/TIG, FCAW, MIG, positions 1G-6G+, " +
                "joints, electrodes/fillers like E6013, E7018, LB52/E7016, ER70S-6, 316L, measuring tools, PPE/HSE, " +
                "defect troubleshooting, welding trade test/viva preparation, and overseas job preparation) " +
                "OR features/usage of the JRS WeldMentor app itself. " +
                "If the user asks about anything outside welding, trade preparation, or this app, politely decline in the " +
                "user's selected language (Bangla or English) and remind them that you only assist with welding and JRS WeldMentor."
    }

    suspend fun detectAndValidateApiKey(
        apiKey: String,
        language: AppLanguage
    ): ApiKeyStatus = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return@withContext ApiKeyStatus.Missing
        }
        try {
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models?pageSize=50")
                .addHeader("x-goog-api-key", cleanKey)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (response.code == 429) {
                    val detail = if (language == AppLanguage.BN) {
                        "API কী সঠিক, তবে ফ্রি কোটা সীমা সাময়িকভাবে পূর্ণ (HTTP 429)।"
                    } else {
                        "API key recognized, but quota is temporarily limited (HTTP 429)."
                    }
                    return@withContext ApiKeyStatus.QuotaLimited(DEFAULT_MODEL, detail)
                }

                if (!response.isSuccessful) {
                    val apiErr = parseGoogleErrorMessage(rawBody, response.code, response.message)
                    val detail = if (language == AppLanguage.BN) {
                        "অকার্যকর বা অননুমোদিত Gemini API কী: $apiErr"
                    } else {
                        "Invalid or unauthorized Gemini API key: $apiErr"
                    }
                    return@withContext ApiKeyStatus.Invalid(detail)
                }

                val selectedModel = pickBestModelFromList(rawBody)
                val detail = if (language == AppLanguage.BN) {
                    "কী যাচাই সম্পন্ন ও সক্রিয় ($selectedModel)"
                } else {
                    "Key verified & active ($selectedModel)"
                }
                ApiKeyStatus.Active(modelId = selectedModel, detail = detail)
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Network error"
            val detail = if (language == AppLanguage.BN) {
                "কী যাচাই করা যায়নি: $msg"
            } else {
                "Could not validate key: $msg"
            }
            ApiKeyStatus.Invalid(detail)
        }
    }

    private fun pickBestModelFromList(rawJson: String): String {
        return try {
            val modelsArr = JSONObject(rawJson).optJSONArray("models") ?: return DEFAULT_MODEL
            val generateCapable = mutableListOf<String>()
            for (i in 0 until modelsArr.length()) {
                val obj = modelsArr.optJSONObject(i) ?: continue
                val methods = obj.optJSONArray("supportedGenerationMethods")
                var supportsGen = false
                if (methods != null) {
                    for (m in 0 until methods.length()) {
                        if (methods.optString(m) == "generateContent") {
                            supportsGen = true
                            break
                        }
                    }
                }
                if (supportsGen) {
                    val id = obj.optString("name").removePrefix("models/").trim()
                    if (id.isNotBlank()) generateCapable.add(id)
                }
            }

            if (generateCapable.isEmpty()) return DEFAULT_MODEL

            generateCapable.firstOrNull { it.equals(DEFAULT_MODEL, ignoreCase = true) }
                ?: generateCapable.firstOrNull { it.startsWith("gemini-3.0-flash") || it.startsWith("gemini-3-flash") }
                ?: generateCapable.firstOrNull { it.startsWith("gemini-3") && it.contains("flash") }
                ?: generateCapable.firstOrNull { it == "gemini-2.5-flash" }
                ?: generateCapable.firstOrNull { it.contains("flash") && it.startsWith("gemini") }
                ?: DEFAULT_MODEL
        } catch (_: Exception) {
            DEFAULT_MODEL
        }
    }

    suspend fun sendMessage(
        apiKey: String,
        modelName: String,
        language: AppLanguage,
        history: List<ChatMessage>,
        userPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException(
                    if (language == AppLanguage.BN) "Gemini API কী সেট করা নেই।" else "Gemini API key is missing."
                )
            )
        }

        val langInstruction = if (language == AppLanguage.BN) {
            "Preferred user language: Bangla (বাংলা). Speak/write naturally in fluent, conversational Bangla without choppy formatting, keeping AWS/ASME codes like E7018, LB52, 6G, TIG readable."
        } else {
            "Preferred user language: English. Respond in clear, smooth, natural conversational English for a welding trainee."
        }

        val contentsArray = JSONArray()
        history.takeLast(8).forEach { msg ->
            contentsArray.put(
                JSONObject().apply {
                    put("role", if (msg.isUser) "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                }
            )
        }
        contentsArray.put(
            JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
            }
        )

        val payload = JSONObject().apply {
            put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(
                        JSONObject().put("text", "$STRICT_SYSTEM_INSTRUCTION\n\n$langInstruction")
                    )
                )
            )
            put("contents", contentsArray)
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.4)
                    put("maxOutputTokens", 1024)
                }
            )
        }

        val primaryModel = modelName.trim().ifBlank { DEFAULT_MODEL }
        val firstAttempt = executeGenerateContent(cleanKey, primaryModel, payload, language)
        if (firstAttempt.isSuccess) return@withContext firstAttempt

        val err = firstAttempt.exceptionOrNull()
        if (err is ModelNotFoundException && primaryModel != "gemini-2.5-flash") {
            return@withContext executeGenerateContent(cleanKey, "gemini-2.5-flash", payload, language)
        }
        firstAttempt
    }

    suspend fun evaluateVivaDelegateAnswer(
        apiKey: String,
        modelName: String,
        language: AppLanguage,
        questionText: String,
        expectedAnswer: String,
        officialExplanation: String,
        userAnswer: String
    ): Result<VivaEvalResult> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key missing"))
        }

        val langInstruction = if (language == AppLanguage.BN) {
            "Respond in natural, smooth, encouraging Bangla (বাংলা) keeping AWS/ASME technical terms readable."
        } else {
            "Respond in clear, natural, encouraging English suitable for a welding trade test candidate."
        }

        val examinerPrompt = """
            You are a Welding Trade Test Delegate Examiner for JRS WeldMentor.
            Evaluate the candidate's answer strictly on welding technical accuracy (accept synonymous wording and bilingual phrasing).
            
            Question asked: $questionText
            Reference Correct Answer: $expectedAnswer
            Technical Basis: $officialExplanation
            Candidate's Spoken/Written Answer: $userAnswer
            
            Rules:
            1. First line MUST be strictly `[PASS]` if the candidate's answer demonstrates core understanding of the key welding fact, OR `[REVIEW]` if the answer is incorrect, off-topic, or misses the main technical point.
            2. Next 2-3 short, natural sentences: Give examiner feedback confirming why it passed or gently explaining the exact technical answer so it sounds great when read aloud.
            3. $langInstruction
        """.trimIndent()

        val payload = JSONObject().apply {
            put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", "$STRICT_SYSTEM_INSTRUCTION\n\n$langInstruction"))
                )
            )
            put(
                "contents",
                JSONArray().put(
                    JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", examinerPrompt)))
                    }
                )
            )
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 512)
                }
            )
        }

        val primaryModel = modelName.trim().ifBlank { DEFAULT_MODEL }
        var textResult = executeGenerateContent(cleanKey, primaryModel, payload, language)
        if (textResult.isFailure && textResult.exceptionOrNull() is ModelNotFoundException && primaryModel != "gemini-2.5-flash") {
            textResult = executeGenerateContent(cleanKey, "gemini-2.5-flash", payload, language)
        }

        textResult.mapCatching { text ->
            val upper = text.uppercase()
            val passed = upper.startsWith("[PASS]") || (upper.contains("[PASS]") && !upper.contains("[REVIEW]"))
            val cleanFeedback = text
                .replace(Regex("^\\[(PASS|REVIEW)]\\s*", RegexOption.IGNORE_CASE), "")
                .trim()
            VivaEvalResult(
                passed = passed,
                feedback = cleanFeedback,
                evaluatedByAi = true
            )
        }
    }

    private fun executeGenerateContent(
        apiKey: String,
        modelId: String,
        payload: JSONObject,
        language: AppLanguage
    ): Result<String> {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent"
            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val serverMsg = parseGoogleErrorMessage(rawBody, response.code, response.message)
                    return when (response.code) {
                        404 -> Result.failure(ModelNotFoundException(serverMsg))
                        429 -> Result.failure(
                            QuotaExceededException(
                                if (language == AppLanguage.BN) "API কোটা সীমা পূর্ণ হয়েছে (429): $serverMsg"
                                else "API quota limit reached (429): $serverMsg"
                            )
                        )
                        400, 401, 403 -> Result.failure(
                            InvalidApiKeyException(
                                if (language == AppLanguage.BN) "অকার্যকর বা অনুমোদনহীন API কী: $serverMsg"
                                else "Invalid or unauthorized API key: $serverMsg"
                            )
                        )
                        else -> Result.failure(IllegalStateException(serverMsg))
                    }
                }

                val text = JSONObject(rawBody)
                    .optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    .orEmpty()
                    .trim()

                if (text.isBlank()) {
                    Result.failure(IllegalStateException("Gemini returned an empty response."))
                } else {
                    Result.success(text)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseGoogleErrorMessage(rawBody: String, code: Int, fallbackMsg: String): String {
        return try {
            JSONObject(rawBody).optJSONObject("error")?.optString("message")
                ?.takeIf { it.isNotBlank() }
                ?: "HTTP $code: $fallbackMsg"
        } catch (_: Exception) {
            "HTTP $code: $fallbackMsg"
        }
    }
}

class ModelNotFoundException(message: String) : IllegalStateException(message)
class QuotaExceededException(message: String) : IllegalStateException(message)
class InvalidApiKeyException(message: String) : IllegalStateException(message)
