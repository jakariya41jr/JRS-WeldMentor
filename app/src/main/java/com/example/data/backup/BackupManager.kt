package com.example.data.backup

import com.example.data.local.ChecklistProgressEntity
import com.example.data.local.DailyTaskEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.UserPreferences
import com.example.data.local.WeldJobEntity
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
    val jobs: List<WeldJobEntity>,
    val tasks: List<DailyTaskEntity>,
    val quizAttempts: List<QuizAttemptEntity>,
    val checklists: List<ChecklistProgressEntity>,
    val languageCode: String?,
    val traineeName: String?,
    val targetTrade: String?,
    val streakDays: Int?,
    val masteredCards: Set<String>?
)

object BackupManager {

    fun exportToJson(
        jobs: List<WeldJobEntity>,
        tasks: List<DailyTaskEntity>,
        attempts: List<QuizAttemptEntity>,
        checklists: List<ChecklistProgressEntity>,
        prefs: UserPreferences
    ): String {
        val root = JSONObject().apply {
            put("app", "JRS WeldMentor")
            put("developer", "Md Jakariya Hasan (Jakariya41jr.xyz)")
            put("schemaVersion", 1)
            put("exportedAt", System.currentTimeMillis())

            put("preferences", JSONObject().apply {
                put("language", prefs.language.code)
                put("traineeName", prefs.traineeName)
                put("targetTrade", prefs.targetTrade)
                put("streakDays", prefs.streakDays)
                put("masteredCards", JSONArray(prefs.masteredFlashcardIds.toList()))
            })

            put("jobs", JSONArray().apply {
                jobs.forEach { j ->
                    put(JSONObject().apply {
                        put("date", j.date)
                        put("process", j.process)
                        put("position", j.position)
                        put("baseMaterial", j.baseMaterial)
                        put("jointType", j.jointType)
                        put("rodFillerAndSize", j.rodFillerAndSize)
                        put("tigCup", j.tigCup)
                        put("tigColletBody", j.tigColletBody)
                        put("tigBackCap", j.tigBackCap)
                        put("tigTungsten", j.tigTungsten)
                        put("amperage", j.amperage)
                        put("voltage", j.voltage)
                        put("gasFlow", j.gasFlow)
                        put("measuringTools", j.measuringTools)
                        put("ppeChecked", j.ppeChecked)
                        put("defectTags", j.defectTags)
                        put("trainerFeedback", j.trainerFeedback)
                        put("status", j.status)
                        put("createdAt", j.createdAt)
                    })
                }
            })

            put("tasks", JSONArray().apply {
                tasks.forEach { t ->
                    put(JSONObject().apply {
                        put("titleEn", t.titleEn)
                        put("titleBn", t.titleBn)
                        put("category", t.category)
                        put("isDone", t.isDone)
                        put("createdAt", t.createdAt)
                    })
                }
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

    fun exportJobsToCsv(jobs: List<WeldJobEntity>): String {
        val sb = StringBuilder()
        sb.appendLine(
            "date,process,position,baseMaterial,jointType,rodFillerAndSize,tigCup,tigColletBody,tigBackCap,tigTungsten,amperage,voltage,gasFlow,measuringTools,ppeChecked,defectTags,trainerFeedback,status"
        )
        jobs.forEach { j ->
            sb.appendLine(
                listOf(
                    j.date, j.process, j.position, j.baseMaterial, j.jointType,
                    j.rodFillerAndSize, j.tigCup, j.tigColletBody, j.tigBackCap, j.tigTungsten,
                    j.amperage, j.voltage, j.gasFlow, j.measuringTools,
                    j.ppeChecked.toString(), j.defectTags, j.trainerFeedback, j.status
                ).joinToString(",") { escapeCsv(it) }
            )
        }
        return sb.toString()
    }

    fun importBackup(rawContent: String): BackupPayload {
        val trimmed = rawContent.trim()
        return if (trimmed.startsWith("{")) {
            parseJsonBackup(trimmed)
        } else {
            parseCsvJobs(trimmed)
        }
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

        val jobsList = mutableListOf<WeldJobEntity>()
        val jobsArr = root.optJSONArray("jobs") ?: JSONArray()
        for (i in 0 until jobsArr.length()) {
            val o = jobsArr.getJSONObject(i)
            jobsList.add(
                WeldJobEntity(
                    date = o.optString("date", ""),
                    process = o.optString("process", "SMAW"),
                    position = o.optString("position", "1G"),
                    baseMaterial = o.optString("baseMaterial", "CS"),
                    jointType = o.optString("jointType", "Butt"),
                    rodFillerAndSize = o.optString("rodFillerAndSize", ""),
                    tigCup = o.optString("tigCup", ""),
                    tigColletBody = o.optString("tigColletBody", ""),
                    tigBackCap = o.optString("tigBackCap", ""),
                    tigTungsten = o.optString("tigTungsten", ""),
                    amperage = o.optString("amperage", ""),
                    voltage = o.optString("voltage", ""),
                    gasFlow = o.optString("gasFlow", ""),
                    measuringTools = o.optString("measuringTools", ""),
                    ppeChecked = o.optBoolean("ppeChecked", true),
                    defectTags = o.optString("defectTags", "Clean/Pass"),
                    trainerFeedback = o.optString("trainerFeedback", ""),
                    status = o.optString("status", "Pass"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val tasksList = mutableListOf<DailyTaskEntity>()
        val tasksArr = root.optJSONArray("tasks") ?: JSONArray()
        for (i in 0 until tasksArr.length()) {
            val o = tasksArr.getJSONObject(i)
            tasksList.add(
                DailyTaskEntity(
                    titleEn = o.optString("titleEn", ""),
                    titleBn = o.optString("titleBn", ""),
                    category = o.optString("category", "PRACTICAL"),
                    isDone = o.optBoolean("isDone", false),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
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
            jobs = jobsList,
            tasks = tasksList,
            quizAttempts = attemptsList,
            checklists = checkList,
            languageCode = prefsObj?.optString("language")?.takeIf { it.isNotBlank() },
            traineeName = prefsObj?.optString("traineeName")?.takeIf { it.isNotBlank() },
            targetTrade = prefsObj?.optString("targetTrade")?.takeIf { it.isNotBlank() },
            streakDays = if (prefsObj?.has("streakDays") == true) prefsObj.optInt("streakDays") else null,
            masteredCards = if (masteredList.isEmpty()) null else masteredList
        )
    }

    private fun parseCsvJobs(csvStr: String): BackupPayload {
        val lines = csvStr.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) throw IllegalArgumentException("CSV file has no job rows.")
        val jobs = mutableListOf<WeldJobEntity>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size < 6) continue
            fun col(idx: Int, default: String = "") = cols.getOrElse(idx) { default }
            jobs.add(
                WeldJobEntity(
                    date = col(0),
                    process = col(1, "SMAW"),
                    position = col(2, "1G"),
                    baseMaterial = col(3, "CS"),
                    jointType = col(4, "Butt"),
                    rodFillerAndSize = col(5),
                    tigCup = col(6),
                    tigColletBody = col(7),
                    tigBackCap = col(8),
                    tigTungsten = col(9),
                    amperage = col(10),
                    voltage = col(11),
                    gasFlow = col(12),
                    measuringTools = col(13),
                    ppeChecked = col(14, "true").equals("true", ignoreCase = true),
                    defectTags = col(15, "Clean/Pass"),
                    trainerFeedback = col(16),
                    status = col(17, "Pass")
                )
            )
        }
        return BackupPayload(
            jobs = jobs,
            tasks = emptyList(),
            quizAttempts = emptyList(),
            checklists = emptyList(),
            languageCode = null,
            traineeName = null,
            targetTrade = null,
            streakDays = null,
            masteredCards = null
        )
    }

    private fun escapeCsv(value: String): String {
        val needsQuote = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (needsQuote) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString())
                current.clear()
            } else {
                current.append(ch)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
