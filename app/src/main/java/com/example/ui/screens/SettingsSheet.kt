package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.data.local.DEFAULT_GEMINI_MODEL
import com.example.data.remote.ApiKeyStatus
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.DeepSlateBg
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassSectionHeader
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.PassEmerald
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarnRose
import com.example.ui.viewmodel.WeldUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    uiState: WeldUiState,
    onSetLanguage: (AppLanguage) -> Unit,
    onSaveGeminiConfig: (String) -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onExportJson: suspend () -> String,
    onExportCsv: suspend () -> String,
    onImportContent: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val prefs = uiState.preferences
    val lang = prefs.language

    var apiKeyInput by remember(prefs.geminiApiKey) { mutableStateOf(prefs.geminiApiKey) }
    var showKey by remember { mutableStateOf(false) }

    var traineeName by remember(prefs.traineeName) { mutableStateOf(prefs.traineeName) }
    var targetTrade by remember(prefs.targetTrade) { mutableStateOf(prefs.targetTrade) }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = onExportJson()
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                onShowMessage("JSON backup saved.")
            }
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val csv = onExportCsv()
                context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                onShowMessage("CSV job log exported.")
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
        containerColor = DeepSlateBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface),
                title = { Text(AppStrings.settingsTitle.get(lang), style = MaterialTheme.typography.titleMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_btn")) {
                        Icon(Icons.Filled.Close, contentDescription = AppStrings.cancel.get(lang), tint = TextPrimary)
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
            // Language Switch
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(
                    title = AppStrings.languageSection.get(lang),
                    icon = Icons.Filled.Language,
                    iconTint = ArcCyan
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
                                selectedContainerColor = ArcCyan.copy(alpha = 0.24f),
                                selectedLabelColor = ArcCyan
                            )
                        )
                    }
                }
            }

            // BYOK Gemini API Key
            GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = MoltenAmber) {
                GlassSectionHeader(
                    title = AppStrings.geminiKeySection.get(lang),
                    subtitle = AppStrings.aiStrictScopeBadge.get(lang),
                    icon = Icons.Filled.Key,
                    iconTint = MoltenAmber
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text(AppStrings.geminiKeyHint.get(lang), color = TextMuted) },
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                imageVector = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle key visibility",
                                tint = TextSecondary
                            )
                        }
                    },
                    singleLine = true,
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
                        TextSecondary
                    )
                    ApiKeyStatus.Validating -> Triple(
                        AppStrings.statusValidating.get(lang),
                        "${AppStrings.geminiModelLabel.get(lang)}: $DEFAULT_GEMINI_MODEL",
                        ArcCyan
                    )
                    is ApiKeyStatus.Active -> Triple(
                        "${AppStrings.statusActive.get(lang)} • ${keyStatus.modelId}",
                        keyStatus.detail,
                        PassEmerald
                    )
                    is ApiKeyStatus.QuotaLimited -> Triple(
                        "${AppStrings.statusQuotaLimited.get(lang)} • ${keyStatus.modelId}",
                        keyStatus.detail,
                        MoltenAmber
                    )
                    is ApiKeyStatus.Invalid -> Triple(
                        AppStrings.statusInvalid.get(lang),
                        keyStatus.detail,
                        WarnRose
                    )
                    is ApiKeyStatus.Offline -> Triple(
                        "${AppStrings.statusOfflineSaved.get(lang)} • ${keyStatus.modelId}",
                        AppStrings.keyAutoDetectNote.get(lang),
                        MoltenAmber
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
                                    color = ArcCyan
                                )
                            }
                            ArcStatusPill(text = statusLabel, color = statusColor)
                        }
                        Text(
                            text = statusDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            onSaveGeminiConfig(apiKeyInput)
                        },
                        modifier = Modifier.testTag("save_api_key_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = Color(0xFF04131E))
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
                        Text(AppStrings.clearKey.get(lang), color = TextSecondary)
                    }
                }
            }

            // Trainee Profile
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(
                    title = AppStrings.profileSection.get(lang),
                    icon = Icons.Filled.Person,
                    iconTint = PassEmerald
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = traineeName,
                    onValueChange = { traineeName = it },
                    label = { Text(AppStrings.traineeNameLabel.get(lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_trainee_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetTrade,
                    onValueChange = { targetTrade = it },
                    label = { Text(AppStrings.targetTradeLabel.get(lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        onSaveProfile(traineeName, targetTrade)
                        onShowMessage("Profile updated.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PassEmerald, contentColor = Color(0xFF022015))
                ) {
                    Text(AppStrings.save.get(lang))
                }
            }

            // Backup & Restore (JSON / CSV)
            GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = ArcCyan) {
                GlassSectionHeader(
                    title = AppStrings.backupSection.get(lang),
                    icon = Icons.Filled.Backup,
                    iconTint = ArcCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { exportJsonLauncher.launch("jrs_weldmentor_backup.json") },
                        modifier = Modifier.weight(1f).testTag("export_json_btn"),
                        border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.exportJsonBtn.get(lang), style = MaterialTheme.typography.labelMedium, color = ArcCyan)
                    }
                    OutlinedButton(
                        onClick = { exportCsvLauncher.launch("jrs_weldmentor_logs.csv") },
                        modifier = Modifier.weight(1f).testTag("export_csv_btn"),
                        border = BorderStroke(1.dp, PassEmerald.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = PassEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.exportCsvBtn.get(lang), style = MaterialTheme.typography.labelMedium, color = PassEmerald)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json", "text/csv", "text/comma-separated-values", "*/*")) },
                    modifier = Modifier.fillMaxWidth().testTag("import_backup_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = Color(0xFF04131E))
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.importBackupBtn.get(lang))
                }
            }

            // Developer & Brand Credit
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ArcCyan.copy(alpha = 0.4f)
            ) {
                Text("JRS WeldMentor", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                ArcStatusPill(
                    text = "Developed by ${AppStrings.devName} | ${AppStrings.devWebsite}",
                    color = ArcCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppStrings.devWebsiteUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.testTag("open_dev_website_btn"),
                    border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Filled.OpenInBrowser, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.visitWebsite.get(lang), color = ArcCyan)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
