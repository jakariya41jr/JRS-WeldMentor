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
            "You are the official AI Mentor for JRS WeldMentor (developed by Md. Jakariya Hasan, jakariya41jr.xyz). " +
                "You must ONLY answer questions related to welding (SMAW/ARC, GTAW/TIG, FCAW, MIG/MAG, positions 1G-6G+, " +
                "joints, electrodes/filler rods like E6013, E7018, LB52/E7016, ER70S-6, 316L, measuring tools, PPE/HSE, " +
                "defect troubleshooting, welding trade test/viva preparation, and overseas welding job preparation) " +
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
}
