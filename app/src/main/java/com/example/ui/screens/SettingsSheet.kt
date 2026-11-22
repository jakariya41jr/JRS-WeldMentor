package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.audio.VoiceAssistantController
import com.example.data.local.AppThemeMode
import com.example.data.local.DEFAULT_GEMINI_MODEL
import com.example.data.local.SpeechSpeedPreset
import com.example.data.remote.ApiKeyStatus
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassSectionHeader
import com.example.ui.theme.WeldTheme
import com.example.ui.viewmodel.WeldUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    uiState: WeldUiState,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetVoiceSettings: (Boolean?, Boolean?, SpeechSpeedPreset?) -> Unit,
    onSaveGeminiConfig: (String) -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onExportJson: suspend () -> String,
    onImportContent: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val palette = WeldTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val prefs = uiState.preferences
    val lang = prefs.language

    var apiKeyInput by remember(prefs.geminiApiKey) { mutableStateOf(prefs.geminiApiKey) }
    var showKey by remember { mutableStateOf(false) }

    var traineeName by remember(prefs.traineeName) { mutableStateOf(prefs.traineeName) }
    var targetTrade by remember(prefs.targetTrade) { mutableStateOf(prefs.targetTrade) }

    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
        onShowMessage(
            if (granted) AppStrings.permGranted.get(lang) else AppStrings.voiceMicPermNeeded.get(lang)
        )
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = onExportJson()
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                onShowMessage("Study progress JSON backup saved.")
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    onImportContent(content)
                }
            }
        }
    }

    Scaffold(
        containerColor = palette.bg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = palette.surface),
                title = {
                    Text(
                        AppStrings.settingsTitle.get(lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_btn")) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = AppStrings.cancel.get(lang),
                            tint = palette.textPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Appearance & Language Section
            GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = palette.arcCyan) {
                GlassSectionHeader(
                    title = AppStrings.appearanceSection.get(lang),
                    icon = if (prefs.themeMode == AppThemeMode.LIGHT) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    iconTint = palette.arcCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = prefs.themeMode == AppThemeMode.DARK,
                        onClick = { onSetThemeMode(AppThemeMode.DARK) },
                        label = { Text(AppStrings.themeDarkGlass.get(lang)) },
                        leadingIcon = { Icon(Icons.Filled.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("settings_theme_dark"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.arcCyan.copy(alpha = 0.22f),
                            selectedLabelColor = palette.arcCyan
                        )
                    )
                    FilterChip(
                        selected = prefs.themeMode == AppThemeMode.LIGHT,
                        onClick = { onSetThemeMode(AppThemeMode.LIGHT) },
                        label = { Text(AppStrings.themeLightGlass.get(lang)) },
                        leadingIcon = { Icon(Icons.Filled.LightMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("settings_theme_light"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.arcCyan.copy(alpha = 0.22f),
                            selectedLabelColor = palette.arcCyan
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = palette.glassBorder)

                GlassSectionHeader(
                    title = AppStrings.languageSection.get(lang),
                    icon = Icons.Filled.Language,
                    iconTint = palette.arcCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppLanguage.entries.forEach { l ->
                        FilterChip(
                            selected = lang == l,
                            onClick = { onSetLanguage(l) },
                            label = { Text(if (l == AppLanguage.EN) "English (EN)" else "বাংলা (Bangla)") },
                            modifier = Modifier.testTag("settings_lang_${l.code}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.arcCyan.copy(alpha = 0.22f),
                                selectedLabelColor = palette.arcCyan
                            )
                        )
                    }
                }
            }

            // 2. Intelligence & Live Model Section (BYOK + Auto-Detection)
            GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = palette.moltenAmber) {
                GlassSectionHeader(
                    title = AppStrings.intelligenceSection.get(lang),
                    subtitle = AppStrings.aiStrictScopeBadge.get(lang),
                    icon = Icons.Filled.AutoAwesome,
                    iconTint = palette.moltenAmber
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text(AppStrings.geminiKeyHint.get(lang), color = palette.textMuted) },
                    leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null, tint = palette.moltenAmber) },
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                imageVector = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle key visibility",
                                tint = palette.textSecondary
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_gemini_key_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                val keyStatus = uiState.apiKeyStatus
                val (statusLabel, statusDetail, statusColor) = when (keyStatus) {
                    ApiKeyStatus.Missing -> Triple(
                        AppStrings.statusMissing.get(lang),
                        AppStrings.keyAutoDetectNote.get(lang),
                        palette.textSecondary
                    )
                    ApiKeyStatus.Validating -> Triple(
                        AppStrings.statusValidating.get(lang),
                        "${AppStrings.geminiModelLabel.get(lang)}: $DEFAULT_GEMINI_MODEL",
                        palette.arcCyan
                    )
                    is ApiKeyStatus.Active -> Triple(
                        "${AppStrings.statusActive.get(lang)} • ${keyStatus.modelId}",
                        keyStatus.detail,
                        palette.passEmerald
                    )
                    is ApiKeyStatus.QuotaLimited -> Triple(
                        "${AppStrings.statusQuotaLimited.get(lang)} • ${keyStatus.modelId}",
                        keyStatus.detail,
                        palette.moltenAmber
                    )
                    is ApiKeyStatus.Invalid -> Triple(
                        AppStrings.statusInvalid.get(lang),
                        keyStatus.detail,
                        palette.warnRose
                    )
                    is ApiKeyStatus.Offline -> Triple(
                        "${AppStrings.statusOfflineSaved.get(lang)} • ${keyStatus.modelId}",
                        AppStrings.keyAutoDetectNote.get(lang),
                        palette.moltenAmber
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_api_key_status_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (keyStatus is ApiKeyStatus.Validating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = palette.arcCyan
                                )
                            }
                            ArcStatusPill(text = statusLabel, color = statusColor)
                        }
                        Text(
                            text = statusDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onSaveGeminiConfig(apiKeyInput) },
                        modifier = Modifier.testTag("save_api_key_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(AppStrings.keySaveActivateBtn.get(lang))
                    }
                    OutlinedButton(
                        onClick = {
                            apiKeyInput = ""
                            onSaveGeminiConfig("")
                        },
                        modifier = Modifier.testTag("clear_api_key_btn")
                    ) {
                        Text(AppStrings.clearKey.get(lang), color = palette.textSecondary)
                    }
                }
            }

            // 3. Voice, Barge-In & Permissions Section
            GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = palette.violetSpark) {
                GlassSectionHeader(
                    title = AppStrings.voiceControlsSection.get(lang),
                    icon = Icons.Filled.GraphicEq,
                    iconTint = palette.violetSpark
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Auto-Speak toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(AppStrings.voiceAutoSpeak.get(lang), style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Text(AppStrings.autoSpeakDesc.get(lang), style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Switch(
                        checked = prefs.autoSpeakAi,
                        onCheckedChange = { onSetVoiceSettings(it, null, null) },
                        modifier = Modifier.testTag("settings_auto_speak_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.passEmerald,
                            checkedTrackColor = palette.passEmerald.copy(alpha = 0.35f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tap-to-Interrupt / Barge-In toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(AppStrings.bargeInTitle.get(lang), style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Text(AppStrings.bargeInDesc.get(lang), style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Switch(
                        checked = prefs.bargeInEnabled,
                        onCheckedChange = { onSetVoiceSettings(null, it, null) },
                        modifier = Modifier.testTag("settings_barge_in_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.arcCyan,
                            checkedTrackColor = palette.arcCyan.copy(alpha = 0.35f)
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = palette.glassBorder)

                Text(AppStrings.voiceCadenceLabel.get(lang), style = MaterialTheme.typography.labelLarge, color = palette.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf(
                        SpeechSpeedPreset.RELAXED to AppStrings.speedRelaxed.get(lang),
                        SpeechSpeedPreset.NATURAL to AppStrings.speedNatural.get(lang),
                        SpeechSpeedPreset.CRISP to AppStrings.speedCrisp.get(lang)
                    )
                    presets.forEach { (preset, label) ->
                        FilterChip(
                            selected = prefs.speechSpeedPreset == preset,
                            onClick = { onSetVoiceSettings(null, null, preset) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("settings_speed_${preset.code}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.violetSpark.copy(alpha = 0.22f),
                                selectedLabelColor = palette.violetSpark
                            )
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = palette.glassBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Mic, contentDescription = null, tint = palette.arcCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppStrings.micPermissionLabel.get(lang), style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                    }
                    if (micGranted) {
                        ArcStatusPill(
                            text = AppStrings.permGranted.get(lang),
                            color = palette.passEmerald,
                            icon = Icons.Filled.CheckCircle
                        )
                    } else {
                        OutlinedButton(
                            onClick = { micPermLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            modifier = Modifier.testTag("settings_grant_mic_btn"),
                            border = BorderStroke(1.dp, palette.moltenAmber.copy(alpha = 0.6f))
                        ) {
                            Text(AppStrings.permGrantBtn.get(lang), color = palette.moltenAmber)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(AppStrings.ttsStatusLabel.get(lang), style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                    ArcStatusPill(
                        text = AppStrings.ttsEngineReady.get(lang),
                        color = palette.arcCyan,
                        icon = Icons.Filled.Verified
                    )
                }
            }

            // 4. Trainee Profile
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(
                    title = AppStrings.profileSection.get(lang),
                    icon = Icons.Filled.Person,
                    iconTint = palette.passEmerald
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = traineeName,
                    onValueChange = { traineeName = it },
                    label = { Text(AppStrings.traineeNameLabel.get(lang)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("settings_trainee_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetTrade,
                    onValueChange = { targetTrade = it },
                    label = { Text(AppStrings.targetTradeLabel.get(lang)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        onSaveProfile(traineeName, targetTrade)
                        onShowMessage("Profile updated.")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(AppStrings.save.get(lang))
                }
            }

            // 5. Study Progress Backup & Restore (JSON)
            GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = palette.arcCyan) {
                GlassSectionHeader(
                    title = AppStrings.backupSection.get(lang),
                    icon = Icons.Filled.Backup,
                    iconTint = palette.arcCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { exportJsonLauncher.launch("jrs_weldmentor_study_backup.json") },
                        modifier = Modifier.weight(1f).testTag("export_json_btn"),
                        border = BorderStroke(1.dp, palette.arcCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = palette.arcCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.exportJsonBtn.get(lang), style = MaterialTheme.typography.labelMedium, color = palette.arcCyan)
                    }
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.weight(1f).testTag("import_backup_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.importBackupBtn.get(lang), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // 6. Developer & Ownership Credit (Sole attribution: Md Jakariya Hasan)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = palette.arcCyan.copy(alpha = 0.4f)
            ) {
                Text(AppStrings.aboutDevSection.get(lang), style = MaterialTheme.typography.labelLarge, color = palette.textSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("JRS WeldMentor", style = MaterialTheme.typography.headlineSmall, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                ArcStatusPill(
                    text = "Developed by ${AppStrings.devName} | ${AppStrings.devWebsite}",
                    color = palette.arcCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppStrings.devWebsiteUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.testTag("open_dev_website_btn"),
                    border = BorderStroke(1.dp, palette.arcCyan.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Filled.OpenInBrowser, contentDescription = null, tint = palette.arcCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.visitWebsite.get(lang), color = palette.arcCyan)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
