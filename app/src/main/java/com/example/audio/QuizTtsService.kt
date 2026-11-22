package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.runtime.Stable
import com.example.data.content.RandomizedQuizQuestion
import com.example.data.local.SpeechSpeedPreset
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

enum class TtsPlaybackStatus {
    INITIALIZING,
    READY,
    SPEAKING,
    STOPPED,
    UNAVAILABLE
}

data class TtsPlaybackState(
    val status: TtsPlaybackStatus = TtsPlaybackStatus.INITIALIZING,
    val activeItemId: String? = null,
    val isReady: Boolean = false,
    val isSpeaking: Boolean = false,
    val errorMessage: String? = null
)

@Stable
interface WeldingTtsService {
    val playbackState: StateFlow<TtsPlaybackState>
    fun updateSettings(language: AppLanguage, speedPreset: SpeechSpeedPreset)
    fun speakText(rawText: String, itemId: String? = null, onComplete: (() -> Unit)? = null)
    fun speakQuizQuestion(
        item: RandomizedQuizQuestion,
        questionNumber: Int,
        totalQuestions: Int,
        includeOptions: Boolean = false,
        onComplete: (() -> Unit)? = null
    )
    fun speakQuizExplanation(
        item: RandomizedQuizQuestion,
        isCorrect: Boolean,
        onComplete: (() -> Unit)? = null
    )
    fun stop()
    fun release()
}

object QuizSpeechFormatter {

    fun formatQuizQuestionPrompt(
        item: RandomizedQuizQuestion,
        questionNumber: Int,
        totalQuestions: Int,
        includeOptions: Boolean,
        language: AppLanguage
    ): String {
        val qHeader = if (language == AppLanguage.BN) {
            "প্রশ্ন $questionNumber, মোট ${totalQuestions}টির মধ্যে।"
        } else {
            "Question $questionNumber of $totalQuestions."
        }
        val questionBody = item.source.question.get(language).trim()
        if (!includeOptions) {
            return "$qHeader $questionBody"
        }

        val sep = if (language == AppLanguage.BN) "।" else "."
        val optionsText = item.shuffledOptions.mapIndexed { idx, opt ->
            val letter = 'A' + idx
            val prefix = if (language == AppLanguage.BN) "অপশন $letter" else "Option $letter"
            "$prefix: ${opt.get(language).trim()}"
        }.joinToString("$sep ")

        return "$qHeader $questionBody$sep $optionsText$sep"
    }

    fun formatQuizExplanationPrompt(
        item: RandomizedQuizQuestion,
        isCorrect: Boolean,
        language: AppLanguage
    ): String {
        val statusPrefix = if (isCorrect) {
            AppStrings.quizCorrectAnnouncement.get(language)
        } else {
            val correctLetter = 'A' + item.correctShuffledIndex
            val correctText = item.shuffledOptions
                .getOrNull(item.correctShuffledIndex)
                ?.get(language)
                .orEmpty()
            if (language == AppLanguage.BN) {
                "${AppStrings.quizIncorrectAnnouncement.get(language)} সঠিক উত্তর অপশন $correctLetter: $correctText।"
            } else {
                "${AppStrings.quizIncorrectAnnouncement.get(language)} The correct answer is Option $correctLetter: $correctText."
            }
        }
        return "$statusPrefix ${item.source.explanation.get(language).trim()}"
    }

