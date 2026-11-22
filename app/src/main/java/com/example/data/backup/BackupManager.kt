package com.example.data.backup

import com.example.data.local.ChecklistProgressEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.UserPreferences
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
    val quizAttempts: List<QuizAttemptEntity>,
    val checklists: List<ChecklistProgressEntity>,
    val languageCode: String?,
    val traineeName: String?,
    val targetTrade: String?,
    val masteredCards: Set<String>?
)

object BackupManager {

    fun exportToJson(
        attempts: List<QuizAttemptEntity>,
        checklists: List<ChecklistProgressEntity>,
        prefs: UserPreferences
    ): String {
        val root = JSONObject().apply {
            put("app", "JRS WeldMentor")
            put("developer", "Md Jakariya Hasan (Jakariya41jr.xyz)")
            put("schemaVersion", 2)
            put("exportedAt", System.currentTimeMillis())

            put("preferences", JSONObject().apply {
                put("language", prefs.language.code)
                put("traineeName", prefs.traineeName)
                put("targetTrade", prefs.targetTrade)
                put("masteredCards", JSONArray(prefs.masteredFlashcardIds.toList()))
            })

            put("quizAttempts", JSONArray().apply {
                attempts.forEach { a ->
                    put(JSONObject().apply {
                        put("questionId", a.questionId)
                        put("topicEn", a.topicEn)
                        put("topicBn", a.topicBn)
                        put("isCorrect", a.isCorrect)
                        put("attemptedAt", a.attemptedAt)
                    })
                }
            })

            put("checklists", JSONArray().apply {
                checklists.forEach { c ->
                    put(JSONObject().apply {
                        put("itemId", c.itemId)
                        put("groupName", c.groupName)
                        put("isChecked", c.isChecked)
                    })
                }
            })
        }
        return root.toString(2)
    }

    fun importBackup(rawContent: String): BackupPayload {
        val trimmed = rawContent.trim()
        if (!trimmed.startsWith("{")) {
            throw IllegalArgumentException("Please select a valid JRS WeldMentor JSON backup file.")
        }
        return parseJsonBackup(trimmed)
    }

    private fun parseJsonBackup(jsonStr: String): BackupPayload {
        val root = JSONObject(jsonStr)
        val prefsObj = root.optJSONObject("preferences")
        val masteredList = mutableSetOf<String>()
        val masteredArr = prefsObj?.optJSONArray("masteredCards")
        if (masteredArr != null) {
            for (i in 0 until masteredArr.length()) {
                masteredList.add(masteredArr.getString(i))
            }
        }

        val attemptsList = mutableListOf<QuizAttemptEntity>()
        val attArr = root.optJSONArray("quizAttempts") ?: JSONArray()
        for (i in 0 until attArr.length()) {
            val o = attArr.getJSONObject(i)
            attemptsList.add(
                QuizAttemptEntity(
                    questionId = o.optString("questionId", ""),
                    topicEn = o.optString("topicEn", ""),
                    topicBn = o.optString("topicBn", ""),
                    isCorrect = o.optBoolean("isCorrect", false),
                    attemptedAt = o.optLong("attemptedAt", System.currentTimeMillis())
                )
            )
        }

        val checkList = mutableListOf<ChecklistProgressEntity>()
        val chkArr = root.optJSONArray("checklists") ?: JSONArray()
        for (i in 0 until chkArr.length()) {
            val o = chkArr.getJSONObject(i)
            checkList.add(
                ChecklistProgressEntity(
                    itemId = o.optString("itemId", ""),
                    groupName = o.optString("groupName", "STANCE"),
                    isChecked = o.optBoolean("isChecked", false)
                )
            )
        }

        return BackupPayload(
            quizAttempts = attemptsList,
            checklists = checkList,
            languageCode = prefsObj?.optString("language")?.takeIf { it.isNotBlank() },
            traineeName = prefsObj?.optString("traineeName")?.takeIf { it.isNotBlank() },
            targetTrade = prefsObj?.optString("targetTrade")?.takeIf { it.isNotBlank() },
            masteredCards = if (masteredList.isEmpty()) null else masteredList
        )
    }
}
