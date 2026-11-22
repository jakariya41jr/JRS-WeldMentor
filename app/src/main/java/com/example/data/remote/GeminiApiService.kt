package com.example.data.remote

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

class GeminiRestClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    companion object {
        const val STRICT_SYSTEM_INSTRUCTION =
            "You are the official AI Mentor for JRS WeldMentor, developed by Md Jakariya Hasan (Jakariya41jr.xyz). " +
                "You must ONLY answer questions related to welding (SMAW, GTAW/TIG, FCAW, MIG, positions 1G-6G+, " +
                "joints, electrodes/fillers like E6013, E7018, LB52/E7016, ER70S-6, 316L, measuring tools, PPE/HSE, " +
                "defect troubleshooting, welding trade test/viva preparation, and overseas job preparation) " +
                "OR features/usage of the JRS WeldMentor app itself. " +
                "If the user asks about anything outside welding, trade preparation, or this app, politely decline in the " +
                "user's selected language (Bangla or English) and remind them that you only assist with welding and JRS WeldMentor."
    }

    suspend fun sendMessage(
        apiKey: String,
        modelName: String,
        language: AppLanguage,
        history: List<ChatMessage>,
        userPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanKey = apiKey.trim()
            if (cleanKey.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("Gemini API key is missing."))
            }

            val langInstruction = if (language == AppLanguage.BN) {
                "Preferred user language: Bangla (বাংলা). Answer clearly in Bangla while keeping technical AWS/ASME codes like E7018, LB52, 6G, TIG readable."
            } else {
                "Preferred user language: English. Answer clearly and practically for a welding trainee."
            }

            val contentsArray = JSONArray()
            val recentHistory = history.takeLast(8)
            recentHistory.forEach { msg ->
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
                            JSONObject().put(
                                "text",
                                "$STRICT_SYSTEM_INSTRUCTION\n\n$langInstruction"
                            )
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

            val modelId = modelName.trim().ifBlank { "gemini-2.5-flash" }
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent"

            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", cleanKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val errMsg = try {
                        JSONObject(rawBody).optJSONObject("error")?.optString("message")
                            ?: "HTTP ${response.code}"
                    } catch (_: Exception) {
                        "HTTP ${response.code}: ${response.message}"
                    }
                    return@withContext Result.failure(IllegalStateException(errMsg))
                }

                val json = JSONObject(rawBody)
                val candidates = json.optJSONArray("candidates")
                val text = candidates
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    .orEmpty()

                if (text.isBlank()) {
                    return@withContext Result.failure(IllegalStateException("Gemini returned an empty response."))
                }
                Result.success(text.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun evaluateVivaDelegateAnswer(
        apiKey: String,
        modelName: String,
        language: AppLanguage,
        questionText: String,
        expectedAnswer: String,
        officialExplanation: String,
        userAnswer: String
    ): Result<com.example.data.content.VivaEvalResult> = withContext(Dispatchers.IO) {
        try {
            val cleanKey = apiKey.trim()
            if (cleanKey.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("Gemini API key missing"))
            }

            val langInstruction = if (language == AppLanguage.BN) {
                "Respond in clear, encouraging Bangla (বাংলা) keeping AWS/ASME technical terms readable."
            } else {
                "Respond in clear, encouraging English suitable for a welding trade test candidate."
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
                2. Next 2-3 short sentences: Give examiner feedback confirming why it passed or gently explaining the exact technical answer so they memorize it for their overseas trade interview.
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

            val modelId = modelName.trim().ifBlank { "gemini-2.5-flash" }
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent"

            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", cleanKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IllegalStateException("HTTP ${response.code}"))
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
                    return@withContext Result.failure(IllegalStateException("Empty evaluator reply"))
                }

                val upper = text.uppercase()
                val passed = upper.startsWith("[PASS]") || (upper.contains("[PASS]") && !upper.contains("[REVIEW]"))
                val cleanFeedback = text
                    .replace(Regex("^\\[(PASS|REVIEW)]\\s*", RegexOption.IGNORE_CASE), "")
                    .trim()

                Result.success(
                    com.example.data.content.VivaEvalResult(
                        passed = passed,
                        feedback = cleanFeedback,
                        evaluatedByAi = true
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