    fun prepareNaturalSpeechSegments(rawText: String, language: AppLanguage): List<String> {
        val orConnector = if (language == AppLanguage.BN) " বা " else " or "
        val toConnector = if (language == AppLanguage.BN) " থেকে " else " to "
        val litersPerMin = if (language == AppLanguage.BN) " লিটার প্রতি মিনিট" else " liters per minute"
        val approxWord = if (language == AppLanguage.BN) "প্রায় " else "approximately "
        val degreeWord = if (language == AppLanguage.BN) " ডিগ্রি" else " degrees"
        val plusWord = if (language == AppLanguage.BN) " প্লাস" else " plus"

        val cleaned = rawText
            .replace(Regex("```[\\s\\S]*?```"), " ")
            .replace(Regex("\\[(PASS|REVIEW)]", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(?:(?m)^|(?<=[.!?।]\\s))\\s*(?:[-*•]+|\\d+[.)])\\s+"), "")
            .replace(Regex("[*_`#>|]"), " ")
            .replace("~", approxWord)
            .replace("°", degreeWord)
            .replace("6G+", "6G$plusWord", ignoreCase = true)
            .replace("L/min", litersPerMin, ignoreCase = true)
            .replace("L/m", litersPerMin, ignoreCase = true)
            .replace(Regex("(\\d+)\\s*[–-]\\s*(\\d+)"), "$1$toConnector$2")
            .replace(Regex("([A-Za-z0-9]+)/([A-Za-z0-9]+)"), "$1$orConnector$2")
            .replace("•", ", ")
            .replace(Regex("\\r?\\n+"), ", ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()

        if (cleaned.isBlank()) return emptyList()

        val rawSentences = cleaned
            .split(Regex("(?<=[.!?।])\\s+"))
            .map { it.trim().trimStart('.', ',', ';') }
            .filter { it.isNotBlank() }

        // Merge consecutive clauses up to ~360 characters to keep spoken flow cohesive and avoid robotic stops
        val merged = mutableListOf<String>()
        val current = StringBuilder()
        for (sentence in rawSentences) {
            if (current.isEmpty()) {
                current.append(sentence)
            } else if (current.length + sentence.length + 1 <= 360) {
                current.append(' ').append(sentence)
            } else {
                merged.add(current.toString())
                current.clear()
                current.append(sentence)
            }
        }
        if (current.isNotEmpty()) {
            merged.add(current.toString())
        }
        return merged
    }
}

@Stable
class AndroidWeldingTtsService(
    context: Context
) : WeldingTtsService {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _playbackState = MutableStateFlow(TtsPlaybackState())
    override val playbackState: StateFlow<TtsPlaybackState> = _playbackState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var currentLanguage: AppLanguage = AppLanguage.EN
    private var currentSpeedPreset: SpeechSpeedPreset = SpeechSpeedPreset.NATURAL

    private var speechGeneration: Long = 0L
    private var activeFinalUtteranceId: String? = null
    private var activeOnComplete: (() -> Unit)? = null

    init {
        initializeEngine()
    }

    private fun initializeEngine() {
        try {
            tts = TextToSpeech(appContext) { status ->
                mainHandler.post {
                    if (status == TextToSpeech.SUCCESS) {
                        try {
                            tts?.setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                    .build()
                            )
                        } catch (_: Exception) {
                        }
                        applyNaturalTtsVoice(currentLanguage, currentSpeedPreset)
                        attachProgressListener()
                        _playbackState.update {
                            it.copy(
                                status = TtsPlaybackStatus.READY,
                                isReady = true,
                                isSpeaking = false,
                                errorMessage = null
                            )
                        }
                    } else {
                        _playbackState.update {
                            it.copy(
                                status = TtsPlaybackStatus.UNAVAILABLE,
                                isReady = false,
                                isSpeaking = false,
                                errorMessage = AppStrings.quizTtsUnavailable.get(currentLanguage)
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            _playbackState.update {
                it.copy(
                    status = TtsPlaybackStatus.UNAVAILABLE,
                    isReady = false,
                    isSpeaking = false,
                    errorMessage = AppStrings.quizTtsUnavailable.get(currentLanguage)
                )
            }
        }
    }

    private fun attachProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    if (utteranceId?.startsWith("weld_${speechGeneration}_") == true) {
                        _playbackState.update {
                            it.copy(
                                status = TtsPlaybackStatus.SPEAKING,
                                isSpeaking = true,
                                errorMessage = null
                            )
                        }
                    }
                }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    if (utteranceId == null || utteranceId == activeFinalUtteranceId) {
                        val cb = activeOnComplete
                        activeOnComplete = null
                        activeFinalUtteranceId = null
                        _playbackState.update {
                            it.copy(
                                status = if (it.isReady) TtsPlaybackStatus.READY else TtsPlaybackStatus.STOPPED,
                                activeItemId = null,
                                isSpeaking = false
                            )
                        }
                        cb?.invoke()
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                handleUtteranceError(utteranceId)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                handleUtteranceError(utteranceId)
            }
        })
    }

    private fun handleUtteranceError(utteranceId: String?) {
        mainHandler.post {
            if (utteranceId == null || utteranceId.startsWith("weld_${speechGeneration}_")) {
                activeOnComplete = null
                activeFinalUtteranceId = null
                _playbackState.update {
                    it.copy(
                        status = if (it.isReady) TtsPlaybackStatus.READY else TtsPlaybackStatus.UNAVAILABLE,
                        activeItemId = null,
                        isSpeaking = false
                    )
                }
            }
        }
    }

    override fun updateSettings(language: AppLanguage, speedPreset: SpeechSpeedPreset) {
        currentLanguage = language
        currentSpeedPreset = speedPreset
        if (_playbackState.value.isReady) {
            applyNaturalTtsVoice(language, speedPreset)
        }
    }

    override fun speakText(rawText: String, itemId: String?, onComplete: (() -> Unit)?) {
        val segments = QuizSpeechFormatter.prepareNaturalSpeechSegments(rawText, currentLanguage)
        if (segments.isEmpty() || !_playbackState.value.isReady) {
            onComplete?.invoke()
            return
        }

        applyNaturalTtsVoice(currentLanguage, currentSpeedPreset)
        speechGeneration += 1L
        val gen = speechGeneration
        val batchPrefix = "weld_$gen"
        activeOnComplete = onComplete
        activeFinalUtteranceId = "${batchPrefix}_${segments.lastIndex}"

        _playbackState.update {
            it.copy(
                status = TtsPlaybackStatus.SPEAKING,
                activeItemId = itemId,
                isSpeaking = true,
                errorMessage = null
            )
        }

        segments.forEachIndexed { idx, clause ->
            val mode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val uttId = "${batchPrefix}_$idx"
            tts?.speak(clause, mode, null, uttId)
        }
    }

    override fun speakQuizQuestion(
        item: RandomizedQuizQuestion,
        questionNumber: Int,
        totalQuestions: Int,
        includeOptions: Boolean,
        onComplete: (() -> Unit)?
    ) {
        val prompt = QuizSpeechFormatter.formatQuizQuestionPrompt(
            item = item,
            questionNumber = questionNumber,
            totalQuestions = totalQuestions,
            includeOptions = includeOptions,
            language = currentLanguage
        )
        speakText(prompt, itemId = "quiz_${item.source.id}", onComplete = onComplete)
    }

    override fun speakQuizExplanation(
        item: RandomizedQuizQuestion,
        isCorrect: Boolean,
        onComplete: (() -> Unit)?
    ) {
        val explanationPrompt = QuizSpeechFormatter.formatQuizExplanationPrompt(
            item = item,
            isCorrect = isCorrect,
            language = currentLanguage
        )
        speakText(explanationPrompt, itemId = "exp_${item.source.id}", onComplete = onComplete)
    }

    override fun stop() {
        speechGeneration += 1L
        activeOnComplete = null
        activeFinalUtteranceId = null
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        _playbackState.update {
            it.copy(
                status = if (it.isReady) TtsPlaybackStatus.STOPPED else it.status,
                activeItemId = null,
                isSpeaking = false
            )
        }
    }

    override fun release() {
        speechGeneration += 1L
        activeOnComplete = null
        activeFinalUtteranceId = null
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        _playbackState.value = TtsPlaybackState(
            status = TtsPlaybackStatus.STOPPED,
            activeItemId = null,
            isReady = false,
            isSpeaking = false
        )
    }

    private fun applyNaturalTtsVoice(language: AppLanguage, speedPreset: SpeechSpeedPreset) {
        val engine = tts ?: return
        val primaryLocale = if (language == AppLanguage.BN) Locale("bn", "BD") else Locale.US
        val secondaryLocale = if (language == AppLanguage.BN) Locale("bn", "IN") else Locale.UK

        val bestVoice = selectBestNaturalVoice(engine, primaryLocale, secondaryLocale)
        if (bestVoice != null) {
            engine.voice = bestVoice
        } else {
            val res = engine.setLanguage(primaryLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                val fallback = engine.setLanguage(secondaryLocale)
                if (fallback == TextToSpeech.LANG_MISSING_DATA || fallback == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }
            }
        }

        val rate = if (language == AppLanguage.BN) {
            (speedPreset.rateMultiplier - 0.03f).coerceIn(0.82f, 1.15f)
        } else {
            speedPreset.rateMultiplier
        }
        engine.setSpeechRate(rate)
        engine.setPitch(1.0f)
    }

    private fun selectBestNaturalVoice(
        engine: TextToSpeech,
        primaryLocale: Locale,
        secondaryLocale: Locale
    ): Voice? {
        return try {
            val voices = engine.voices ?: return null
            val targetLang = primaryLocale.language
            voices
                .filter { v ->
                    v.locale != null &&
                        v.locale.language.equals(targetLang, ignoreCase = true) &&
                        !v.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
                }
                .maxByOrNull { v ->
                    var score = 0
                    if (v.locale == primaryLocale) score += 200
                    else if (v.locale == secondaryLocale) score += 130
                    if (v.quality >= Voice.QUALITY_VERY_HIGH) score += 120
                    else if (v.quality >= Voice.QUALITY_HIGH) score += 80
                    else if (v.quality >= Voice.QUALITY_NORMAL) score += 30
                    if (!v.isNetworkConnectionRequired) score += 20
                    if (v.latency <= Voice.LATENCY_NORMAL) score += 15
                    score
                }
        } catch (_: Exception) {
            null
        }
    }
}
