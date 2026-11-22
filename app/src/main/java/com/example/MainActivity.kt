package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppThemeMode
import com.example.data.local.WeldJobEntity
import com.example.i18n.AppStrings
import com.example.ui.screens.AiMentorScreen
import com.example.ui.screens.CurrencyConverterScreen
import com.example.ui.screens.GuidesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LogbookFormSheet
import com.example.ui.screens.LogbookScreen
import com.example.ui.screens.SettingsSheet
import com.example.ui.screens.SummaryShareDialog
import com.example.ui.screens.VivaQuizScreen
import com.example.ui.theme.JrsWeldMentorTheme
import com.example.ui.theme.WeldGlassBackground
import com.example.ui.theme.WeldTheme
import com.example.ui.viewmodel.WeldMentorViewModel

private enum class MainTab(val tag: String, val icon: ImageVector) {
    HOME("nav_home", Icons.Filled.Home),
    LOGBOOK("nav_logbook", Icons.Filled.Build),
    VIVA("nav_viva", Icons.Filled.Quiz),
    GUIDES("nav_guides", Icons.Filled.MenuBook),
    CURRENCY("nav_currency", Icons.Filled.CurrencyExchange),
    MENTOR("nav_mentor", Icons.Filled.AutoAwesome)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeldMentorAppRoot(
                onThemeUpdated = { isLight ->
                    enableEdgeToEdge(
                        statusBarStyle = if (isLight) {
                            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                        },
                        navigationBarStyle = if (isLight) {
                            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun WeldMentorAppRoot(
    viewModel: WeldMentorViewModel = viewModel(),
    onThemeUpdated: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = uiState.preferences
    val lang = prefs.language
    val themeMode = prefs.themeMode

    LaunchedEffect(themeMode) {
        onThemeUpdated(themeMode == AppThemeMode.LIGHT)
    }

    JrsWeldMentorTheme(themeMode = themeMode) {
        val palette = WeldTheme.colors

        var currentTab by remember { mutableStateOf(MainTab.HOME) }
        var guidesSubTab by remember { mutableIntStateOf(0) }
        var vivaSubMode by remember { mutableIntStateOf(0) }
        var editingJob by remember { mutableStateOf<WeldJobEntity?>(null) }
        var showLogForm by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showSummaryCard by remember { mutableStateOf(false) }

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(uiState.statusMessage) {
            val msg = uiState.statusMessage
            if (!msg.isNullOrBlank()) {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearStatusMessage()
            }
        }

        WeldGlassBackground {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 700.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = palette.surface.copy(alpha = 0.94f),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.weight(0.2f))
                            MainTab.entries.forEach { tab ->
                                val label = tabLabel(tab, lang)
                                NavigationRailItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = label) },
                                    label = { Text(label, maxLines = 1) },
                                    modifier = Modifier.testTag(tab.tag),
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = palette.arcCyan,
                                        selectedTextColor = palette.arcCyan,
                                        indicatorColor = palette.arcCyan.copy(alpha = 0.16f),
                                        unselectedIconColor = palette.textSecondary,
                                        unselectedTextColor = palette.textSecondary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            BrandGlassTopBar(
                                langLabel = lang.displayLabel,
                                isOnline = uiState.isOnline,
                                isLightTheme = themeMode == AppThemeMode.LIGHT,
                                subtitle = AppStrings.builtBy.get(lang),
                                onToggleLang = { viewModel.toggleLanguage() },
                                onToggleTheme = { viewModel.toggleThemeMode() },
                                onOpenSettings = { showSettings = true }
                            )
                        },
                        bottomBar = {
                            if (!useNavigationRail) {
                                Surface(
                                    modifier = if (palette.isLight) Modifier.shadow(12.dp) else Modifier,
                                    color = palette.surface.copy(alpha = 0.96f),
                                    border = BorderStroke(1.dp, palette.glassBorder)
                                ) {
                                    NavigationBar(
                                        containerColor = Color.Transparent,
                                        tonalElevation = 0.dp
                                    ) {
                                        MainTab.entries.forEach { tab ->
                                            val label = tabLabel(tab, lang)
                                            NavigationBarItem(
                                                selected = currentTab == tab,
                                                onClick = { currentTab = tab },
                                                icon = { Icon(tab.icon, contentDescription = label) },
                                                label = {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        softWrap = false,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
                                                modifier = Modifier.testTag(tab.tag),
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = palette.arcCyan,
                                                    selectedTextColor = palette.arcCyan,
                                                    indicatorColor = palette.arcCyan.copy(alpha = 0.16f),
                                                    unselectedIconColor = palette.textSecondary,
                                                    unselectedTextColor = palette.textSecondary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                                },
                                label = "mainTabTransition",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = 760.dp)
                            ) { activeTab ->
                                when (activeTab) {
                                    MainTab.HOME -> HomeScreen(
                                        uiState = uiState,
                                        onOpenNewLog = {
                                            editingJob = null
                                            showLogForm = true
                                        },
                                        onOpenSummaryCard = { showSummaryCard = true },
                                        onOpenPlanners = {
                                            guidesSubTab = 3
                                            currentTab = MainTab.GUIDES
                                        },
                                        onOpenCurrency = { currentTab = MainTab.CURRENCY },
                                        onOpenVivaDelegator = {
                                            vivaSubMode = 1
                                            currentTab = MainTab.VIVA
                                        },
                                        onAddPracticeMinutes = { viewModel.addPracticeMinutes(it) },
                                        onAddTask = { en, bn, cat -> viewModel.addDailyTask(en, bn, cat) },
                                        onToggleTask = { id, done -> viewModel.toggleDailyTask(id, done) },
                                        onDeleteTask = { id -> viewModel.deleteDailyTask(id) }
                                    )

                                    MainTab.LOGBOOK -> LogbookScreen(
                                        uiState = uiState,
                                        onUpdateFilter = { viewModel.updateLogFilter(it) },
                                        onOpenNewForm = {
                                            editingJob = null
                                            showLogForm = true
                                        },
                                        onEditJob = { job ->
                                            editingJob = job
                                            showLogForm = true
                                        },
                                        onDeleteJob = { id -> viewModel.deleteJob(id) }
                                    )

                                    MainTab.VIVA -> VivaQuizScreen(
                                        uiState = uiState,
                                        selectedSubMode = vivaSubMode,
                                        onSelectSubMode = { vivaSubMode = it },
                                        onRecordAttempt = { qId, tEn, tBn, correct ->
                                            viewModel.recordQuizAttempt(qId, tEn, tBn, correct)
                                        },
                                        onResetStats = { viewModel.resetQuizStats() },
                                        onToggleMasteredCard = { cId, m ->
                                            viewModel.toggleMasteredFlashcard(cId, m)
                                        },
                                        onSubmitDelegateAnswer = { q, ans ->
                                            viewModel.submitDelegateAnswer(q, ans)
                                        },
                                        onNextDelegateQuestion = { total ->
                                            viewModel.nextDelegateQuestion(total)
                                        },
                                        onShuffleDelegateQuestions = { total ->
                                            viewModel.shuffleDelegateQuestions(total)
                                        },
                                        onResetDelegateFeedback = {
                                            viewModel.resetDelegateFeedback()
                                        }
                                    )

                                    MainTab.GUIDES -> GuidesScreen(
                                        uiState = uiState,
                                        selectedSubTab = guidesSubTab,
                                        onSelectSubTab = { guidesSubTab = it },
                                        onToggleChecklist = { id, grp, checked ->
                                            viewModel.toggleChecklist(id, grp, checked)
                                        }
                                    )

                                    MainTab.CURRENCY -> CurrencyConverterScreen(
                                        uiState = uiState,
                                        onRefreshLiveRates = { viewModel.refreshLiveRates() },
                                        onSetCustomRate = { from, to, rate ->
                                            viewModel.setCustomRate(from, to, rate)
                                        }
                                    )

                                    MainTab.MENTOR -> AiMentorScreen(
                                        uiState = uiState,
                                        onSendMessage = { viewModel.sendAiMessage(it) },
                                        onClearChat = { viewModel.clearAiChat() },
                                        onToggleAutoSpeak = { enabled ->
                                            viewModel.setVoiceSettings(autoSpeakAi = enabled)
                                        },
                                        onSubmitDelegateAnswer = { q, ans ->
                                            viewModel.submitDelegateAnswer(q, ans)
                                        },
                                        onNextDelegateQuestion = { total ->
                                            viewModel.nextDelegateQuestion(total)
                                        },
                                        onShuffleDelegateQuestions = { total ->
                                            viewModel.shuffleDelegateQuestions(total)
                                        },
                                        onResetDelegateFeedback = {
                                            viewModel.resetDelegateFeedback()
                                        },
                                        onOpenVivaCards = {
                                            vivaSubMode = 2
                                            currentTab = MainTab.VIVA
                                        },
                                        onOpenSettings = { showSettings = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showLogForm) {
            LogbookFormSheet(
                existingJob = editingJob,
                lang = lang,
                onDismiss = { showLogForm = false },
                onSave = { job ->
                    viewModel.saveJob(job)
                    showLogForm = false
                }
            )
        }

        if (showSettings) {
            SettingsSheet(
                uiState = uiState,
                onSetLanguage = { viewModel.setLanguage(it) },
                onSetThemeMode = { viewModel.setThemeMode(it) },
                onSetVoiceSettings = { autoSpeak, bargeIn, speed ->
                    viewModel.setVoiceSettings(autoSpeak, bargeIn, speed)
                },
                onSaveGeminiConfig = { key -> viewModel.saveGeminiConfig(key) },
                onSaveProfile = { name, trade -> viewModel.saveProfile(name, trade) },
                onExportJson = { viewModel.buildJsonBackup() },
                onExportCsv = { viewModel.buildCsvBackup() },
                onImportContent = { viewModel.importBackup(it) },
                onShowMessage = { viewModel.showStatusMessage(it) },
                onDismiss = { showSettings = false }
            )
        }

        if (showSummaryCard) {
            SummaryShareDialog(
                uiState = uiState,
                onShowMessage = { viewModel.showStatusMessage(it) },
                onDismiss = { showSummaryCard = false }
            )
        }
    }
}

@Composable
private fun BrandGlassTopBar(
    langLabel: String,
    isOnline: Boolean,
    isLightTheme: Boolean,
    subtitle: String,
    onToggleLang: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val palette = WeldTheme.colors
    Surface(
        modifier = if (palette.isLight) Modifier.shadow(6.dp) else Modifier,
        color = palette.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, palette.glassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_weldmentor_symbol),
                contentDescription = AppStrings.appName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = AppStrings.appName,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        maxLines = 1
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = (if (isOnline) palette.passEmerald else palette.moltenAmber).copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = if (isOnline) "ONLINE" else "OFFLINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOnline) palette.passEmerald else palette.moltenAmber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.testTag("top_theme_toggle_btn")
            ) {
                Icon(
                    imageVector = if (isLightTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    contentDescription = "Toggle theme",
                    tint = palette.moltenAmber
                )
            }

            Surface(
                onClick = onToggleLang,
                modifier = Modifier.testTag("top_lang_toggle_btn"),
                shape = RoundedCornerShape(50),
                color = palette.arcCyan.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, palette.arcCyan.copy(alpha = 0.42f))
            ) {
                Text(
                    text = if (langLabel == "EN") "EN / বাংলা" else "বাংলা / EN",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.arcCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("top_settings_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = palette.textPrimary
                )
            }
        }
    }
}

private fun tabLabel(tab: MainTab, lang: com.example.i18n.AppLanguage): String = when (tab) {
    MainTab.HOME -> AppStrings.navHome.get(lang)
    MainTab.LOGBOOK -> AppStrings.navLogbook.get(lang)
    MainTab.VIVA -> AppStrings.navViva.get(lang)
    MainTab.GUIDES -> AppStrings.navGuides.get(lang)
    MainTab.CURRENCY -> AppStrings.navCurrency.get(lang)
    MainTab.MENTOR -> AppStrings.navMentor.get(lang)
}
