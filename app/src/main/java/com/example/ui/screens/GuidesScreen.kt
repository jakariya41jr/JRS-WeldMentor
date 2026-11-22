package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.content.WorkshopGuidesData
import com.example.i18n.AppStrings
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassSectionHeader
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.PassEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarnRose
import com.example.ui.viewmodel.WeldUiState

@Composable
fun GuidesScreen(
    uiState: WeldUiState,
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    onToggleChecklist: (String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.preferences.language

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tabs = listOf(
                    4 to AppStrings.tabTheory.get(lang),
                    0 to AppStrings.tabTips.get(lang),
                    1 to AppStrings.tabDefects.get(lang),
                    2 to AppStrings.tabParams.get(lang),
                    3 to AppStrings.tabPlanners.get(lang)
                )
                items(tabs) { (idx, label) ->
                    FilterChip(
                        selected = selectedSubTab == idx,
                        onClick = { onSelectSubTab(idx) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("guide_subtab_$idx"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArcCyan.copy(alpha = 0.22f),
                            selectedLabelColor = ArcCyan
                        )
                    )
                }
            }
        }

        when (selectedSubTab) {
            4 -> {
                items(WorkshopGuidesData.theoryTopics, key = { it.id }) { topic ->
                    GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = PassEmerald, contentPadding = 16.dp) {
                        ArcStatusPill(text = topic.category.get(lang), color = PassEmerald, icon = Icons.Filled.MenuBook)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(topic.title.get(lang), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(topic.summary.get(lang), style = MaterialTheme.typography.bodySmall, color = MoltenAmber)
                        Spacer(modifier = Modifier.height(10.dp))
                        topic.steps.forEach { step ->
                            Text(
                                text = step.get(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            0 -> {
                items(WorkshopGuidesData.handsOnTips, key = { it.id }) { tip ->
                    GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = ArcCyan, contentPadding = 16.dp) {
                        ArcStatusPill(text = tip.category.get(lang), color = ArcCyan, icon = Icons.Filled.Handyman)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(tip.title.get(lang), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(tip.summary.get(lang), style = MaterialTheme.typography.bodySmall, color = MoltenAmber)
                        Spacer(modifier = Modifier.height(10.dp))
                        tip.steps.forEach { step ->
                            Text(
                                text = step.get(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            1 -> {
                items(WorkshopGuidesData.defectGuides, key = { it.id }) { defect ->
                    GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = WarnRose, contentPadding = 16.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = WarnRose, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(defect.title.get(lang), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(defect.visualCue.get(lang), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(AppStrings.likelyCauses.get(lang), style = MaterialTheme.typography.labelLarge, color = MoltenAmber)
                        Text(defect.causes.get(lang), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(AppStrings.practicalFixes.get(lang), style = MaterialTheme.typography.labelLarge, color = PassEmerald)
                        Text(defect.fixes.get(lang), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }

            2 -> {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MoltenAmber.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, MoltenAmber.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = MoltenAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.paramDisclaimer.get(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }

                items(WorkshopGuidesData.paramReferenceRows) { row ->
                    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ArcStatusPill(text = row.process, color = ArcCyan, icon = Icons.Filled.Tune)
                                ArcStatusPill(text = row.diameter, color = MoltenAmber)
                            }
                            Text(row.polarity, style = MaterialTheme.typography.labelMedium, color = PassEmerald)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(row.rodOrWire, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${row.position}  •  ${row.ampsOrVolts}  •  ${row.gasFlow}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            else -> {
                // Position & Stance Planner + Trade Test & Overseas Readiness
                val stanceDone = WorkshopGuidesData.stanceChecklist.count { uiState.checklistMap[it.id] == true }
                val stanceTotal = WorkshopGuidesData.stanceChecklist.size
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = MoltenAmber, contentPadding = 14.dp) {
                        GlassSectionHeader(
                            title = AppStrings.stancePlannerTitle.get(lang),
                            subtitle = "$stanceDone / $stanceTotal",
                            icon = Icons.Filled.Build,
                            iconTint = MoltenAmber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { stanceDone.toFloat() / stanceTotal.coerceAtLeast(1) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                            color = MoltenAmber,
                            trackColor = Color.White.copy(alpha = 0.08f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
                items(WorkshopGuidesData.stanceChecklist, key = { it.id }) { item ->
                    val checked = uiState.checklistMap[item.id] == true
                    ChecklistItemCard(
                        id = item.id,
                        title = item.title.get(lang),
                        detail = item.detail.get(lang),
                        checked = checked,
                        accent = MoltenAmber,
                        onToggle = { onToggleChecklist(item.id, item.group, !checked) }
                    )
                }

                val tradeDone = WorkshopGuidesData.tradeTestChecklist.count { uiState.checklistMap[it.id] == true }
                val tradeTotal = WorkshopGuidesData.tradeTestChecklist.size
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = PassEmerald, contentPadding = 14.dp) {
                        GlassSectionHeader(
                            title = AppStrings.tradeTestTitle.get(lang),
                            subtitle = "$tradeDone / $tradeTotal",
                            icon = Icons.Filled.FlightTakeoff,
                            iconTint = PassEmerald
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { tradeDone.toFloat() / tradeTotal.coerceAtLeast(1) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                            color = PassEmerald,
                            trackColor = Color.White.copy(alpha = 0.08f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
                items(WorkshopGuidesData.tradeTestChecklist, key = { it.id }) { item ->
                    val checked = uiState.checklistMap[item.id] == true
                    ChecklistItemCard(
                        id = item.id,
                        title = item.title.get(lang),
                        detail = item.detail.get(lang),
                        checked = checked,
                        accent = PassEmerald,
                        onToggle = { onToggleChecklist(item.id, item.group, !checked) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ChecklistItemCard(
    id: String,
    title: String,
    detail: String,
    checked: Boolean,
    accent: Color,
    onToggle: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("checklist_$id"),
        onClick = onToggle,
        borderColor = if (checked) accent.copy(alpha = 0.45f) else GlassBorder,
        contentPadding = 14.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = if (checked) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = title,
                tint = if (checked) accent else TextMuted,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(modifier = Modifier.height(3.dp))
                Text(detail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
