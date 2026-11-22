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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.content.VivaKnowledgeBase
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCard
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.PassEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletSpark
import com.example.ui.theme.WarnRose
import com.example.ui.viewmodel.WeldUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VivaQuizScreen(
    uiState: WeldUiState,
    onRecordAttempt: (String, String, String, Boolean) -> Unit,
    onResetStats: () -> Unit,
    onToggleMasteredCard: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.preferences.language
    val metrics = uiState.metrics
    var subMode by remember { mutableIntStateOf(0) } // 0 = Quiz, 1 = Flashcards
    var selectedTopicEn by remember { mutableStateOf("ALL") }
    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }

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
    val safeQuestion = filteredQuestions[currentQuestionIdx % filteredQuestions.size]

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
                    onClick = { subMode = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("tab_viva_quiz"),
                    icon = { Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text(AppStrings.vivaQuizTab.get(lang))
                }
                SegmentedButton(
                    selected = subMode == 1,
                    onClick = { subMode = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("tab_rod_memorizer"),
                    icon = { Icon(Icons.Filled.Style, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text(AppStrings.rodMemorizerTab.get(lang))
                }
            }
        }

        if (subMode == 0) {
            // Accuracy & Weak Topics Summary
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = VioletSpark, contentPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${AppStrings.accuracyLabel.get(lang)}: ${metrics.quizAccuracyPct}%",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = "${AppStrings.answeredLabel.get(lang)}: ${metrics.totalQuizAnswered} • ${filteredQuestions.size} Qs",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        if (metrics.totalQuizAnswered > 0) {
                            TextButton(onClick = onResetStats, modifier = Modifier.testTag("reset_quiz_btn")) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.resetStats.get(lang), color = TextSecondary)
                            }
                        }
                    }

                    if (metrics.weakTopics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = AppStrings.weakTopicsLabel.get(lang),
                            style = MaterialTheme.typography.labelLarge,
                            color = MoltenAmber
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            metrics.weakTopics.forEach { stat ->
                                val label = if (lang == AppLanguage.BN) stat.topicBn else stat.topicEn
                                ArcStatusPill(
                                    text = "$label (${stat.accuracyPct}%)",
                                    color = MoltenAmber,
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
                                    currentQuestionIdx = 0
                                    selectedOption = null
                                },
                                label = { Text(AppStrings.weakTopicsLabel.get(lang)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MoltenAmber.copy(alpha = 0.25f),
                                    selectedLabelColor = MoltenAmber
                                )
                            )
                        }
                    }
                    items(allTopics) { topic ->
                        FilterChip(
                            selected = selectedTopicEn == topic.en,
                            onClick = {
                                selectedTopicEn = topic.en
                                currentQuestionIdx = 0
                                selectedOption = null
                            },
                            label = { Text(topic.get(lang)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArcCyan.copy(alpha = 0.22f),
                                selectedLabelColor = ArcCyan
                            )
                        )
                    }
                }
            }

            // Question Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ArcCyan.copy(alpha = 0.35f),
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArcStatusPill(text = safeQuestion.topic.get(lang), color = ArcCyan)
                        Text(
                            text = "${(currentQuestionIdx % filteredQuestions.size) + 1} / ${filteredQuestions.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = safeQuestion.question.get(lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    safeQuestion.options.forEachIndexed { idx, opt ->
                        val answered = selectedOption != null
                        val isChosen = selectedOption == idx
                        val isRightOption = idx == safeQuestion.correctIndex

                        val borderCol = when {
                            !answered -> GlassBorder
                            isRightOption -> PassEmerald
                            isChosen -> WarnRose
                            else -> GlassBorder
                        }
                        val bgCol = when {
                            !answered -> Color.White.copy(alpha = 0.04f)
                            isRightOption -> PassEmerald.copy(alpha = 0.18f)
                            isChosen -> WarnRose.copy(alpha = 0.18f)
                            else -> Color.White.copy(alpha = 0.02f)
                        }

                        Surface(
                            onClick = {
                                if (selectedOption == null) {
                                    selectedOption = idx
                                    onRecordAttempt(
                                        safeQuestion.id,
                                        safeQuestion.topic.en,
                                        safeQuestion.topic.bn,
                                        idx == safeQuestion.correctIndex
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
                                    color = ArcCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = opt.get(lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (answered && isRightOption) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PassEmerald, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = selectedOption != null) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ArcCyan.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = AppStrings.explanationHeader.get(lang),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ArcCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = safeQuestion.explanation.get(lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    currentQuestionIdx = (currentQuestionIdx + 1) % filteredQuestions.size
                                    selectedOption = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quiz_next_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = Color(0xFF04131E))
                            ) {
                                Text(AppStrings.nextQuestion.get(lang))
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // Rod & Code Flashcard Memorizer
            item {
                val masteredCount = masteredIds.size
                GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = ArcCyan, contentPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.rodMemorizerTab.get(lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        ArcStatusPill(
                            text = "$masteredCount / ${cards.size} ${AppStrings.knewIt.get(lang)}",
                            color = PassEmerald
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
                    accentGlow = if (isCardFlipped) MoltenAmber else ArcCyan,
                    borderColor = if (isMastered) PassEmerald.copy(alpha = 0.5f) else ArcCyan.copy(alpha = 0.4f),
                    onClick = { isCardFlipped = !isCardFlipped },
                    contentPadding = 22.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArcStatusPill(text = currentCard.category.get(lang), color = ArcCyan)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Flip, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${(cardIdx % cards.size) + 1}/${cards.size}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = currentCard.codeTitle,
                        style = MaterialTheme.typography.displaySmall,
                        color = if (isCardFlipped) MoltenAmber else ArcCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentCard.frontSubtitle.get(lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    if (!isCardFlipped) {
                        Spacer(modifier = Modifier.height(22.dp))
                        Text(
                            text = AppStrings.tapToFlip.get(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = currentCard.backSpecs.get(lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = currentCard.backTip.get(lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MoltenAmber
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
                        border = BorderStroke(1.dp, MoltenAmber.copy(alpha = 0.5f))
                    ) {
                        Text(AppStrings.reviewMore.get(lang), color = MoltenAmber)
                    }
                    Button(
                        onClick = {
                            onToggleMasteredCard(currentCard.id, true)
                            isCardFlipped = false
                            cardIdx = (cardIdx + 1) % cards.size
                        },
                        modifier = Modifier.weight(1f).testTag("flashcard_mastered_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PassEmerald, contentColor = Color(0xFF022015))
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
