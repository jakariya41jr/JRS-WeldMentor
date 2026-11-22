package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AndroidWeldingTtsService
import com.example.audio.QuizSpeechFormatter
import com.example.audio.TtsPlaybackState
import com.example.audio.TtsPlaybackStatus
import com.example.audio.VoiceAssistantController
import com.example.audio.WeldingTtsService
import com.example.data.backup.BackupManager
import com.example.data.content.CurrencyCatalog
import com.example.data.content.RandomizedQuizQuestion
import com.example.data.content.VivaKnowledgeBase
import com.example.data.content.WorkshopGuidesData
import com.example.data.local.AppThemeMode
import com.example.data.local.ChecklistProgressEntity
import com.example.data.local.DEFAULT_GEMINI_MODEL
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.SpeechSpeedPreset
import com.example.data.local.UserPreferences
import com.example.data.remote.GeminiRestClient
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.CenterpiecePhase
import com.example.ui.components.WeldEnergyOrbCenterpiece
import com.example.ui.components.pulseProfileFor
import com.example.ui.screens.VivaQuizScreen
import com.example.ui.theme.JrsWeldMentorTheme
import com.example.ui.viewmodel.WeldUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `app name and developer attribution match JRS WeldMentor standards`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JRS WeldMentor", appName)
        assertEquals("Md Jakariya Hasan", AppStrings.devName)
        assertEquals("Jakariya41jr.xyz", AppStrings.devWebsite)
        assertFalse(AppStrings.builtBy.en.contains("Zakaria", ignoreCase = true))
        assertFalse(GeminiRestClient.STRICT_SYSTEM_INSTRUCTION.contains("Zakaria", ignoreCase = true))
    }

    @Test
    fun `json educational backup round trip preserves quiz attempts and checklists`() {
        val sampleAttempt = QuizAttemptEntity(
            questionId = "q_what_is_welding",
            topicEn = "Welding Basics",
            topicBn = "ওয়েল্ডিং বেসিক",
            isCorrect = true
        )
        val sampleChecklist = ChecklistProgressEntity(
            itemId = "chk_6g_stance",
            groupName = "STANCE",
            isChecked = true
        )
        val json = BackupManager.exportToJson(
            attempts = listOf(sampleAttempt),
            checklists = listOf(sampleChecklist),
            prefs = UserPreferences(
                language = AppLanguage.BN,
                traineeName = "Test Learner",
                targetTrade = "6G TIG/SMAW",
                masteredFlashcardIds = setOf("fc_e7018", "fc_er70s2")
            )
        )
        val parsed = BackupManager.importBackup(json)
        assertEquals(1, parsed.quizAttempts.size)
        assertEquals("q_what_is_welding", parsed.quizAttempts.first().questionId)
        assertEquals(1, parsed.checklists.size)
        assertEquals("chk_6g_stance", parsed.checklists.first().itemId)
        assertEquals("bn", parsed.languageCode)
        assertEquals("Test Learner", parsed.traineeName)
        assertEquals("6G TIG/SMAW", parsed.targetTrade)
        assertEquals(setOf("fc_e7018", "fc_er70s2"), parsed.masteredCards)
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
        assertFalse(failEval.passed)
    }

    @Test
    fun `currency catalog supports all pinned destinations and offline conversion`() {
        val rates = CurrencyCatalog.buildDefaultRatesMap()
        CurrencyCatalog.pinnedWeldingCodes.forEach { code ->
            assertTrue("Missing pinned code $code", rates.containsKey(code))
        }
        assertTrue(rates.containsKey("BDT"))
        assertTrue(CurrencyCatalog.allCurrencies.size >= 90)
        val sarToBdt = (rates["BDT"] ?: 0.0) / (rates["SAR"] ?: 1.0)
        assertTrue(sarToBdt > 25.0)
    }

    @Test
    fun `default gemini model is gemini-3_0-flash and speech normalizer smooths output`() {
        assertEquals("gemini-3.0-flash", DEFAULT_GEMINI_MODEL)
        assertEquals("gemini-3.0-flash", GeminiRestClient.DEFAULT_MODEL)
        assertEquals("gemini-3.0-flash", UserPreferences().geminiModel)

        val enSegments = VoiceAssistantController.prepareNaturalSpeechSegments(
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
        val deckA = VivaKnowledgeBase.buildShuffledQuizDeck(original, Random(42))
        val deckB = VivaKnowledgeBase.buildShuffledQuizDeck(original, Random(99))

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
        assertEquals(AppThemeMode.DARK, defaults.themeMode)
        assertEquals(SpeechSpeedPreset.NATURAL, defaults.speechSpeedPreset)
        assertTrue(defaults.bargeInEnabled)
        assertTrue(defaults.autoSpeakAi)
        assertEquals(0.97f, SpeechSpeedPreset.NATURAL.rateMultiplier, 0.001f)
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromCode("light"))

        val mergedSegments = VoiceAssistantController.prepareNaturalSpeechSegments(
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

    @Test
    fun `energy orb pulse profiles differentiate idle, listening, thinking, and speaking states`() {
        val idle = pulseProfileFor(CenterpiecePhase.IDLE)
        val listening = pulseProfileFor(CenterpiecePhase.LISTENING)
        val thinking = pulseProfileFor(CenterpiecePhase.THINKING)
        val speaking = pulseProfileFor(CenterpiecePhase.SPEAKING)

        assertTrue(speaking.maxScale > idle.maxScale)
        assertTrue(listening.maxScale > idle.maxScale)
        assertTrue(thinking.orbitDurationMs < listening.orbitDurationMs)
        assertTrue(listening.orbitDurationMs < idle.orbitDurationMs)
        assertTrue(speaking.rippleCount > idle.rippleCount)
        assertTrue(idle.expandDurationMs > speaking.expandDurationMs)
    }

    @Test
    fun `energy orb centerpiece renders and transitions through idle, listening, thinking, and speaking`() {
        composeTestRule.mainClock.autoAdvance = false
        var currentPhase by mutableStateOf(CenterpiecePhase.IDLE)
        var orbClicks = 0

        composeTestRule.setContent {
            JrsWeldMentorTheme(themeMode = AppThemeMode.DARK) {
                WeldEnergyOrbCenterpiece(
                    phase = currentPhase,
                    title = AppStrings.aiMentorTitle.en,
                    subtitle = AppStrings.aiStrictScopeBadge.en,
                    badgeText = "Active • gemini-3.0-flash",
                    lang = AppLanguage.EN,
                    onOrbClick = { orbClicks++ }
                )
            }
        }

        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.onNodeWithTag("mentor_central_orb").assertIsDisplayed()
        composeTestRule.onNodeWithTag("orb_status_text").assertTextEquals("TAP TO START")

        composeTestRule.onNodeWithTag("mentor_central_orb").performClick()
        composeTestRule.mainClock.advanceTimeBy(50)
        assertEquals(1, orbClicks)

        currentPhase = CenterpiecePhase.LISTENING
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.onNodeWithTag("orb_status_text").assertTextEquals("LISTENING...")
        composeTestRule.onNodeWithTag("orb_tap_to_stop_btn").assertIsDisplayed()

        currentPhase = CenterpiecePhase.THINKING
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.onNodeWithTag("orb_status_text").assertTextEquals("THINKING...")

        currentPhase = CenterpiecePhase.SPEAKING
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.onNodeWithTag("orb_status_text").assertTextEquals("SPEAKING...")
        composeTestRule.onNodeWithTag("orb_tap_to_interrupt_btn").assertIsDisplayed()
    }

    @Test
    fun `quiz speech formatter builds bilingual spoken prompts for randomized questions and options`() {
        val deck = VivaKnowledgeBase.buildShuffledQuizDeck(VivaKnowledgeBase.quizQuestions.take(3), Random(17))
        val item = deck.first()

        val enPrompt = QuizSpeechFormatter.formatQuizQuestionPrompt(
            item = item,
            questionNumber = 1,
            totalQuestions = 3,
            includeOptions = true,
            language = AppLanguage.EN
        )
        assertTrue(enPrompt.startsWith("Question 1 of 3."))
        assertTrue(enPrompt.contains(item.source.question.en))
        assertTrue(enPrompt.contains("Option A: ${item.shuffledOptions[0].en}"))
        assertTrue(enPrompt.contains("Option D: ${item.shuffledOptions[3].en}"))

        val bnExplanation = QuizSpeechFormatter.formatQuizExplanationPrompt(
            item = item,
            isCorrect = false,
            language = AppLanguage.BN
        )
        val expectedLetter = 'A' + item.correctShuffledIndex
        assertTrue(bnExplanation.contains("ভুল উত্তর"))
        assertTrue(bnExplanation.contains("অপশন $expectedLetter"))
        assertTrue(bnExplanation.contains(item.source.explanation.bn))

        val context = ApplicationProvider.getApplicationContext<Context>()
        val realService = AndroidWeldingTtsService(context)
        realService.updateSettings(AppLanguage.EN, SpeechSpeedPreset.CRISP)
        realService.speakQuizQuestion(item, 1, 3, includeOptions = false)
        realService.stop()
        realService.release()
    }

    @Test
    fun `viva quiz automatically reads randomized questions aloud via WeldingTtsService`() {
        val spokenQuestions = mutableListOf<Pair<String, Boolean>>()
        val spokenExplanations = mutableListOf<String>()
        var stopCount = 0

        val fakeTts = object : WeldingTtsService {
            private val _state = MutableStateFlow(
                TtsPlaybackState(
                    status = TtsPlaybackStatus.READY,
                    isReady = true,
                    isSpeaking = false
                )
            )
            override val playbackState: StateFlow<TtsPlaybackState> = _state.asStateFlow()

            override fun updateSettings(language: AppLanguage, speedPreset: SpeechSpeedPreset) {}

            override fun speakText(rawText: String, itemId: String?, onComplete: (() -> Unit)?) {
                _state.value = _state.value.copy(status = TtsPlaybackStatus.SPEAKING, activeItemId = itemId, isSpeaking = true)
            }

            override fun speakQuizQuestion(
                item: RandomizedQuizQuestion,
                questionNumber: Int,
                totalQuestions: Int,
                includeOptions: Boolean,
                onComplete: (() -> Unit)?
            ) {
                spokenQuestions += item.source.id to includeOptions
                _state.value = _state.value.copy(
                    status = TtsPlaybackStatus.SPEAKING,
                    activeItemId = "quiz_${item.source.id}",
                    isSpeaking = true
                )
            }

            override fun speakQuizExplanation(
                item: RandomizedQuizQuestion,
                isCorrect: Boolean,
                onComplete: (() -> Unit)?
            ) {
                spokenExplanations += item.source.id
                _state.value = _state.value.copy(
                    status = TtsPlaybackStatus.SPEAKING,
                    activeItemId = "exp_${item.source.id}",
                    isSpeaking = true
                )
            }

            override fun stop() {
                stopCount++
                _state.value = _state.value.copy(status = TtsPlaybackStatus.STOPPED, activeItemId = null, isSpeaking = false)
            }

            override fun release() {}
        }

        composeTestRule.setContent {
            JrsWeldMentorTheme(themeMode = AppThemeMode.DARK) {
                VivaQuizScreen(
                    uiState = WeldUiState(preferences = UserPreferences(autoSpeakAi = true)),
                    selectedSubMode = 0,
                    onSelectSubMode = {},
                    onRecordAttempt = { _, _, _, _ -> },
                    onResetStats = {},
                    onToggleMasteredCard = { _, _ -> },
                    onSubmitDelegateAnswer = { _, _ -> },
                    onNextDelegateQuestion = {},
                    onShuffleDelegateQuestions = {},
                    onResetDelegateFeedback = {},
                    ttsServiceOverride = fakeTts
                )
            }
        }

        composeTestRule.waitForIdle()
        assertEquals(1, spokenQuestions.size)
        assertFalse(spokenQuestions.first().second)
        composeTestRule.onNodeWithTag("quiz_tts_status_pill").assertIsDisplayed()

        // Toggle reading A-D options aloud
        composeTestRule.onNodeWithTag("quiz_read_options_chip").performClick()
        composeTestRule.waitForIdle()
        assertEquals(2, spokenQuestions.size)
        assertTrue(spokenQuestions.last().second)

        // Answering stops question playback and speaks explanation
        composeTestRule.onNodeWithTag("quiz_option_0").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue(stopCount >= 1)
        assertEquals(1, spokenExplanations.size)
        composeTestRule.onNodeWithTag("quiz_speak_explanation_btn").performScrollTo().assertIsDisplayed()

        // Advancing to next randomized question stops explanation and reads next question aloud
        composeTestRule.onNodeWithTag("quiz_next_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(3, spokenQuestions.size)
    }
}
