package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.rememberVoiceAssistantController
import com.example.data.content.VivaKnowledgeBase
import com.example.data.model.QuizQuestion
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.CenterpiecePhase
import com.example.ui.components.WeldEnergyOrbCenterpiece
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.GlassCard
import com.example.ui.theme.WeldTheme
import com.example.ui.viewmodel.WeldUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VivaQuizScreen(
    uiState: WeldUiState,
    selectedSubMode: Int,
    onSelectSubMode: (Int) -> Unit,
    onRecordAttempt: (String, String, String, Boolean) -> Unit,
    onResetStats: () -> Unit,
    onToggleMasteredCard: (String, Boolean) -> Unit,
    onSubmitDelegateAnswer: (QuizQuestion, String) -> Unit,
    onNextDelegateQuestion: (Int) -> Unit,
    onShuffleDelegateQuestions: (Int) -> Unit,
    onResetDelegateFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = WeldTheme.colors
    val prefs = uiState.preferences
    val lang = prefs.language
    val metrics = uiState.metrics
    val voice = rememberVoiceAssistantController(lang, prefs.speechSpeedPreset)
    val subMode = selectedSubMode
    var selectedTopicEn by remember { mutableStateOf("ALL") }
    var quizShuffleSeed by remember { mutableIntStateOf(Random.nextInt()) }
    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }

    var delegateAnswerInput by remember { mutableStateOf("") }
    var autoSpeakDelegate by remember { mutableStateOf(prefs.autoSpeakAi) }
    var vivaHandsFreeLive by remember { mutableStateOf(false) }
    val bargeInEnabled = prefs.bargeInEnabled
    val scope = rememberCoroutineScope()

    DisposableEffect(subMode) {
        onDispose {
            if (subMode != 1) {
                vivaHandsFreeLive = false
                voice.stopListening()
                voice.stopSpeaking()
            }
        }
    }

    val allTopics = remember {
        VivaKnowledgeBase.quizQuestions.map { it.topic }.distinctBy { it.en }
    }
    val filteredQuestions = remember(selectedTopicEn, metrics.weakTopics) {
        when (selectedTopicEn) {
            "ALL" -> VivaKnowledgeBase.quizQuestions
            "WEAK" -> {
                val weakSet = metrics.weakTopics.map { it.topicEn }.toSet()
                VivaKnowledgeBase.quizQuestions.filter { weakSet.contains(it.topic.en) }
                    .ifEmpty { VivaKnowledgeBase.quizQuestions }
            }
            else -> VivaKnowledgeBase.quizQuestions.filter { it.topic.en == selectedTopicEn }
        }
    }
    val shuffledDeck = remember(filteredQuestions, quizShuffleSeed) {
        VivaKnowledgeBase.buildShuffledQuizDeck(filteredQuestions, Random(quizShuffleSeed))
    }
    val safeItem = shuffledDeck[currentQuestionIdx % shuffledDeck.size]
    val safeQuestion = safeItem.source

    var cardIdx by remember { mutableIntStateOf(0) }
    var isCardFlipped by remember { mutableStateOf(false) }
    val cards = VivaKnowledgeBase.flashCards
    val currentCard = cards[cardIdx % cards.size]
    val masteredIds = uiState.preferences.masteredFlashcardIds

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = subMode == 0,
                    onClick = { onSelectSubMode(0) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    modifier = Modifier.testTag("tab_viva_quiz"),
                    icon = { Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(15.dp)) }
                ) {
                    Text(AppStrings.vivaQuizTab.get(lang), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                SegmentedButton(
                    selected = subMode == 1,
                    onClick = { onSelectSubMode(1) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    modifier = Modifier.testTag("tab_viva_delegate"),
                    icon = { Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(15.dp)) }
                ) {
                    Text(AppStrings.vivaDelegateTab.get(lang), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                SegmentedButton(
                    selected = subMode == 2,
                    onClick = { onSelectSubMode(2) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    modifier = Modifier.testTag("tab_rod_memorizer"),
                    icon = { Icon(Icons.Filled.Style, contentDescription = null, modifier = Modifier.size(15.dp)) }
                ) {
                    Text(AppStrings.rodMemorizerTab.get(lang), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        if (subMode == 0) {
            // Accuracy & Weak Topics Summary
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = palette.violetSpark, contentPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${AppStrings.accuracyLabel.get(lang)}: ${metrics.quizAccuracyPct}%",
                                style = MaterialTheme.typography.headlineSmall,
                                color = palette.textPrimary
                            )
                            Text(
                                text = "${AppStrings.answeredLabel.get(lang)}: ${metrics.totalQuizAnswered} • ${filteredQuestions.size} Qs",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    quizShuffleSeed = Random.nextInt()
                                    currentQuestionIdx = 0
                                    selectedOption = null
                                },
                                modifier = Modifier.testTag("quiz_shuffle_btn")
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = AppStrings.shuffleQuestionsBtn.get(lang), tint = palette.arcCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.shuffleQuestionsBtn.get(lang), color = palette.arcCyan)
                            }
                            if (metrics.totalQuizAnswered > 0) {
                                TextButton(onClick = onResetStats, modifier = Modifier.testTag("reset_quiz_btn")) {
                                    Text(AppStrings.resetStats.get(lang), color = palette.textSecondary)
                                }
                            }
                        }
                    }

                    if (metrics.weakTopics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = AppStrings.weakTopicsLabel.get(lang),
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.moltenAmber
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            metrics.weakTopics.forEach { stat ->
                                val label = if (lang == AppLanguage.BN) stat.topicBn else stat.topicEn
                                ArcStatusPill(
                                    text = "$label (${stat.accuracyPct}%)",
                                    color = palette.moltenAmber,
                                    icon = Icons.Filled.WarningAmber
                                )
                            }
                        }
                    }
                }
            }

            // Topic Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedTopicEn == "ALL",
                            onClick = {
                                selectedTopicEn = "ALL"
                                quizShuffleSeed = Random.nextInt()
                                currentQuestionIdx = 0
                                selectedOption = null
                            },
                            label = { Text(AppStrings.allFilter.get(lang)) }
                        )
                    }
                    if (metrics.weakTopics.isNotEmpty()) {
                        item {
                            FilterChip(
                                selected = selectedTopicEn == "WEAK",
                                onClick = {
                                    selectedTopicEn = "WEAK"
                                    quizShuffleSeed = Random.nextInt()
                                    currentQuestionIdx = 0
                                    selectedOption = null
                                },
                                label = { Text(AppStrings.weakTopicsLabel.get(lang)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = palette.moltenAmber.copy(alpha = 0.25f),
                                    selectedLabelColor = palette.moltenAmber
                                )
                            )
                        }
                    }
                    items(allTopics) { topic ->
                        FilterChip(
                            selected = selectedTopicEn == topic.en,
                            onClick = {
                                selectedTopicEn = topic.en
                                quizShuffleSeed = Random.nextInt()
                                currentQuestionIdx = 0
                                selectedOption = null
                            },
                            label = { Text(topic.get(lang)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.arcCyan.copy(alpha = 0.22f),
                                selectedLabelColor = palette.arcCyan
                            )
                        )
                    }
                }
            }

            // Question Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = palette.arcCyan.copy(alpha = 0.35f),
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            ArcStatusPill(text = safeQuestion.topic.get(lang), color = palette.arcCyan)
                            ArcStatusPill(text = AppStrings.shuffledSetBadge.get(lang), color = palette.violetSpark)
                        }
                        Text(
                            text = "${(currentQuestionIdx % shuffledDeck.size) + 1} / ${shuffledDeck.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = safeQuestion.question.get(lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    safeItem.shuffledOptions.forEachIndexed { idx, opt ->
                        val answered = selectedOption != null
                        val isChosen = selectedOption == idx
                        val isRightOption = idx == safeItem.correctShuffledIndex

                        val borderCol = when {
                            !answered -> palette.glassBorder
                            isRightOption -> palette.passEmerald
                            isChosen -> palette.warnRose
                            else -> palette.glassBorder
                        }
                        val bgCol = when {
                            !answered -> if (palette.isLight) Color(0xFFF1F5F9) else Color.White.copy(alpha = 0.04f)
                            isRightOption -> palette.passEmerald.copy(alpha = 0.16f)
                            isChosen -> palette.warnRose.copy(alpha = 0.16f)
                            else -> if (palette.isLight) Color(0xFFF8FAFC) else Color.White.copy(alpha = 0.02f)
                        }

                        Surface(
                            onClick = {
                                if (selectedOption == null) {
                                    selectedOption = idx
                                    onRecordAttempt(
                                        safeQuestion.id,
                                        safeQuestion.topic.en,
                                        safeQuestion.topic.bn,
                                        idx == safeItem.correctShuffledIndex
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("quiz_option_$idx"),
                            shape = RoundedCornerShape(14.dp),
                            color = bgCol,
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${'A' + idx}. ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.arcCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = opt.get(lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (answered && isRightOption) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = palette.passEmerald, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = selectedOption != null) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = palette.arcCyan.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, palette.arcCyan.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = AppStrings.explanationHeader.get(lang),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = palette.arcCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = safeQuestion.explanation.get(lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val nextPos = currentQuestionIdx + 1
                                    if (nextPos >= shuffledDeck.size) {
                                        quizShuffleSeed = Random.nextInt()
                                        currentQuestionIdx = 0
                                    } else {
                                        currentQuestionIdx = nextPos
                                    }
                                    selectedOption = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quiz_next_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(AppStrings.nextQuestion.get(lang))
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        } else if (subMode == 1) {
            // Interactive Delegate Viva Mode (Randomized Deck + Energy Orb + Barge-In Interrupt + Hands-Free Live)
            val delegateState = uiState.vivaDelegate
            val allQuestions = VivaKnowledgeBase.quizQuestions
            val delegateQuestion = allQuestions[delegateState.activeQuestionIndex % allQuestions.size]
            val aiExaminerReady = uiState.isOnline && uiState.preferences.geminiApiKey.isNotBlank()
            val alreadyCompleted = delegateState.completedQuestionIds.contains(delegateQuestion.id)

            fun launchDelegateHandsFreeListen() {
                if (!vivaHandsFreeLive || subMode != 1 || delegateState.isEvaluating || voice.isSpeaking) return
                voice.startListening(
                    silentTimeoutFallback = true,
                    onNoSpeech = {
                        if (vivaHandsFreeLive && subMode == 1 && !delegateState.isEvaluating && !voice.isSpeaking) {
                            scope.launch {
                                delay(260)
                                launchDelegateHandsFreeListen()
                            }
                        }
                    },
                    onResult = { spoken ->
                        delegateAnswerInput = spoken
                        onResetDelegateFeedback()
                        if (vivaHandsFreeLive) {
                            onSubmitDelegateAnswer(delegateQuestion, spoken)
                        }
                    }
                )
            }

            fun handleDelegateOrbTap() {
                when {
                    voice.isSpeaking -> {
                        if (bargeInEnabled) {
                            vivaHandsFreeLive = true
                            autoSpeakDelegate = true
                            voice.interruptAndListen(
                                silentTimeoutFallback = true,
                                onNoSpeech = {
                                    if (vivaHandsFreeLive && subMode == 1 && !delegateState.isEvaluating && !voice.isSpeaking) {
                                        scope.launch {
                                            delay(260)
                                            launchDelegateHandsFreeListen()
                                        }
                                    }
                                },
                                onResult = { spoken ->
                                    delegateAnswerInput = spoken
                                    onResetDelegateFeedback()
                                    if (vivaHandsFreeLive) {
                                        onSubmitDelegateAnswer(delegateQuestion, spoken)
                                    }
                                }
                            )
                        } else {
                            voice.stopSpeaking()
                        }
                    }
                    voice.isListening || vivaHandsFreeLive -> {
                        vivaHandsFreeLive = false
                        voice.stopListening()
                        voice.stopSpeaking()
                    }
                    !delegateState.isEvaluating -> {
                        vivaHandsFreeLive = true
                        autoSpeakDelegate = true
                        voice.clearError()
                        if (delegateState.lastFeedback != null || !voice.ttsReady) {
                            launchDelegateHandsFreeListen()
                        } else {
                            voice.speak(delegateQuestion.question.get(lang)) {
                                if (vivaHandsFreeLive && subMode == 1 && !delegateState.isEvaluating) {
                                    scope.launch {
                                        delay(260)
                                        launchDelegateHandsFreeListen()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                LaunchedEffect(delegateState.currentIndex, delegateState.activeQuestionIndex, subMode, autoSpeakDelegate, voice.ttsReady) {
                    if (subMode == 1 && delegateState.lastFeedback == null && voice.ttsReady) {
                        if (vivaHandsFreeLive) {
                            voice.speak(delegateQuestion.question.get(lang)) {
                                if (vivaHandsFreeLive && subMode == 1 && !delegateState.isEvaluating) {
                                    scope.launch {
                                        delay(260)
                                        launchDelegateHandsFreeListen()
                                    }
                                }
                            }
                        } else if (autoSpeakDelegate) {
                            voice.speak(delegateQuestion.question.get(lang))
                        }
                    }
                }
                LaunchedEffect(delegateState.lastFeedback) {
                    val fb = delegateState.lastFeedback
                    if (!fb.isNullOrBlank()) {
                        if (vivaHandsFreeLive) {
                            voice.speak(fb) {
                                if (vivaHandsFreeLive && subMode == 1) {
                                    scope.launch {
                                        delay(340)
                                        if (delegateState.lastPassed == true) {
                                            delegateAnswerInput = ""
                                            onNextDelegateQuestion(allQuestions.size)
                                        } else {
                                            launchDelegateHandsFreeListen()
                                        }
                                    }
                                }
                            }
                        } else if (autoSpeakDelegate) {
                            voice.speak(fb)
                        }
                    }
                }

                val delegatePhase = when {
                    delegateState.isEvaluating -> CenterpiecePhase.THINKING
                    voice.isSpeaking -> CenterpiecePhase.SPEAKING
                    voice.isListening -> CenterpiecePhase.LISTENING
                    else -> CenterpiecePhase.IDLE
                }

                WeldEnergyOrbCenterpiece(
                    phase = delegatePhase,
                    title = AppStrings.delegateModeTitle.get(lang),
                    subtitle = when {
                        voice.partialTranscript.isNotBlank() -> "${AppStrings.liveHearingPrefix.get(lang)}: \"${voice.partialTranscript}\""
                        delegateState.isEvaluating -> AppStrings.delegateEvaluating.get(lang)
                        voice.isSpeaking -> if (bargeInEnabled) AppStrings.orbTapToInterrupt.get(lang) else AppStrings.liveSpeakingPrompt.get(lang)
                        voice.isListening -> AppStrings.liveListeningPrompt.get(lang)
                        else -> AppStrings.delegateModeSubtitle.get(lang)
                    },
                    badgeText = if (aiExaminerReady) AppStrings.delegateAiBadge.get(lang) else AppStrings.delegateOfflineBadge.get(lang),
                    lang = lang,
                    compact = false,
                    onOrbClick = { handleDelegateOrbTap() },
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = palette.arcCyan, contentPadding = 14.dp) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ArcStatusPill(
                            text = "${delegateState.completedQuestionIds.size} / ${allQuestions.size} ${AppStrings.knewIt.get(lang)}",
                            color = palette.arcCyan
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                voice.stopSpeaking()
                                delegateAnswerInput = ""
                                onShuffleDelegateQuestions(allQuestions.size)
                            },
                            label = { Text(AppStrings.shuffleQuestionsBtn.get(lang)) },
                            leadingIcon = {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("delegate_shuffle_btn")
                        )
                        FilterChip(
                            selected = autoSpeakDelegate,
                            onClick = { autoSpeakDelegate = !autoSpeakDelegate },
                            label = { Text(AppStrings.voiceAutoSpeak.get(lang)) },
                            leadingIcon = {
                                Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("delegate_auto_speak_chip")
                        )
                        FilterChip(
                            selected = vivaHandsFreeLive,
                            onClick = {
                                vivaHandsFreeLive = !vivaHandsFreeLive
                                if (vivaHandsFreeLive) {
                                    autoSpeakDelegate = true
                                    voice.clearError()
                                    if (delegateState.lastFeedback != null || !voice.ttsReady) {
                                        launchDelegateHandsFreeListen()
                                    } else {
                                        voice.speak(delegateQuestion.question.get(lang)) {
                                            if (vivaHandsFreeLive && subMode == 1 && !delegateState.isEvaluating) {
                                                scope.launch {
                                                    delay(260)
                                                    launchDelegateHandsFreeListen()
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    voice.stopListening()
                                    voice.stopSpeaking()
                                }
                            },
                            label = { Text(AppStrings.vivaHandsFreeToggle.get(lang)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (vivaHandsFreeLive) Icons.Filled.Stop else Icons.Filled.GraphicEq,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.passEmerald.copy(alpha = 0.24f),
                                selectedLabelColor = palette.passEmerald
                            ),
                            modifier = Modifier.testTag("delegate_hands_free_chip")
                        )
                    }
                }
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (alreadyCompleted) palette.passEmerald.copy(alpha = 0.5f) else palette.arcCyan.copy(alpha = 0.4f),
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            ArcStatusPill(text = delegateQuestion.topic.get(lang), color = palette.arcCyan)
                            if (alreadyCompleted) {
                                ArcStatusPill(text = AppStrings.knewIt.get(lang), color = palette.passEmerald, icon = Icons.Filled.CheckCircle)
                            }
                        }
                        Text(
                            text = "${(delegateState.currentIndex % allQuestions.size) + 1} / ${allQuestions.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = delegateQuestion.question.get(lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (voice.isSpeaking) {
                                    voice.stopSpeaking()
                                } else {
                                    voice.speak(delegateQuestion.question.get(lang))
                                }
                            },
                            modifier = Modifier.testTag("delegate_speak_question_btn"),
                            border = BorderStroke(1.dp, palette.arcCyan.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (voice.isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                                contentDescription = AppStrings.voiceTapListen.get(lang),
                                tint = palette.arcCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (voice.isSpeaking) AppStrings.voiceStopSpeaking.get(lang) else AppStrings.voiceTapListen.get(lang),
                                color = palette.arcCyan
                            )
                        }

                        Button(
                            onClick = {
                                vivaHandsFreeLive = false
                                if (voice.isListening) {
                                    voice.stopListening()
                                } else {
                                    voice.startListening { spoken ->
                                        delegateAnswerInput = if (delegateAnswerInput.isBlank()) spoken else "$delegateAnswerInput $spoken"
                                        onResetDelegateFeedback()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("delegate_voice_answer_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (voice.isListening) palette.warnRose else palette.violetSpark,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (voice.isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                                contentDescription = AppStrings.voiceSpeakAnswer.get(lang),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (voice.isListening) AppStrings.voiceListening.get(lang) else AppStrings.voiceSpeakAnswer.get(lang))
                        }
                    }

                    voice.voiceError?.let { vErr ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(vErr, style = MaterialTheme.typography.bodySmall, color = palette.warnRose)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = delegateAnswerInput,
                        onValueChange = { delegateAnswerInput = it },
                        placeholder = {
                            Text(
                                text = if (voice.isListening && voice.partialTranscript.isNotBlank()) voice.partialTranscript
                                else AppStrings.delegateAnswerHint.get(lang),
                                color = if (voice.isListening) palette.arcCyan else palette.textSecondary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delegate_answer_input"),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (delegateAnswerInput.isNotBlank() && !delegateState.isEvaluating) {
                                    onSubmitDelegateAnswer(delegateQuestion, delegateAnswerInput)
                                }
                            },
                            enabled = delegateAnswerInput.isNotBlank() && !delegateState.isEvaluating,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("delegate_submit_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (delegateState.isEvaluating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(AppStrings.delegateEvaluating.get(lang))
                            } else {
                                Text(AppStrings.delegateSubmitBtn.get(lang))
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                voice.stopSpeaking()
                                delegateAnswerInput = ""
                                onNextDelegateQuestion(allQuestions.size)
                            },
                            modifier = Modifier.testTag("delegate_next_btn"),
                            border = BorderStroke(1.dp, palette.glassBorder)
                        ) {
                            Text(AppStrings.nextQuestion.get(lang), color = palette.textPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = palette.textPrimary)
                        }
                    }

                    AnimatedVisibility(visible = delegateState.lastFeedback != null) {
                        val passed = delegateState.lastPassed == true
                        val badgeCol = if (passed) palette.passEmerald else palette.moltenAmber
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = badgeCol.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, badgeCol.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ArcStatusPill(
                                        text = if (passed) AppStrings.delegatePassedBadge.get(lang) else AppStrings.delegateReviewBadge.get(lang),
                                        color = badgeCol
                                    )
                                    IconButton(
                                        onClick = {
                                            val fb = delegateState.lastFeedback.orEmpty()
                                            if (voice.isSpeaking) voice.stopSpeaking() else voice.speak(fb)
                                        },
                                        modifier = Modifier.testTag("delegate_speak_feedback_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (voice.isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                                            contentDescription = AppStrings.voiceTapListen.get(lang),
                                            tint = badgeCol
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = delegateState.lastFeedback.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Rod & Code Flashcard Memorizer
            item {
                val masteredCount = masteredIds.size
                GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = palette.arcCyan, contentPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.rodMemorizerTab.get(lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        ArcStatusPill(
                            text = "$masteredCount / ${cards.size} ${AppStrings.knewIt.get(lang)}",
                            color = palette.passEmerald
                        )
                    }
                }
            }

            item {
                val isMastered = masteredIds.contains(currentCard.id)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flashcard_surface"),
                    accentGlow = if (isCardFlipped) palette.moltenAmber else palette.arcCyan,
                    borderColor = if (isMastered) palette.passEmerald.copy(alpha = 0.5f) else palette.arcCyan.copy(alpha = 0.4f),
                    onClick = { isCardFlipped = !isCardFlipped },
                    contentPadding = 22.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArcStatusPill(text = currentCard.category.get(lang), color = palette.arcCyan)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Flip, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${(cardIdx % cards.size) + 1}/${cards.size}", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = currentCard.codeTitle,
                        style = MaterialTheme.typography.displaySmall,
                        color = if (isCardFlipped) palette.moltenAmber else palette.arcCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentCard.frontSubtitle.get(lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )

                    if (!isCardFlipped) {
                        Spacer(modifier = Modifier.height(22.dp))
                        Text(
                            text = AppStrings.tapToFlip.get(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (palette.isLight) Color(0xFFF1F5F9) else Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, palette.glassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = currentCard.backSpecs.get(lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = currentCard.backTip.get(lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.moltenAmber
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onToggleMasteredCard(currentCard.id, false)
                            isCardFlipped = false
                            cardIdx = (cardIdx + 1) % cards.size
                        },
                        modifier = Modifier.weight(1f).testTag("flashcard_review_btn"),
                        border = BorderStroke(1.dp, palette.moltenAmber.copy(alpha = 0.5f))
                    ) {
                        Text(AppStrings.reviewMore.get(lang), color = palette.moltenAmber)
                    }
                    Button(
                        onClick = {
                            onToggleMasteredCard(currentCard.id, true)
                            isCardFlipped = false
                            cardIdx = (cardIdx + 1) % cards.size
                        },
                        modifier = Modifier.weight(1f).testTag("flashcard_mastered_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.knewIt.get(lang))
                    }
                }
            }
        }
    }
}
