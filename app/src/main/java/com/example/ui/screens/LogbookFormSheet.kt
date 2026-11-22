package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.PreferencesManager
import com.example.data.local.WeldJobEntity
import com.example.data.model.BaseMaterial
import com.example.data.model.DefectTag
import com.example.data.model.JobStatus
import com.example.data.model.JointType
import com.example.data.model.WeldPosition
import com.example.data.model.WeldProcess
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.GlassCard
import com.example.ui.theme.WeldTheme

private val PRESET_RODS = listOf(
    "E6013 3.2mm",
    "E7018 2.5mm",
    "E7018 3.2mm",
    "LB52 2.5mm",
    "LB52 3.2mm",
    "ER70S-6 2.4mm",
    "316L 2.4mm"
)

private val MEASURING_TOOLS = listOf(
    "Angle Locator",
    "Vernier Caliper",
    "Micrometer",
    "Digital Angle Gauge",
    "Laser Level",
    "Tape Measure",
    "Fillet Weld Gauge"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogbookFormSheet(
    existingJob: WeldJobEntity?,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (WeldJobEntity) -> Unit
) {
    BackHandler(onBack = onDismiss)
    val colors = WeldTheme.colors

    var date by remember { mutableStateOf(existingJob?.date ?: PreferencesManager.todayString()) }
    var process by remember { mutableStateOf(existingJob?.process ?: WeldProcess.SMAW.label) }
    var position by remember { mutableStateOf(existingJob?.position ?: WeldPosition.G6.code) }
    var material by remember { mutableStateOf(existingJob?.baseMaterial ?: BaseMaterial.CS.code) }
    var jointType by remember { mutableStateOf(existingJob?.jointType ?: JointType.BUTT.code) }
    var rodFiller by remember { mutableStateOf(existingJob?.rodFillerAndSize ?: "LB52 2.5mm") }

    var tigCup by remember { mutableStateOf(existingJob?.tigCup ?: "#6") }
    var tigColletBody by remember { mutableStateOf(existingJob?.tigColletBody ?: "Gas Lens 2.4mm") }
    var tigBackCap by remember { mutableStateOf(existingJob?.tigBackCap ?: "Medium") }
    var tigTungsten by remember { mutableStateOf(existingJob?.tigTungsten ?: "2% Lanthanated 2.4mm") }

    var amperage by remember { mutableStateOf(existingJob?.amperage ?: "90A") }
    var voltage by remember { mutableStateOf(existingJob?.voltage ?: "") }
    var gasFlow by remember { mutableStateOf(existingJob?.gasFlow ?: "12 L/min") }

    var selectedTools by remember {
        mutableStateOf(
            existingJob?.measuringTools?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.toSet()
                ?: setOf("Angle Locator", "Vernier Caliper")
        )
    }
    var ppeChecked by remember { mutableStateOf(existingJob?.ppeChecked ?: true) }
    var selectedDefects by remember {
        mutableStateOf(
            existingJob?.defectTags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.toSet()
                ?: setOf(DefectTag.CLEAN_PASS.code)
        )
    }
    var trainerFeedback by remember { mutableStateOf(existingJob?.trainerFeedback ?: "") }
    var status by remember { mutableStateOf(existingJob?.status ?: JobStatus.PASS.code) }

    val showTigSection = process.contains("TIG", ignoreCase = true) || process.contains("GTAW", ignoreCase = true) ||
        position.contains("GTAW", ignoreCase = true)

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
                title = {
                    Text(
                        text = if (existingJob == null) AppStrings.createLog.get(lang) else AppStrings.editLog.get(lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_log_form_btn")) {
                        Icon(Icons.Filled.Close, contentDescription = AppStrings.cancel.get(lang), tint = colors.textPrimary)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onSave(
                                WeldJobEntity(
                                    id = existingJob?.id ?: 0L,
                                    date = date.trim().ifBlank { PreferencesManager.todayString() },
                                    process = process,
                                    position = position,
                                    baseMaterial = material,
                                    jointType = jointType,
                                    rodFillerAndSize = rodFiller.trim().ifBlank { "E7018 3.2mm" },
                                    tigCup = if (showTigSection) tigCup.trim() else "",
                                    tigColletBody = if (showTigSection) tigColletBody.trim() else "",
                                    tigBackCap = if (showTigSection) tigBackCap.trim() else "",
                                    tigTungsten = if (showTigSection) tigTungsten.trim() else "",
                                    amperage = amperage.trim(),
                                    voltage = voltage.trim(),
                                    gasFlow = gasFlow.trim(),
                                    measuringTools = selectedTools.joinToString(", "),
                                    ppeChecked = ppeChecked,
                                    defectTags = selectedDefects.ifEmpty { setOf(DefectTag.CLEAN_PASS.code) }.joinToString(", "),
                                    trainerFeedback = trainerFeedback.trim(),
                                    status = status,
                                    createdAt = existingJob?.createdAt ?: System.currentTimeMillis()
                                )
                            )
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_log_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.arcCyan,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(AppStrings.save.get(lang))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Date & Process
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(AppStrings.dateLabel.get(lang)) },
                    modifier = Modifier.fillMaxWidth().testTag("form_date_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(AppStrings.processLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    WeldProcess.entries.forEach { proc ->
                        FilterChip(
                            selected = process == proc.label,
                            onClick = { process = proc.label },
                            label = { Text(proc.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.arcCyan.copy(alpha = 0.2f),
                                selectedLabelColor = colors.arcCyan
                            )
                        )
                    }
                }
            }

            // Position, Material, Joint
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(AppStrings.positionLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    WeldPosition.entries.forEach { pos ->
                        FilterChip(
                            selected = position == pos.code,
                            onClick = { position = pos.code },
                            label = { Text(pos.code) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.moltenAmber.copy(alpha = 0.2f),
                                selectedLabelColor = colors.moltenAmber
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(AppStrings.materialLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BaseMaterial.entries.forEach { mat ->
                        FilterChip(
                            selected = material == mat.code,
                            onClick = { material = mat.code },
                            label = { Text(mat.bi.get(lang)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.arcCyan.copy(alpha = 0.2f),
                                selectedLabelColor = colors.arcCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(AppStrings.jointLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    JointType.entries.forEach { jt ->
                        FilterChip(
                            selected = jointType == jt.code,
                            onClick = { jointType = jt.code },
                            label = { Text(jt.code) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.arcCyan.copy(alpha = 0.2f),
                                selectedLabelColor = colors.arcCyan
                            )
                        )
                    }
                }
            }

            // Rod / Filler & Size
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(AppStrings.rodFillerLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PRESET_RODS.forEach { preset ->
                        FilterChip(
                            selected = rodFiller == preset,
                            onClick = { rodFiller = preset },
                            label = { Text(preset, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rodFiller,
                    onValueChange = { rodFiller = it },
                    placeholder = { Text(AppStrings.rodFillerHint.get(lang), color = colors.textMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("form_rod_input"),
                    singleLine = true
                )
            }

            // TIG Torch Setup (shown for TIG/GTAW)
            if (showTigSection) {
                GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = colors.arcCyan) {
                    Text(AppStrings.tigSetupLabel.get(lang), style = MaterialTheme.typography.titleSmall, color = colors.arcCyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tigCup,
                            onValueChange = { tigCup = it },
                            label = { Text(AppStrings.tigCupLabel.get(lang)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tigColletBody,
                            onValueChange = { tigColletBody = it },
                            label = { Text(AppStrings.tigColletLabel.get(lang)) },
                            modifier = Modifier.weight(1.4f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(AppStrings.tigBackCapLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Long", "Medium", "Short").forEach { cap ->
                            FilterChip(
                                selected = tigBackCap == cap,
                                onClick = { tigBackCap = cap },
                                label = { Text(cap) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = tigTungsten,
                        onValueChange = { tigTungsten = it },
                        label = { Text(AppStrings.tigTungstenLabel.get(lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Machine Settings
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(AppStrings.machineSettingsLabel.get(lang), style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amperage,
                        onValueChange = { amperage = it },
                        label = { Text(AppStrings.ampsLabel.get(lang)) },
                        modifier = Modifier.weight(1f).testTag("form_amps_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = voltage,
                        onValueChange = { voltage = it },
                        label = { Text(AppStrings.voltsLabel.get(lang)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gasFlow,
                    onValueChange = { gasFlow = it },
                    label = { Text(AppStrings.gasFlowLabel.get(lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Measuring Tools & PPE
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(AppStrings.measuringToolsLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MEASURING_TOOLS.forEach { tool ->
                        val active = selectedTools.contains(tool)
                        FilterChip(
                            selected = active,
                            onClick = {
                                selectedTools = if (active) selectedTools - tool else selectedTools + tool
                            },
                            label = { Text(tool, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(AppStrings.ppeChecklistLabel.get(lang), style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Switch(
                        checked = ppeChecked,
                        onCheckedChange = { ppeChecked = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.passEmerald,
                            checkedTrackColor = colors.passEmerald.copy(alpha = 0.35f)
                        )
                    )
                }
            }

            // Defect Tags, Feedback, Status
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(AppStrings.defectTagsLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DefectTag.entries.forEach { tag ->
                        val active = selectedDefects.contains(tag.code)
                        FilterChip(
                            selected = active,
                            onClick = {
                                selectedDefects = if (tag == DefectTag.CLEAN_PASS) {
                                    setOf(DefectTag.CLEAN_PASS.code)
                                } else {
                                    val withoutClean = selectedDefects - DefectTag.CLEAN_PASS.code
                                    if (active) withoutClean - tag.code else withoutClean + tag.code
                                }
                            },
                            label = { Text(tag.bi.get(lang), style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (tag == DefectTag.CLEAN_PASS) colors.passEmerald.copy(alpha = 0.2f) else colors.warnRose.copy(alpha = 0.2f),
                                selectedLabelColor = if (tag == DefectTag.CLEAN_PASS) colors.passEmerald else colors.warnRose
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = trainerFeedback,
                    onValueChange = { trainerFeedback = it },
                    label = { Text(AppStrings.trainerFeedbackLabel.get(lang)) },
                    modifier = Modifier.fillMaxWidth().testTag("form_feedback_input"),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(AppStrings.statusLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    JobStatus.entries.forEach { st ->
                        val isPass = st == JobStatus.PASS
                        FilterChip(
                            selected = status == st.code,
                            onClick = { status = st.code },
                            label = { Text(st.bi.get(lang)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = (if (isPass) colors.passEmerald else colors.moltenAmber).copy(alpha = 0.22f),
                                selectedLabelColor = if (isPass) colors.passEmerald else colors.moltenAmber
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
