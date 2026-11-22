package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.WeldJobEntity
import com.example.data.model.BaseMaterial
import com.example.data.model.JobStatus
import com.example.data.model.WeldPosition
import com.example.data.model.WeldProcess
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.GlassCard
import com.example.ui.theme.WeldTheme
import com.example.ui.viewmodel.LogbookFilter
import com.example.ui.viewmodel.WeldUiState

@Composable
fun LogbookScreen(
    uiState: WeldUiState,
    onUpdateFilter: ((LogbookFilter) -> LogbookFilter) -> Unit,
    onOpenNewForm: () -> Unit,
    onEditJob: (WeldJobEntity) -> Unit,
    onDeleteJob: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = WeldTheme.colors
    val lang = uiState.preferences.language
    val filter = uiState.logFilter
    var pendingDeleteJob by remember { mutableStateOf<WeldJobEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenNewForm,
                modifier = Modifier.testTag("logbook_fab_add"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = AppStrings.logNewWeld.get(lang))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = AppStrings.logbookTitle.get(lang),
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = filter.searchQuery,
                    onValueChange = { q -> onUpdateFilter { it.copy(searchQuery = q) } },
                    placeholder = { Text(AppStrings.searchLogsHint.get(lang), color = palette.textMuted) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = palette.arcCyan) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logbook_search_input")
                )
            }

            // Process & Material & Status Filter Row
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filter.processFilter == "ALL",
                            onClick = { onUpdateFilter { it.copy(processFilter = "ALL") } },
                            label = { Text("${AppStrings.allFilter.get(lang)} (${uiState.jobs.size})") }
                        )
                    }
                    items(WeldProcess.entries) { proc ->
                        FilterChip(
                            selected = filter.processFilter == proc.label,
                            onClick = {
                                onUpdateFilter {
                                    it.copy(processFilter = if (it.processFilter == proc.label) "ALL" else proc.label)
                                }
                            },
                            label = { Text(proc.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.arcCyan.copy(alpha = 0.22f),
                                selectedLabelColor = palette.arcCyan
                            )
                        )
                    }
                    items(BaseMaterial.entries) { mat ->
                        FilterChip(
                            selected = filter.materialFilter == mat.code,
                            onClick = {
                                onUpdateFilter {
                                    it.copy(materialFilter = if (it.materialFilter == mat.code) "ALL" else mat.code)
                                }
                            },
                            label = { Text(mat.code) }
                        )
                    }
                    items(JobStatus.entries) { st ->
                        FilterChip(
                            selected = filter.statusFilter == st.code,
                            onClick = {
                                onUpdateFilter {
                                    it.copy(statusFilter = if (it.statusFilter == st.code) "ALL" else st.code)
                                }
                            },
                            label = { Text(st.bi.get(lang)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filter.positionFilter == "ALL",
                            onClick = { onUpdateFilter { it.copy(positionFilter = "ALL") } },
                            label = { Text("${AppStrings.positionLabel.get(lang)}: ${AppStrings.allFilter.get(lang)}") }
                        )
                    }
                    items(WeldPosition.entries) { pos ->
                        FilterChip(
                            selected = filter.positionFilter == pos.code,
                            onClick = {
                                onUpdateFilter {
                                    it.copy(positionFilter = if (it.positionFilter == pos.code) "ALL" else pos.code)
                                }
                            },
                            label = { Text(pos.code) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.moltenAmber.copy(alpha = 0.22f),
                                selectedLabelColor = palette.moltenAmber
                            )
                        )
                    }
                }
            }

            if (uiState.filteredJobs.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentGlow = palette.arcCyan,
                        contentPadding = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Build,
                                contentDescription = null,
                                tint = palette.arcCyan,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = AppStrings.noLogsTitle.get(lang),
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = AppStrings.noLogsDesc.get(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onOpenNewForm,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(AppStrings.logNewWeld.get(lang))
                            }
                        }
                    }
                }
            } else {
                items(uiState.filteredJobs, key = { it.id }) { job ->
                    WeldJobCard(
                        job = job,
                        lang = lang,
                        onEdit = { onEditJob(job) },
                        onDelete = { pendingDeleteJob = job }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    pendingDeleteJob?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteJob = null },
            containerColor = palette.surface,
            title = { Text(AppStrings.confirmDelete.get(lang), color = palette.textPrimary) },
            text = {
                Text("${target.date} • ${target.process} ${target.position} (${target.rodFillerAndSize})", color = palette.textSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteJob(target.id)
                        pendingDeleteJob = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.warnRose)
                ) {
                    Text(AppStrings.delete.get(lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteJob = null }) {
                    Text(AppStrings.cancel.get(lang), color = palette.textSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeldJobCard(
    job: WeldJobEntity,
    lang: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = WeldTheme.colors
    val isPass = job.status == JobStatus.PASS.code
    val statusColor = if (isPass) palette.passEmerald else palette.moltenAmber

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weld_job_card_${job.id}"),
        borderColor = if (isPass) palette.passEmerald.copy(alpha = 0.38f) else palette.glassBorder,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ArcStatusPill(text = job.process, color = palette.arcCyan)
                ArcStatusPill(text = job.position, color = palette.moltenAmber)
                ArcStatusPill(text = "${job.baseMaterial} • ${job.jointType}", color = palette.textSecondary)
            }
            ArcStatusPill(
                text = if (isPass) JobStatus.PASS.bi.get(lang) else JobStatus.NEEDS_IMPROVEMENT.bi.get(lang),
                color = statusColor,
                icon = if (isPass) Icons.Filled.CheckCircle else Icons.Filled.WarningAmber
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = job.rodFillerAndSize,
            style = MaterialTheme.typography.titleMedium,
            color = palette.textPrimary
        )
        Text(
            text = "${job.date}  •  ${job.amperage.ifBlank { "-" }} ${if (job.voltage.isNotBlank()) "/ ${job.voltage}" else ""}  •  ${job.gasFlow.ifBlank { "-" }}",
            style = MaterialTheme.typography.labelMedium,
            color = palette.arcCyan
        )

        if (job.tigCup.isNotBlank() || job.tigTungsten.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "TIG: Cup ${job.tigCup} | ${job.tigColletBody} | Cap ${job.tigBackCap} | ${job.tigTungsten}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary
            )
        }

        if (job.measuringTools.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${AppStrings.measuringToolsLabel.get(lang)}: ${job.measuringTools}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (job.ppeChecked) {
                ArcStatusPill(text = "PPE OK", color = palette.passEmerald)
            }
            job.defectTags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                val isClean = tag.equals("Clean/Pass", ignoreCase = true)
                ArcStatusPill(text = tag, color = if (isClean) palette.passEmerald else palette.warnRose)
            }
        }

        if (job.trainerFeedback.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"${job.trainerFeedback}\"",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textPrimary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_job_${job.id}")) {
                Icon(Icons.Filled.Edit, contentDescription = AppStrings.edit.get(lang), tint = palette.arcCyan, modifier = Modifier.size(19.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_job_${job.id}")) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = AppStrings.delete.get(lang), tint = palette.warnRose, modifier = Modifier.size(19.dp))
            }
        }
    }
}
