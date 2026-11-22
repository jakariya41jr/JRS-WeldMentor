package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DailyTaskEntity
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.DeepSlateBg
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassSectionHeader
import com.example.ui.theme.WeldTheme
import com.example.ui.viewmodel.WeldUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: WeldUiState,
    onOpenNewLog: () -> Unit,
    onOpenSummaryCard: () -> Unit,
    onOpenPlanners: () -> Unit,
    onOpenCurrency: () -> Unit,
    onOpenVivaDelegator: () -> Unit,
    onAddPracticeMinutes: (Int) -> Unit,
    onAddTask: (String, String, String) -> Unit,
    onToggleTask: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = WeldTheme.colors
    val context = LocalContext.current
    val lang = uiState.preferences.language
    val metrics = uiState.metrics
    val prefs = uiState.preferences
    var showAddTaskDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Banner Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                borderColor = palette.arcCyan.copy(alpha = 0.35f),
                contentPadding = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(198.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_weld_hero),
                        contentDescription = AppStrings.heroTitle.get(lang),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        DeepSlateBg.copy(alpha = 0.35f),
                                        DeepSlateBg.copy(alpha = 0.78f),
                                        DeepSlateBg.copy(alpha = 0.96f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ArcStatusPill(
                                text = prefs.targetTrade,
                                color = Color(0xFF38BDF8),
                                icon = Icons.Filled.Verified
                            )
                            ArcStatusPill(
                                text = "${prefs.streakDays} ${AppStrings.daysUnit.get(lang)}",
                                color = Color(0xFFF59E0B),
                                icon = Icons.Filled.LocalFireDepartment
                            )
                        }

                        Column {
                            Text(
                                text = AppStrings.heroTitle.get(lang),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFFF1F5F9),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = AppStrings.heroSubtitle.get(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = onOpenNewLog,
                                    modifier = Modifier.testTag("home_log_new_weld_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = AppStrings.logNewWeld.get(lang),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                OutlinedButton(
                                    onClick = onOpenSummaryCard,
                                    modifier = Modifier.testTag("home_summary_card_btn"),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Assessment,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = AppStrings.shareReport.get(lang),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Progress Breakdown Grid (Practical Jobs, Viva Readiness, Streak, Pass Rate)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricProgressCard(
                        title = AppStrings.practicalJobsLabel.get(lang),
                        value = "${metrics.totalJobs}",
                        subtitle = "${metrics.uniquePositions.size}/6 ${AppStrings.positionsMastered.get(lang)}",
                        progress = metrics.practicalProgress,
                        accentColor = palette.arcCyan,
                        icon = Icons.Filled.Build,
                        modifier = Modifier.weight(1f)
                    )
                    MetricProgressCard(
                        title = AppStrings.vivaReadinessLabel.get(lang),
                        value = "${metrics.quizAccuracyPct}%",
                        subtitle = "${metrics.totalQuizAnswered} ${AppStrings.answeredLabel.get(lang)}",
                        progress = metrics.vivaProgress,
                        accentColor = palette.violetSpark,
                        icon = Icons.Filled.Quiz,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricProgressCard(
                        title = AppStrings.passRateLabel.get(lang),
                        value = "${metrics.passRatePct}%",
                        subtitle = "${metrics.passCount}/${metrics.totalJobs} Pass",
                        progress = if (metrics.totalJobs == 0) 0f else metrics.passRatePct / 100f,
                        accentColor = palette.passEmerald,
                        icon = Icons.Filled.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        accentGlow = palette.moltenAmber,
                        contentPadding = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = palette.moltenAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Surface(
                                onClick = { onAddPracticeMinutes(15) },
                                modifier = Modifier.testTag("add_15_min_btn"),
                                shape = RoundedCornerShape(8.dp),
                                color = palette.moltenAmber.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, palette.moltenAmber.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = AppStrings.add15Min.get(lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.moltenAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${prefs.practiceMinutesToday}m",
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "${AppStrings.practiceMinutes.get(lang)} / ${prefs.dailyMinutesGoal}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = {
                                (prefs.practiceMinutesToday.toFloat() / prefs.dailyMinutesGoal.coerceAtLeast(1))
                                    .coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = palette.moltenAmber,
                            trackColor = palette.progressTrack,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Completed Positions Badges
        if (metrics.uniquePositions.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                    Text(
                        text = AppStrings.positionsMastered.get(lang),
                        style = MaterialTheme.typography.titleSmall,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        metrics.uniquePositions.forEach { pos ->
                            ArcStatusPill(text = pos, color = palette.passEmerald)
                        }
                    }
                }
            }
        }

        // Daily Tasks & Learning Tracker
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
                GlassSectionHeader(
                    title = AppStrings.dailyGoalsHeader.get(lang),
                    subtitle = "${metrics.todayJobs}/${prefs.dailyJobGoal} Jobs • ${metrics.todayQuizAnswered}/${prefs.dailyQuizGoal} Quiz",
                    icon = Icons.Filled.CheckCircle,
                    iconTint = palette.passEmerald,
                    trailing = {
                        Surface(
                            onClick = { showAddTaskDialog = true },
                            modifier = Modifier.testTag("home_add_task_btn"),
                            shape = RoundedCornerShape(10.dp),
                            color = palette.arcCyan.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, palette.arcCyan.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = AppStrings.addTask.get(lang),
                                    tint = palette.arcCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = AppStrings.addTask.get(lang),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.arcCyan
                                )
                            }
                        }
                    }
                )
            }
        }

        items(uiState.tasks, key = { it.id }) { task ->
            DailyTaskRowCard(
                task = task,
                lang = lang,
                onToggle = { onToggleTask(task.id, !task.isDone) },
                onDelete = { onDeleteTask(task.id) }
            )
        }

        // Viva Delegator Voice Interview Spotlight
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_viva_delegator_card"),
                accentGlow = palette.violetSpark,
                borderColor = palette.violetSpark.copy(alpha = 0.45f),
                onClick = onOpenVivaDelegator,
                contentPadding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.violetSpark.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, palette.violetSpark.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RecordVoiceOver,
                            contentDescription = AppStrings.delegateHomeTitle.get(lang),
                            tint = palette.violetSpark,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.delegateHomeTitle.get(lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = AppStrings.delegateHomeDesc.get(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = palette.violetSpark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Quick Trade Readiness Tools
        item {
            GlassSectionHeader(
                title = AppStrings.quickToolsHeader.get(lang),
                icon = Icons.Filled.Build,
                iconTint = palette.arcCyan
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickToolTile(
                        title = AppStrings.stancePlannerTitle.get(lang),
                        subtitle = AppStrings.stancePlannerDesc.get(lang),
                        icon = Icons.Filled.Build,
                        accent = palette.moltenAmber,
                        tag = "tool_stance_planner",
                        onClick = onOpenPlanners,
                        modifier = Modifier.weight(1f)
                    )
                    QuickToolTile(
                        title = AppStrings.tradeTestTitle.get(lang),
                        subtitle = AppStrings.tradeTestDesc.get(lang),
                        icon = Icons.Filled.FlightTakeoff,
                        accent = palette.passEmerald,
                        tag = "tool_trade_test",
                        onClick = onOpenPlanners,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickToolTile(
                        title = AppStrings.currencyHomeTitle.get(lang),
                        subtitle = AppStrings.currencyHomeDesc.get(lang),
                        icon = Icons.Filled.CurrencyExchange,
                        accent = palette.violetSpark,
                        tag = "tool_currency_converter",
                        onClick = onOpenCurrency,
                        modifier = Modifier.weight(1f)
                    )
                    QuickToolTile(
                        title = AppStrings.summaryCardTitle.get(lang),
                        subtitle = AppStrings.summaryCardDesc.get(lang),
                        icon = Icons.Filled.Assessment,
                        accent = palette.arcCyan,
                        tag = "tool_summary_card",
                        onClick = onOpenSummaryCard,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Footer Brand & Developer Ownership Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_dev_brand_card"),
                borderColor = palette.arcCyan.copy(alpha = 0.35f),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppStrings.devWebsiteUrl))
                    context.startActivity(intent)
                },
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_weldmentor_symbol),
                        contentDescription = AppStrings.appName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.appName,
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Developed by ${AppStrings.devName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                    ArcStatusPill(
                        text = AppStrings.devWebsite,
                        color = palette.arcCyan,
                        icon = Icons.Filled.OpenInBrowser
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showAddTaskDialog) {
        AddDailyTaskDialog(
            lang = lang,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, cat ->
                onAddTask(title, title, cat)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
private fun MetricProgressCard(
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val palette = WeldTheme.colors
    GlassCard(
        modifier = modifier,
        accentGlow = accentColor,
        contentPadding = 14.dp
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = palette.textPrimary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accentColor,
            trackColor = palette.progressTrack,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun DailyTaskRowCard(
    task: DailyTaskEntity,
    lang: AppLanguage,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = WeldTheme.colors
    val title = if (lang == AppLanguage.BN && task.titleBn.isNotBlank()) task.titleBn else task.titleEn
    val catColor = when (task.category) {
        "PRACTICAL" -> palette.arcCyan
        "VIVA" -> palette.violetSpark
        else -> palette.moltenAmber
    }
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_task_${task.id}"),
        onClick = onToggle,
        borderColor = if (task.isDone) palette.passEmerald.copy(alpha = 0.4f) else palette.glassBorder,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (task.isDone) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = title,
                tint = if (task.isDone) palette.passEmerald else palette.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isDone) palette.textMuted else palette.textPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                ArcStatusPill(text = task.category, color = catColor)
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = AppStrings.delete.get(lang),
                    tint = palette.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickToolTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = WeldTheme.colors
    GlassCard(
        modifier = modifier.testTag(tag),
        accentGlow = accent,
        onClick = onClick,
        contentPadding = 14.dp
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = accent.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AddDailyTaskDialog(
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val palette = WeldTheme.colors
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("PRACTICAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        title = {
            Text(AppStrings.newTaskTitle.get(lang), color = palette.textPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text(AppStrings.taskTitleHint.get(lang), color = palette.textMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_input"),
                    singleLine = false,
                    maxLines = 3
                )
                Text(AppStrings.taskCategoryLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = palette.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "PRACTICAL" to AppStrings.catPractical.get(lang),
                        "VIVA" to AppStrings.catViva.get(lang),
                        "SAFETY" to AppStrings.catSafety.get(lang)
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = category == key,
                            onClick = { category = key },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.arcCyan.copy(alpha = 0.22f),
                                selectedLabelColor = palette.arcCyan
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), category) },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_new_task_btn")
            ) {
                Text(AppStrings.save.get(lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.cancel.get(lang), color = palette.textSecondary)
            }
        }
    )
}
