package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.rememberVoiceAssistantController
import com.example.data.content.VivaKnowledgeBase
import com.example.data.model.QuizQuestion
import com.example.data.remote.ApiKeyStatus
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.BiText
import com.example.ui.components.CenterpiecePhase
import com.example.ui.components.WeldingArcCenterpiece
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCard
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.PassEmerald
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletSpark
import com.example.ui.theme.WarnRose
import com.example.ui.viewmodel.WeldUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val STARTER_PROMPTS = listOf(
    BiText("How to pass 6G TIG root without suck-back?", "সাক-ব্যাক ছাড়া 6G TIG রুট পাস করার উপায় কী?"),
    BiText("LB52 vs E7018: amps & key differences", "LB52 এবং E7018-এর মধ্যে পার্থক্য ও অ্যাম্পিয়ার"),
    BiText("How do I fix Undercut in 3G Vertical Up?", "3G ভার্টিক্যাল আপে আন্ডারকাট কীভাবে বন্ধ করবো?"),
    BiText("Explain TIG Cup, Gas Lens & Short Back Cap", "TIG সিরামিক কাপ, গ্যাস লেন্স ও শর্ট ব্যাক ক্যাপ বুঝিয়ে দিন")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiMentorScreen(
    uiState: WeldUiState,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onSubmitDelegateAnswer: (QuizQuestion, String) -> Unit,
    onNextDelegateQuestion: (Int) -> Unit,
    onShuffleDelegateQuestions: (Int) -> Unit,
    onResetDelegateFeedback: () -> Unit,
    onOpenVivaCards: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.preferences.language
    val hasKey = uiState.preferences.geminiApiKey.isNotBlank()
    val isOnline = uiState.isOnline
    val chatState = uiState.aiChat
    val delegateState = uiState.vivaDelegate
    val apiKeyStatus = uiState.apiKeyStatus

    var input by remember { mutableStateOf("") }
    var delegateInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val voice = rememberVoiceAssistantController(lang)
    val scope = rememberCoroutineScope()

    // Session Mode: false = AI Chat Mentor, true = Live Delegator Session inside AI Mentor
    var isDelegatorSession by remember { mutableStateOf(false) }

    var autoSpeakAi by remember { mutableStateOf(true) }
    var lastSpokenMessageId by remember { mutableStateOf<String?>(null) }

    // Dual Voice Mode for Chat
    var isLiveTalkMode by remember { mutableStateOf(false) }
    var liveSessionActive by remember { mutableStateOf(false) }

    // Dual Voice Mode for Delegator inside AI Mentor
    var delegateLiveActive by remember { mutableStateOf(false) }

    val allQuestions = VivaKnowledgeBase.quizQuestions
    val delegateQuestion = allQuestions[delegateState.activeQuestionIndex % allQuestions.size]

    fun launchChatLiveListening() {
        if (!liveSessionActive || isDelegatorSession || !isOnline || chatState.isLoading || voice.isSpeaking) return
        voice.startListening(
            silentTimeoutFallback = true,
            onNoSpeech = {
                if (liveSessionActive && !isDelegatorSession && isOnline && !chatState.isLoading && !voice.isSpeaking) {
                    scope.launch {
                        delay(320)
                        launchChatLiveListening()
                    }
                }
            },
            onResult = { spoken ->
                if (liveSessionActive && !isDelegatorSession && isOnline && !chatState.isLoading) {
                    onSendMessage(spoken)
                } else if (!liveSessionActive) {
                    input = if (input.isBlank()) spoken else "$input $spoken"
                }
            }
        )
    }

    fun launchMentorDelegateListening() {
        if (!delegateLiveActive || !isDelegatorSession || delegateState.isEvaluating || voice.isSpeaking) return
        voice.startListening(
            silentTimeoutFallback = true,
            onNoSpeech = {
                if (delegateLiveActive && isDelegatorSession && !delegateState.isEvaluating && !voice.isSpeaking) {
                    scope.launch {
                        delay(320)
                        launchMentorDelegateListening()
                    }
                }
            },
            onResult = { spoken ->
                delegateInput = spoken
                onResetDelegateFeedback()
                if (delegateLiveActive && isDelegatorSession) {
                    onSubmitDelegateAnswer(delegateQuestion, spoken)
                }
            }
        )
    }

    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty() && !isDelegatorSession) {
            listState.animateScrollToItem(chatState.messages.size - 1)
            val latest = chatState.messages.last()
            if (!latest.isUser && latest.id != lastSpokenMessageId) {
                lastSpokenMessageId = latest.id
                if (liveSessionActive) {
                    voice.speak(latest.text) {
                        if (liveSessionActive && !isDelegatorSession && isOnline && !chatState.isLoading) {
                            scope.launch {
                                delay(320)
                                launchChatLiveListening()
                            }
                        }
                    }
                } else if (autoSpeakAi) {
                    voice.speak(latest.text)
                }
            }
        }
    }

    LaunchedEffect(delegateState.currentIndex, delegateState.activeQuestionIndex, isDelegatorSession, delegateLiveActive) {
        if (isDelegatorSession && delegateLiveActive && delegateState.lastFeedback == null) {
            if (voice.ttsReady) {
                voice.speak(delegateQuestion.question.get(lang)) {
                    if (delegateLiveActive && isDelegatorSession && !delegateState.isEvaluating) {
                        scope.launch {
                            delay(320)
                            launchMentorDelegateListening()
                        }
                    }
                }
            } else {
                launchMentorDelegateListening()
            }
        }
    }

    LaunchedEffect(delegateState.lastFeedback) {
        val fb = delegateState.lastFeedback
        if (isDelegatorSession && !fb.isNullOrBlank()) {
            if (delegateLiveActive) {
                voice.speak(fb) {
                    if (delegateLiveActive && isDelegatorSession) {
                        scope.launch {
                            delay(380)
                            if (delegateState.lastPassed == true) {
                                delegateInput = ""
                                onNextDelegateQuestion(allQuestions.size)
                            } else {
                                launchMentorDelegateListening()
                            }
                        }
                    }
                }
            } else if (autoSpeakAi) {
                voice.speak(fb)
            }
        }
    }

    LaunchedEffect(chatState.errorMessage, isOnline, hasKey) {
        if (liveSessionActive && (chatState.errorMessage != null || !isOnline || !hasKey)) {
            liveSessionActive = false
            voice.stopListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            liveSessionActive = false
            delegateLiveActive = false
            voice.stopListening()
            voice.stopSpeaking()
        }
    }

    val (pillText, pillColor) = when (apiKeyStatus) {
        ApiKeyStatus.Missing -> AppStrings.statusMissing.get(lang) to MoltenAmber
        ApiKeyStatus.Validating -> AppStrings.statusValidating.get(lang) to ArcCyan
        is ApiKeyStatus.Active -> "${AppStrings.statusActive.get(lang)} • ${apiKeyStatus.modelId}" to PassEmerald
        is ApiKeyStatus.QuotaLimited -> AppStrings.statusQuotaLimited.get(lang) to MoltenAmber
        is ApiKeyStatus.Invalid -> AppStrings.statusInvalid.get(lang) to WarnRose
        is ApiKeyStatus.Offline -> "${AppStrings.statusOfflineSaved.get(lang)} • ${apiKeyStatus.modelId}" to MoltenAmber
    }

    val centerpiecePhase = when {
        chatState.isLoading || (isDelegatorSession && delegateState.isEvaluating) -> CenterpiecePhase.THINKING
        voice.isSpeaking -> CenterpiecePhase.SPEAKING
        voice.isListening -> CenterpiecePhase.LISTENING
        else -> CenterpiecePhase.IDLE
    }

    val centerpieceSubtitle = when {
        voice.partialTranscript.isNotBlank() ->
            "${AppStrings.liveHearingPrefix.get(lang)}: \"${voice.partialTranscript}\""
        centerpiecePhase == CenterpiecePhase.THINKING ->
            if (isDelegatorSession) AppStrings.delegateEvaluating.get(lang) else AppStrings.liveThinkingPrompt.get(lang)
        centerpiecePhase == CenterpiecePhase.SPEAKING ->
            AppStrings.liveSpeakingPrompt.get(lang)
        centerpiecePhase == CenterpiecePhase.LISTENING ->
            AppStrings.liveListeningPrompt.get(lang)
        isDelegatorSession ->
            AppStrings.delegateModeSubtitle.get(lang)
        else ->
            AppStrings.aiStrictScopeBadge.get(lang)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Animated Native Welding Arc & Voice Wave Visual Centerpiece
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeldingArcCenterpiece(
                phase = centerpiecePhase,
                title = if (isDelegatorSession) AppStrings.aiModeDelegatorLabel.get(lang) else AppStrings.aiMentorTitle.get(lang),
                subtitle = centerpieceSubtitle,
                badgeText = if (isDelegatorSession) {
                    if (isOnline && hasKey) AppStrings.delegateAiBadge.get(lang) else AppStrings.delegateOfflineBadge.get(lang)
                } else {
                    pillText
                }
            )

            // Quick Utility Strip + Session Switcher (AI Chat vs Delegator Mode)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                    SegmentedButton(
                        selected = !isDelegatorSession,
                        onClick = {
                            if (isDelegatorSession) {
                                delegateLiveActive = false
                                voice.stopListening()
                                voice.stopSpeaking()
                                isDelegatorSession = false
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("ai_session_chat_btn"),
                        icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp)) }
                    ) {
                        Text(AppStrings.aiModeChatLabel.get(lang), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    SegmentedButton(
                        selected = isDelegatorSession,
                        onClick = {
                            if (!isDelegatorSession) {
                                liveSessionActive = false
                                voice.stopListening()
                                voice.stopSpeaking()
                                isDelegatorSession = true
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("ai_session_delegator_btn"),
                        icon = { Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(15.dp)) }
                    ) {
                        Text(AppStrings.aiModeDelegatorLabel.get(lang), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                IconButton(
                    onClick = {
                        autoSpeakAi = !autoSpeakAi
                        if (!autoSpeakAi) voice.stopSpeaking()
                    },
                    modifier = Modifier.testTag("ai_auto_speak_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (autoSpeakAi) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                        contentDescription = AppStrings.voiceAutoSpeak.get(lang),
                        tint = if (autoSpeakAi) PassEmerald else TextSecondary
                    )
                }
                if (!isDelegatorSession && chatState.messages.isNotEmpty()) {
                    IconButton(onClick = {
                        liveSessionActive = false
                        voice.stopListening()
                        voice.stopSpeaking()
                        onClearChat()
                    }, modifier = Modifier.testTag("clear_ai_chat_btn")) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = AppStrings.clearChat.get(lang), tint = TextSecondary)
                    }
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("ai_open_settings_btn")) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = ArcCyan)
                }
            }
        }

        AnimatedContent(
            targetState = isDelegatorSession,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "mentorSessionContent",
            modifier = Modifier.weight(1f)
        ) { delegatorActive ->
            if (delegatorActive) {
                // Embedded Live Delegator Session inside AI Mentor (Works Online AI & Offline)
                val alreadyCompleted = delegateState.completedQuestionIds.contains(delegateQuestion.id)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (alreadyCompleted) PassEmerald.copy(alpha = 0.5f) else ArcCyan.copy(alpha = 0.4f),
                        accentGlow = if (delegateLiveActive) PassEmerald else VioletSpark,
                        contentPadding = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                ArcStatusPill(text = delegateQuestion.topic.get(lang), color = ArcCyan)
                                ArcStatusPill(
                                    text = "${delegateState.completedQuestionIds.size}/${allQuestions.size} ${AppStrings.knewIt.get(lang)}",
                                    color = if (alreadyCompleted) PassEmerald else VioletSpark,
                                    icon = if (alreadyCompleted) Icons.Filled.CheckCircle else null
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        voice.stopSpeaking()
                                        delegateInput = ""
                                        onShuffleDelegateQuestions(allQuestions.size)
                                    },
                                    modifier = Modifier.testTag("ai_delegate_shuffle_btn")
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppStrings.shuffleQuestionsBtn.get(lang), color = ArcCyan)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = delegateQuestion.question.get(lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (voice.isSpeaking) voice.stopSpeaking()
                                    else voice.speak(delegateQuestion.question.get(lang))
                                },
                                modifier = Modifier.testTag("ai_delegate_speak_q_btn"),
                                border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = if (voice.isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                                    contentDescription = AppStrings.voiceTapListen.get(lang),
                                    tint = ArcCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (voice.isSpeaking) AppStrings.voiceStopSpeaking.get(lang) else AppStrings.voiceTapListen.get(lang),
                                    color = ArcCyan
                                )
                            }

                            Button(
                                onClick = {
                                    if (delegateLiveActive) {
                                        delegateLiveActive = false
                                        voice.stopListening()
                                        voice.stopSpeaking()
                                    } else {
                                        delegateLiveActive = true
                                        autoSpeakAi = true
                                        voice.clearError()
                                        if (delegateState.lastFeedback != null || !voice.ttsReady) {
                                            launchMentorDelegateListening()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_delegate_live_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (delegateLiveActive) WarnRose else PassEmerald,
                                    contentColor = if (delegateLiveActive) Color.White else Color(0xFF022015)
                                )
                            ) {
                                Icon(
                                    imageVector = if (delegateLiveActive) Icons.Filled.Stop else Icons.Filled.GraphicEq,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (delegateLiveActive) AppStrings.endLiveTalk.get(lang) else AppStrings.vivaHandsFreeToggle.get(lang),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        voice.voiceError?.let { vErr ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(vErr, style = MaterialTheme.typography.bodySmall, color = WarnRose)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mode A: Manual Voice-to-Text + Editable Field + Evaluate Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = {
                                    delegateLiveActive = false
                                    if (voice.isListening) {
                                        voice.stopListening()
                                    } else {
                                        voice.startListening(silentTimeoutFallback = false) { spoken ->
                                            delegateInput = if (delegateInput.isBlank()) spoken else "$delegateInput $spoken"
                                            onResetDelegateFeedback()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(50.dp)
                                    .testTag("ai_delegate_mic_btn"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (voice.isListening && !delegateLiveActive) WarnRose else SlateSurfaceElevated,
                                border = BorderStroke(1.dp, if (voice.isListening) WarnRose else ArcCyan.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    imageVector = if (voice.isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                                    contentDescription = AppStrings.voiceSpeakAnswer.get(lang),
                                    tint = if (voice.isListening) Color.White else ArcCyan,
                                    modifier = Modifier.padding(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = delegateInput,
                                onValueChange = { delegateInput = it },
                                placeholder = {
                                    Text(
                                        text = if (voice.isListening && voice.partialTranscript.isNotBlank()) voice.partialTranscript
                                        else AppStrings.delegateAnswerHint.get(lang),
                                        color = if (voice.isListening) ArcCyan else TextMuted
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_delegate_answer_input"),
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (delegateInput.isNotBlank() && !delegateState.isEvaluating) {
                                        onSubmitDelegateAnswer(delegateQuestion, delegateInput)
                                    }
                                },
                                enabled = delegateInput.isNotBlank() && !delegateState.isEvaluating,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("ai_delegate_submit_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = Color(0xFF04131E))
                            ) {
                                if (delegateState.isEvaluating) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF04131E))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(AppStrings.delegateEvaluating.get(lang))
                                } else {
                                    Text(AppStrings.delegateSubmitBtn.get(lang))
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    voice.stopSpeaking()
                                    delegateInput = ""
                                    onNextDelegateQuestion(allQuestions.size)
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("ai_delegate_next_btn"),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Text(AppStrings.nextQuestion.get(lang), color = TextPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextPrimary)
                            }
                        }

                        AnimatedVisibility(visible = delegateState.lastFeedback != null) {
                            val passed = delegateState.lastPassed == true
                            val badgeCol = if (passed) PassEmerald else MoltenAmber
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = badgeCol.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, badgeCol.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    ArcStatusPill(
                                        text = if (passed) AppStrings.delegatePassedBadge.get(lang) else AppStrings.delegateReviewBadge.get(lang),
                                        color = badgeCol
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = delegateState.lastFeedback.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenVivaCards,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ai_delegate_open_viva_btn"),
                        border = BorderStroke(1.dp, VioletSpark.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = VioletSpark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppStrings.launchFullVivaDeckBtn.get(lang), color = VioletSpark)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else if (!hasKey) {
                // No API Key View (Delegator Mode above still works offline!)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentGlow = MoltenAmber,
                        contentPadding = 22.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Key, contentDescription = null, tint = MoltenAmber, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = AppStrings.aiNoKeyTitle.get(lang),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppStrings.aiNoKeyDesc.get(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onOpenSettings,
                                    modifier = Modifier.testTag("configure_api_key_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = Color(0xFF04131E))
                                ) {
                                    Text(AppStrings.openSettingsBtn.get(lang))
                                }
                                OutlinedButton(
                                    onClick = { isDelegatorSession = true },
                                    border = BorderStroke(1.dp, VioletSpark.copy(alpha = 0.5f))
                                ) {
                                    Text(AppStrings.aiModeDelegatorLabel.get(lang), color = VioletSpark)
                                }
                            }
                        }
                    }
                }
            } else {
                // Full AI Chat Mentor + Dual Voice Mode
                Column(modifier = Modifier.fillMaxSize()) {
                    if (!isOnline) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarnRose.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, WarnRose.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CloudOff, contentDescription = null, tint = WarnRose, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(AppStrings.aiOfflineBanner.get(lang), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                    }

                    if (apiKeyStatus is ApiKeyStatus.Invalid || apiKeyStatus is ApiKeyStatus.QuotaLimited) {
                        val alertColor = if (apiKeyStatus is ApiKeyStatus.Invalid) WarnRose else MoltenAmber
                        val alertMsg = when (apiKeyStatus) {
                            is ApiKeyStatus.Invalid -> apiKeyStatus.detail
                            is ApiKeyStatus.QuotaLimited -> apiKeyStatus.detail
                            else -> ""
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = alertColor.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, alertColor.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = alertColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = alertMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = onOpenSettings,
                                    border = BorderStroke(1.dp, alertColor.copy(alpha = 0.6f))
                                ) {
                                    Text(AppStrings.settingsTitle.get(lang), color = alertColor, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (chatState.messages.isEmpty()) {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                                    Text(
                                        text = if (lang == AppLanguage.BN) {
                                            "ওয়েল্ডিং প্রসেস, 1G–6G+, রড কোড, TIG টর্চ বা ভাইভা নিয়ে প্রশ্ন করুন:"
                                        } else {
                                            "Ask anything about welding processes, 1G–6G+, rods, TIG setup, defects, or viva prep:"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        items(chatState.messages, key = { it.id }) { msg ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                        bottomEnd = if (msg.isUser) 4.dp else 16.dp
                                    ),
                                    color = if (msg.isUser) ArcCyan.copy(alpha = 0.22f) else SlateSurfaceElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (msg.isUser) ArcCyan.copy(alpha = 0.45f) else GlassBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth(0.88f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                                        if (!msg.isUser) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                Surface(
                                                    onClick = {
                                                        if (voice.isSpeaking) voice.stopSpeaking() else voice.speak(msg.text)
                                                    },
                                                    shape = RoundedCornerShape(50),
                                                    color = ArcCyan.copy(alpha = 0.14f),
                                                    modifier = Modifier.testTag("ai_speak_msg_${msg.id}")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = if (voice.isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                                                            contentDescription = AppStrings.voiceTapListen.get(lang),
                                                            tint = ArcCyan,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (voice.isSpeaking) AppStrings.voiceStopSpeaking.get(lang) else AppStrings.voiceTapListen.get(lang),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = ArcCyan
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (chatState.isLoading) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = ArcCyan)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = AppStrings.liveThinkingPrompt.get(lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        chatState.errorMessage?.let { err ->
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = WarnRose.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, WarnRose.copy(alpha = 0.45f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(err, style = MaterialTheme.typography.bodySmall, color = WarnRose, modifier = Modifier.padding(10.dp))
                                }
                            }
                        }
                    }

                    // Quick Starter Prompts
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(STARTER_PROMPTS) { prompt ->
                            SuggestionChip(
                                onClick = { if (isOnline && !chatState.isLoading) onSendMessage(prompt.get(lang)) },
                                label = { Text(prompt.get(lang), style = MaterialTheme.typography.bodySmall, color = TextPrimary) }
                            )
                        }
                    }

                    voice.voiceError?.let { vErr ->
                        Text(
                            text = vErr,
                            style = MaterialTheme.typography.bodySmall,
                            color = WarnRose,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    // Dual Voice Mode Switcher: Mode A (Voice-to-Text Manual Send) vs Mode B (Hands-Free Live Talk)
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        SegmentedButton(
                            selected = !isLiveTalkMode,
                            onClick = {
                                isLiveTalkMode = false
                                if (liveSessionActive) {
                                    liveSessionActive = false
                                    voice.stopListening()
                                    voice.stopSpeaking()
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            modifier = Modifier.testTag("ai_mode_voice_to_text"),
                            icon = { Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        ) {
                            Text(AppStrings.voiceModeManual.get(lang), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        SegmentedButton(
                            selected = isLiveTalkMode,
                            onClick = {
                                isLiveTalkMode = true
                                voice.clearError()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            modifier = Modifier.testTag("ai_mode_live_talk"),
                            icon = { Icon(Icons.Filled.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        ) {
                            Text(AppStrings.voiceModeLiveTalk.get(lang), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    if (!isLiveTalkMode) {
                        Text(
                            text = AppStrings.voiceModeManualDesc.get(lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = {
                                    if (voice.isListening) {
                                        voice.stopListening()
                                    } else {
                                        voice.startListening(silentTimeoutFallback = false) { spoken ->
                                            input = if (input.isBlank()) spoken else "$input $spoken"
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(50.dp)
                                    .testTag("ai_voice_mic_btn"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (voice.isListening) WarnRose else SlateSurfaceElevated,
                                border = BorderStroke(1.dp, if (voice.isListening) WarnRose else ArcCyan.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    imageVector = if (voice.isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                                    contentDescription = if (voice.isListening) AppStrings.voiceListening.get(lang) else AppStrings.voiceSpeakBtn.get(lang),
                                    tint = if (voice.isListening) Color.White else ArcCyan,
                                    modifier = Modifier.padding(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = input,
                                onValueChange = { input = it },
                                placeholder = {
                                    Text(
                                        text = when {
                                            voice.isListening && voice.partialTranscript.isNotBlank() -> voice.partialTranscript
                                            voice.isListening -> AppStrings.voiceListening.get(lang)
                                            else -> AppStrings.aiInputHint.get(lang)
                                        },
                                        color = if (voice.isListening) ArcCyan else TextMuted
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_chat_input"),
                                maxLines = 3,
                                shape = RoundedCornerShape(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                onClick = {
                                    if (input.isNotBlank() && isOnline && !chatState.isLoading) {
                                        onSendMessage(input)
                                        input = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(50.dp)
                                    .testTag("ai_chat_send_btn"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (input.isNotBlank() && isOnline) ArcCyan else SlateSurfaceElevated
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (input.isNotBlank() && isOnline) Color(0xFF04131E) else TextMuted,
                                    modifier = Modifier.padding(13.dp)
                                )
                            }
                        }
                    } else {
                        val phaseColor = when {
                            !liveSessionActive -> ArcCyan
                            chatState.isLoading -> MoltenAmber
                            voice.isSpeaking -> PassEmerald
                            voice.isListening -> WarnRose
                            else -> VioletSpark
                        }
                        val phaseMessage = when {
                            !liveSessionActive -> AppStrings.liveTalkReadyDesc.get(lang)
                            voice.partialTranscript.isNotBlank() ->
                                "${AppStrings.liveHearingPrefix.get(lang)}: \"${voice.partialTranscript}\""
                            chatState.isLoading -> AppStrings.liveThinkingPrompt.get(lang)
                            voice.isSpeaking -> AppStrings.liveSpeakingPrompt.get(lang)
                            voice.isListening -> AppStrings.liveListeningPrompt.get(lang)
                            else -> AppStrings.liveListeningPrompt.get(lang)
                        }

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("ai_live_talk_panel"),
                            accentGlow = phaseColor,
                            borderColor = phaseColor.copy(alpha = 0.55f),
                            contentPadding = 14.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(phaseColor)
                                )
                                Text(
                                    text = phaseMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            if (!liveSessionActive) {
                                Button(
                                    onClick = {
                                        if (isOnline && !chatState.isLoading) {
                                            liveSessionActive = true
                                            voice.clearError()
                                            launchChatLiveListening()
                                        }
                                    },
                                    enabled = isOnline && !chatState.isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("ai_start_live_talk_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PassEmerald,
                                        contentColor = Color(0xFF022015)
                                    )
                                ) {
                                    Icon(Icons.Filled.GraphicEq, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(AppStrings.startLiveTalk.get(lang))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        liveSessionActive = false
                                        voice.stopListening()
                                        voice.stopSpeaking()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("ai_stop_live_talk_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WarnRose,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(AppStrings.endLiveTalk.get(lang))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
