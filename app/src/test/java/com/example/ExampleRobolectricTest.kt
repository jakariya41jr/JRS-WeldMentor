package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.content.VivaKnowledgeBase
import com.example.data.content.WorkshopGuidesData
import com.example.data.local.UserPreferences
import com.example.data.local.WeldJobEntity
import com.example.data.remote.GeminiRestClient
import com.example.i18n.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches JRS WeldMentor`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JRS WeldMentor", appName)
    }

    @Test
    fun `json and csv backup round trip preserves weld jobs`() {
        val sampleJob = WeldJobEntity(
            date = "2026-11-21",
            process = "GTAW/TIG",
            position = "6G+6\"GTAW",
            baseMaterial = "SS",
            jointType = "Butt",
            rodFillerAndSize = "316L 2.4mm",
            tigCup = "#7",
            tigColletBody = "Gas Lens 2.4mm",
            tigBackCap = "Short",
            tigTungsten = "2% Lanthanated 2.4mm",
            amperage = "95A",
            voltage = "",
            gasFlow = "12 L/min",
            measuringTools = "Angle Locator, Vernier Caliper",
            ppeChecked = true,
            defectTags = "Clean/Pass",
            trainerFeedback = "Smooth root penetration",
            status = "Pass"
        )
        val json = BackupManager.exportToJson(
            jobs = listOf(sampleJob),
            tasks = emptyList(),
            attempts = emptyList(),
            checklists = emptyList(),
            prefs = UserPreferences(language = AppLanguage.BN, traineeName = "Test Welder")
        )
        val parsedJson = BackupManager.importBackup(json)
        assertEquals(1, parsedJson.jobs.size)
        assertEquals("6G+6\"GTAW", parsedJson.jobs.first().position)
        assertEquals("bn", parsedJson.languageCode)

        val csv = BackupManager.exportJobsToCsv(listOf(sampleJob))
        val parsedCsv = BackupManager.importBackup(csv)
        assertEquals(1, parsedCsv.jobs.size)
        assertEquals("316L 2.4mm", parsedCsv.jobs.first().rodFillerAndSize)
    }

    @Test
    fun `bilingual offline knowledge base and strict AI instruction are populated`() {
        assertTrue(VivaKnowledgeBase.quizQuestions.size >= 18)
        assertTrue(VivaKnowledgeBase.flashCards.size >= 8)
        assertTrue(WorkshopGuidesData.theoryTopics.size >= 4)
        assertTrue(WorkshopGuidesData.defectGuides.size >= 5)
        assertTrue(GeminiRestClient.STRICT_SYSTEM_INSTRUCTION.contains("Md Jakariya Hasan"))
        assertTrue(GeminiRestClient.STRICT_SYSTEM_INSTRUCTION.contains("Jakariya41jr.xyz"))
    }

    @Test
    fun `offline delegate viva evaluator scores welding answers and avoids false positives`() {
        val whatIsWelding = VivaKnowledgeBase.quizQuestions.first { it.id == "q_what_is_welding" }
        val passEval = VivaKnowledgeBase.evaluateOfflineDelegateAnswer(
            question = whatIsWelding,
            userAnswer = "Welding permanently joins two metals by melting and fusion",
            language = AppLanguage.EN
        )
        assertTrue(passEval.passed)

        val failEval = VivaKnowledgeBase.evaluateOfflineDelegateAnswer(
            question = whatIsWelding,
            userAnswer = "I like football",
            language = AppLanguage.EN
        )
        assertTrue(!failEval.passed)
    }

    @Test
    fun `currency catalog supports all pinned destinations and offline conversion`() {
        val rates = com.example.data.content.CurrencyCatalog.buildDefaultRatesMap()
        com.example.data.content.CurrencyCatalog.pinnedWeldingCodes.forEach { code ->
            assertTrue("Missing pinned code $code", rates.containsKey(code))
        }
        assertTrue(rates.containsKey("BDT"))
        assertTrue(com.example.data.content.CurrencyCatalog.allCurrencies.size >= 90)
        val sarToBdt = (rates["BDT"] ?: 0.0) / (rates["SAR"] ?: 1.0)
        assertTrue(sarToBdt > 25.0)
    }

    @Test
    fun `default gemini model is gemini-3_0-flash and speech normalizer smooths output`() {
        assertEquals("gemini-3.0-flash", com.example.data.local.DEFAULT_GEMINI_MODEL)
        assertEquals("gemini-3.0-flash", GeminiRestClient.DEFAULT_MODEL)
        assertEquals("gemini-3.0-flash", UserPreferences().geminiModel)

        val enSegments = com.example.audio.VoiceAssistantController.prepareNaturalSpeechSegments(
            rawText = "**Root Pass:** Use 90-100A with `12 L/min` Argon / shielding gas.",
            language = AppLanguage.EN
        )
        assertTrue(enSegments.isNotEmpty())
        val joinedEn = enSegments.joinToString(" ")
        assertTrue(joinedEn.contains("90 to 100A"))
        assertTrue(joinedEn.contains("liters per minute"))
        assertTrue(!joinedEn.contains("**") && !joinedEn.contains("`"))
    }

    @Test
    fun `quiz randomization shuffles questions and A-B-C-D options while preserving correct mapping`() {
        val original = VivaKnowledgeBase.quizQuestions
        val deckA = VivaKnowledgeBase.buildShuffledQuizDeck(original, kotlin.random.Random(42))
        val deckB = VivaKnowledgeBase.buildShuffledQuizDeck(original, kotlin.random.Random(99))

        assertEquals(original.size, deckA.size)
        assertTrue(deckA.map { it.source.id } != original.map { it.id })
        assertTrue(deckA.map { it.source.id } != deckB.map { it.source.id })

        deckA.forEach { item ->
            val expectedCorrect = item.source.options[item.source.correctIndex]
            assertEquals(expectedCorrect, item.shuffledOptions[item.correctShuffledIndex])
        }
        assertTrue(deckA.any { it.correctShuffledIndex >= 2 })
    }

    @Test
    fun `theme mode, voice speed presets, and continuous tts clause merging work cleanly`() {
        val defaults = UserPreferences()
        assertEquals(com.example.data.local.AppThemeMode.DARK, defaults.themeMode)
        assertEquals(com.example.data.local.SpeechSpeedPreset.NATURAL, defaults.speechSpeedPreset)
        assertTrue(defaults.bargeInEnabled)
        assertTrue(defaults.autoSpeakAi)
        assertEquals(0.97f, com.example.data.local.SpeechSpeedPreset.NATURAL.rateMultiplier, 0.001f)
        assertEquals(
            com.example.data.local.AppThemeMode.LIGHT,
            com.example.data.local.AppThemeMode.fromCode("light")
        )

        val mergedSegments = com.example.audio.VoiceAssistantController.prepareNaturalSpeechSegments(
            rawText = "1. Clean the bevel. 2. Set amperage to 95A. - Keep a tight arc.",
            language = AppLanguage.EN
        )
        assertEquals(1, mergedSegments.size)
        val spoken = mergedSegments.first()
        assertTrue(spoken.contains("Clean the bevel."))
        assertTrue(spoken.contains("Set amperage to 95A."))
        assertTrue(spoken.contains("Keep a tight arc."))
        assertTrue(!spoken.contains("1. ") && !spoken.contains("2. "))
    }
}
