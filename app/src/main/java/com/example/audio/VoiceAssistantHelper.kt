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

    private var currentLanguage: AppLanguage = AppLanguage.EN
    private var pendingSpeechCallback: ((String) -> Unit)? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                mainHandler.post {
                    if (status == TextToSpeech.SUCCESS) {
                        ttsReady = true
                        applyTtsLanguage(currentLanguage)
                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                mainHandler.post { isSpeaking = true }
                            }

                            override fun onDone(utteranceId: String?) {
                                mainHandler.post { isSpeaking = false }
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                mainHandler.post { isSpeaking = false }
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                mainHandler.post { isSpeaking = false }
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
            applyTtsLanguage(language)
        }
    }

    private fun applyTtsLanguage(language: AppLanguage) {
        val engine = tts ?: return
        val preferred = if (language == AppLanguage.BN) Locale("bn", "BD") else Locale.US
        val result = engine.setLanguage(preferred)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            if (language == AppLanguage.BN) {
                val indiaBn = Locale("bn", "IN")
                val fallbackRes = engine.setLanguage(indiaBn)
                if (fallbackRes == TextToSpeech.LANG_MISSING_DATA || fallbackRes == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }
            } else {
                engine.setLanguage(Locale.US)
            }
        }
        engine.setSpeechRate(0.98f)
    }

    fun speak(rawText: String) {
        val cleaned = cleanMarkdownForSpeech(rawText)
        if (cleaned.isBlank() || !ttsReady) return
        voiceError = null
        applyTtsLanguage(currentLanguage)
        val utteranceId = "weld_${System.currentTimeMillis()}"
        tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        isSpeaking = false
    }

    fun startListening(onResult: (String) -> Unit) {
        voiceError = null
        stopSpeaking()

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            pendingSpeechCallback = onResult
            requestAudioPermission()
            return
        }

        beginRecognizerSession(onResult)
    }

    internal fun onPermissionResult(granted: Boolean) {
        val pending = pendingSpeechCallback
        pendingSpeechCallback = null
        if (granted) {
            if (pending != null) {
                beginRecognizerSession(pending)
            }
        } else {
            voiceError = AppStrings.voiceMicPermNeeded.get(currentLanguage)
        }
    }

    private fun beginRecognizerSession(onResult: (String) -> Unit) {
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
                    voiceError = mapSpeechError(error)
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val top = matches?.firstOrNull()?.trim().orEmpty()
                    if (top.isNotBlank()) {
                        onResult(top)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val localeTag = if (currentLanguage == AppLanguage.BN) "bn-BD" else "en-US"
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
            isListening = true
            recognizer.startListening(intent)
        } catch (_: Exception) {
            isListening = false
            voiceError = AppStrings.voiceUnavailable.get(currentLanguage)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }
        isListening = false
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

    private fun cleanMarkdownForSpeech(text: String): String {
        return text
            .replace(Regex("```[\\s\\S]*?```"), " ")
            .replace(Regex("[*_`#>|~]"), " ")
            .replace("•", ", ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun release() {
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
