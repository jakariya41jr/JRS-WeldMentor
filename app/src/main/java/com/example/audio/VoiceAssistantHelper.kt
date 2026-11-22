package com.example.audio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
import com.example.data.local.SpeechSpeedPreset
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings

@Stable
class VoiceAssistantController(
    private val context: Context,
    private val requestAudioPermission: () -> Unit,
    val ttsService: WeldingTtsService = AndroidWeldingTtsService(context)
) {
    var isListening by mutableStateOf(false)
        private set

    var isSpeaking by mutableStateOf(ttsService.playbackState.value.isSpeaking)
        private set

    var ttsReady by mutableStateOf(ttsService.playbackState.value.isReady)
        private set

    var voiceError by mutableStateOf<String?>(null)
        private set

    var partialTranscript by mutableStateOf("")
        private set

    var hasAudioPermission by mutableStateOf(checkAudioPermission(context))
        private set

    private var currentLanguage: AppLanguage = AppLanguage.EN
    private var currentSpeedPreset: SpeechSpeedPreset = SpeechSpeedPreset.NATURAL
    private var pendingSpeechRequest: PendingSpeechRequest? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var listenGeneration: Long = 0L

    private data class PendingSpeechRequest(
        val silentTimeoutFallback: Boolean,
        val onNoSpeech: (() -> Unit)?,
        val onResult: (String) -> Unit
    )

    internal fun syncTtsPlaybackState(state: TtsPlaybackState) {
        ttsReady = state.isReady
        isSpeaking = state.isSpeaking
    }

    fun refreshPermissionState() {
        hasAudioPermission = checkAudioPermission(context)
    }

    fun updateSettings(language: AppLanguage, speedPreset: SpeechSpeedPreset = currentSpeedPreset) {
        currentLanguage = language
        currentSpeedPreset = speedPreset
        ttsService.updateSettings(language, speedPreset)
        syncTtsPlaybackState(ttsService.playbackState.value)
    }

    fun updateLanguage(language: AppLanguage) {
        updateSettings(language, currentSpeedPreset)
    }

    fun speak(rawText: String, onComplete: (() -> Unit)? = null) {
        voiceError = null
        ttsService.speakText(rawText, itemId = null, onComplete = onComplete)
        syncTtsPlaybackState(ttsService.playbackState.value)
    }

    fun stopSpeaking() {
        ttsService.stop()
        syncTtsPlaybackState(ttsService.playbackState.value)
    }

    /**
     * Barge-in / Tap-to-interrupt: halts AI TTS playback immediately and transitions cleanly to listening.
     */
    fun interruptAndListen(
        silentTimeoutFallback: Boolean = true,
        onNoSpeech: (() -> Unit)? = null,
        onResult: (String) -> Unit
    ) {
        stopSpeaking()
        startListening(
            silentTimeoutFallback = silentTimeoutFallback,
            onNoSpeech = onNoSpeech,
            onResult = onResult
        )
    }

    fun startListening(
        silentTimeoutFallback: Boolean = false,
        onNoSpeech: (() -> Unit)? = null,
        onResult: (String) -> Unit
    ) {
        voiceError = null
        partialTranscript = ""
        stopSpeaking()
        refreshPermissionState()

        if (!hasAudioPermission) {
            pendingSpeechRequest = PendingSpeechRequest(silentTimeoutFallback, onNoSpeech, onResult)
            requestAudioPermission()
            return
        }

        beginRecognizerSession(silentTimeoutFallback, onNoSpeech, onResult)
    }

    internal fun onPermissionResult(granted: Boolean) {
        hasAudioPermission = granted
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

        listenGeneration += 1L
        val activeGen = listenGeneration

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (activeGen != listenGeneration) return
                    isListening = true
                    voiceError = null
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    if (activeGen != listenGeneration) return
                    isListening = false
                }

                override fun onError(error: Int) {
                    if (activeGen != listenGeneration) return
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
                    if (activeGen != listenGeneration) return
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
                    if (activeGen != listenGeneration) return
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
        listenGeneration += 1L
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
        listenGeneration += 1L
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }
        speechRecognizer = null
        ttsService.release()
        syncTtsPlaybackState(ttsService.playbackState.value)
        isListening = false
    }

    companion object {
        fun checkAudioPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }

        internal fun prepareNaturalSpeechSegments(rawText: String, language: AppLanguage): List<String> {
            return QuizSpeechFormatter.prepareNaturalSpeechSegments(rawText, language)
        }
    }
}

@Composable
fun rememberVoiceAssistantController(
    language: AppLanguage,
    speedPreset: SpeechSpeedPreset = SpeechSpeedPreset.NATURAL
): VoiceAssistantController {
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

    LaunchedEffect(controller) {
        controller.ttsService.playbackState.collect { ttsState ->
            controller.syncTtsPlaybackState(ttsState)
        }
    }

    LaunchedEffect(language, speedPreset) {
        controller.updateSettings(language, speedPreset)
    }

    DisposableEffect(controller) {
        onDispose {
            controller.release()
        }
    }

    return controller
}
