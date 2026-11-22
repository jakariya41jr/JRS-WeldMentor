package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class LiveRatesResponse(
    val ratesPerUsd: Map<String, Double>,
    val fetchedAtMillis: Long
)

class ExchangeRateService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchLiveRatesUsd(): Result<LiveRatesResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://open.er-api.com/v6/latest/USD")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("HTTP ${response.code}: ${response.message}")
                    )
                }
                val json = JSONObject(body)
                if (json.optString("result") != "success") {
                    return@withContext Result.failure(
                        IllegalStateException("Rate service returned an error.")
                    )
                }
                val ratesObj = json.getJSONObject("rates")
                val map = mutableMapOf<String, Double>()
                val keys = ratesObj.keys()
                while (keys.hasNext()) {
                    val code = keys.next()
                    val rate = ratesObj.optDouble(code, -1.0)
                    if (rate > 0.0) {
                        map[code.uppercase()] = rate
                    }
                }
                Result.success(
                    LiveRatesResponse(
                        ratesPerUsd = map,
                        fetchedAtMillis = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
