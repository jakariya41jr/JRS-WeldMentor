package com.example.audio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import java.util.Locale

@Stable
class VoiceAssistantController(
    private val context: Context,
    private val requestAudioPermission: () -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    var isListening by mutableStateOf(false)
        private set

    var isSpeaking by mutableStateOf(false)
        private set

    var ttsReady by mutableStateOf(false)
        private set

    var voiceError by mutableStateOf<String?>(null)
        private set

    var partialTranscript by mutableStateOf("")
        private set

    private var currentLanguage: AppLanguage = AppLanguage.EN
    private var pendingSpeechRequest: PendingSpeechRequest? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var activeFinalUtteranceId: String? = null
    private var activeOnSpeakComplete: (() -> Unit)? = null

    private data class PendingSpeechRequest(
        val silentTimeoutFallback: Boolean,
        val onNoSpeech: (() -> Unit)?,
        val onResult: (String) -> Unit
    )

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                mainHandler.post {
                    if (status == TextToSpeech.SUCCESS) {
                        ttsReady = true
                        applyNaturalTtsVoice(currentLanguage)
                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                mainHandler.post { isSpeaking = true }
                            }

                            override fun onDone(utteranceId: String?) {
                                mainHandler.post {
                                    if (utteranceId == null || utteranceId == activeFinalUtteranceId) {
                                        isSpeaking = false
                                        val cb = activeOnSpeakComplete
                                        activeOnSpeakComplete = null
                                        activeFinalUtteranceId = null
                                        cb?.invoke()
                                    }
                                }
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                mainHandler.post {
                                    isSpeaking = false
                                    activeOnSpeakComplete = null
                                    activeFinalUtteranceId = null
                                }
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                mainHandler.post {
                                    isSpeaking = false
                                    activeOnSpeakComplete = null
                                    activeFinalUtteranceId = null
                                }
                            }
                        })
                    } else {
                        ttsReady = false
                    }
                }
            }
        } catch (_: Exception) {
            ttsReady = false
        }
    }

    fun updateLanguage(language: AppLanguage) {
        currentLanguage = language
        if (ttsReady) {
            applyNaturalTtsVoice(language)
        }
    }

    private fun applyNaturalTtsVoice(language: AppLanguage) {
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

        // Measured, warm cadence prevents choppy or rushed robotic delivery
        engine.setSpeechRate(if (language == AppLanguage.BN) 0.94f else 0.97f)
        engine.setPitch(1.01f)
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
                    else if (v.locale == secondaryLocale) score += 120
                    if (v.quality >= Voice.QUALITY_VERY_HIGH) score += 105
                    else if (v.quality >= Voice.QUALITY_HIGH) score += 70
                    else if (v.quality >= Voice.QUALITY_NORMAL) score += 25
                    if (!v.isNetworkConnectionRequired) score += 25
                    if (v.latency <= Voice.LATENCY_NORMAL) score += 15
                    score
                }
        } catch (_: Exception) {
            null
        }
    }

    fun speak(rawText: String, onComplete: (() -> Unit)? = null) {
        val segments = prepareNaturalSpeechSegments(rawText, currentLanguage)
        if (segments.isEmpty() || !ttsReady) {
            onComplete?.invoke()
            return
        }
        voiceError = null
        applyNaturalTtsVoice(currentLanguage)

        val batchPrefix = "weld_${System.currentTimeMillis()}"
        activeOnSpeakComplete = onComplete
        activeFinalUtteranceId = "${batchPrefix}_${segments.lastIndex}"

        segments.forEachIndexed { idx, clause ->
            val mode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val uttId = "${batchPrefix}_$idx"
            tts?.speak(clause, mode, null, uttId)
        }
    }

    fun stopSpeaking() {
        activeOnSpeakComplete = null
        activeFinalUtteranceId = null
        tts?.stop()
        isSpeaking = false
    }

    fun startListening(
        silentTimeoutFallback: Boolean = false,
        onNoSpeech: (() -> Unit)? = null,
        onResult: (String) -> Unit
    ) {
        voiceError = null
        partialTranscript = ""
        stopSpeaking()

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            pendingSpeechRequest = PendingSpeechRequest(silentTimeoutFallback, onNoSpeech, onResult)
            requestAudioPermission()
            return
        }

        beginRecognizerSession(silentTimeoutFallback, onNoSpeech, onResult)
    }

    internal fun onPermissionResult(granted: Boolean) {
        val pending = pendingSpeechRequest
        pendingSpeechRequest = null
        if (granted) {
            if (pending != null) {
                beginRecognizerSession(
                    pending.silentTimeoutFallback,
                    pending.onNoSpeech,
                    pending.onResult
                )
            }
        } else {
            voiceError = AppStrings.voiceMicPermNeeded.get(currentLanguage)
        }
    }

    private fun beginRecognizerSession(
        silentTimeoutFallback: Boolean,
        onNoSpeech: (() -> Unit)?,
        onResult: (String) -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            voiceError = AppStrings.voiceUnavailable.get(currentLanguage)
            isListening = false
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    voiceError = null
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListening = false
                }

                override fun onError(error: Int) {
                    isListening = false
                    partialTranscript = ""
                    val isSoftRetry = error == SpeechRecognizer.ERROR_NO_MATCH ||
                        error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                        error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY
                    if (silentTimeoutFallback && isSoftRetry) {
                        voiceError = null
                        onNoSpeech?.invoke()
                    } else {
                        voiceError = mapSpeechError(error)
                    }
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    partialTranscript = ""
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val top = matches?.firstOrNull()?.trim().orEmpty()
                    if (top.isNotBlank()) {
                        onResult(top)
                    } else if (silentTimeoutFallback) {
                        onNoSpeech?.invoke()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val live = matches?.firstOrNull()?.trim().orEmpty()
                    if (live.isNotBlank()) {
                        partialTranscript = live
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val localeTag = if (currentLanguage == AppLanguage.BN) "bn-BD" else "en-US"
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1100L)
            }
            isListening = true
            recognizer.startListening(intent)
        } catch (_: Exception) {
            isListening = false
            partialTranscript = ""
            voiceError = AppStrings.voiceUnavailable.get(currentLanguage)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (_: Exception) {
        }
        isListening = false
        partialTranscript = ""
    }

    fun clearError() {
        voiceError = null
    }

    private fun mapSpeechError(errorCode: Int): String? = when (errorCode) {
        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
            if (currentLanguage == AppLanguage.BN) {
                "কোনো কথা বোঝা যায়নি। আবার বলুন।"
            } else {
                "No clear speech detected. Please try again."
            }
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            AppStrings.voiceMicPermNeeded.get(currentLanguage)
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
            if (currentLanguage == AppLanguage.BN) {
                "মাইক্রোফোন ব্যস্ত আছে, কিছুক্ষণ পর চেষ্টা করুন।"
            } else {
                "Recognizer busy. Please tap again."
            }
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            if (currentLanguage == AppLanguage.BN) {
                "ভয়েস রিকগনিশনে নেটওয়ার্ক সমস্যা হয়েছে।"
            } else {
                "Speech recognition network error."
            }
        else ->
            if (currentLanguage == AppLanguage.BN) {
                "ভয়েস ইনপুট নেওয়া যায়নি।"
            } else {
                "Could not capture voice input."
            }
    }

    fun release() {
        activeOnSpeakComplete = null
        activeFinalUtteranceId = null
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }
        speechRecognizer = null
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        isListening = false
        isSpeaking = false
    }

    companion object {
        internal fun prepareNaturalSpeechSegments(rawText: String, language: AppLanguage): List<String> {
            val orConnector = if (language == AppLanguage.BN) " বা " else " or "
            val toConnector = if (language == AppLanguage.BN) " থেকে " else " to "
            val litersPerMin = if (language == AppLanguage.BN) " লিটার প্রতি মিনিট" else " liters per minute"
            val approxWord = if (language == AppLanguage.BN) "প্রায় " else "approximately "
            val degreeWord = if (language == AppLanguage.BN) " ডিগ্রি" else " degrees"
            val plusWord = if (language == AppLanguage.BN) " প্লাস" else " plus"

            val cleaned = rawText
                .replace(Regex("```[\\s\\S]*?```"), " ")
                .replace(Regex("\\[(PASS|REVIEW)]", RegexOption.IGNORE_CASE), "")
                .replace(Regex("[*_`#>|]"), " ")
                .replace("~", approxWord)
                .replace("°", degreeWord)
                .replace("6G+", "6G$plusWord", ignoreCase = true)
                .replace("L/min", litersPerMin, ignoreCase = true)
                .replace("L/m", litersPerMin, ignoreCase = true)
                .replace(Regex("(\\d+)\\s*[–-]\\s*(\\d+)"), "$1$toConnector$2")
                .replace(Regex("([A-Za-z0-9]+)/([A-Za-z0-9]+)"), "$1$orConnector$2")
                .replace("•", ", ")
                .replace(Regex("\\r?\\n+"), ". ")
                .replace(Regex("\\s{2,}"), " ")
                .trim()

            if (cleaned.isBlank()) return emptyList()

            val rawSentences = cleaned
                .split(Regex("(?<=[.!?।])\\s+"))
                .map { it.trim().trimStart('.', ',', ';') }
                .filter { it.isNotBlank() }

            val merged = mutableListOf<String>()
            val current = StringBuilder()
            for (sentence in rawSentences) {
                if (current.isEmpty()) {
                    current.append(sentence)
                } else if (current.length + sentence.length + 1 <= 240) {
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
}

@Composable
fun rememberVoiceAssistantController(language: AppLanguage): VoiceAssistantController {
    val context = LocalContext.current
    var controllerRef by remember { mutableStateOf<VoiceAssistantController?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        controllerRef?.onPermissionResult(granted)
    }

    val controller = remember(context) {
        VoiceAssistantController(
            context = context,
            requestAudioPermission = {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        ).also { controllerRef = it }
    }

    LaunchedEffect(language) {
        controller.updateLanguage(language)
    }

    DisposableEffect(controller) {
        onDispose {
            controller.release()
        }
    }

    return controller
}
