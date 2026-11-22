package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.Home
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
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JrsWeldMentorTheme
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.PassEmerald
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WeldGlassBackground
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
            JrsWeldMentorTheme {
                WeldMentorAppRoot()
            }
        }
    }
}

@Composable
fun WeldMentorAppRoot(viewModel: WeldMentorViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lang = uiState.preferences.language

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
                        containerColor = SlateSurface.copy(alpha = 0.92f),
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
                                    selectedIconColor = ArcCyan,
                                    selectedTextColor = ArcCyan,
                                    indicatorColor = ArcCyan.copy(alpha = 0.16f)
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
                            subtitle = AppStrings.builtBy.get(lang),
                            onToggleLang = { viewModel.toggleLanguage() },
                            onOpenSettings = { showSettings = true }
                        )
                    },
                    bottomBar = {
                        if (!useNavigationRail) {
                            Surface(
                                color = SlateSurface.copy(alpha = 0.94f),
                                border = BorderStroke(1.dp, GlassBorder)
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
                                            selectedIconColor = ArcCyan,
                                            selectedTextColor = ArcCyan,
                                            indicatorColor = ArcCyan.copy(alpha = 0.16f),
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 760.dp)
                        ) {
                            when (currentTab) {
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

@Composable
private fun BrandGlassTopBar(
    langLabel: String,
    isOnline: Boolean,
    subtitle: String,
    onToggleLang: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        color = SlateSurface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, GlassBorder)
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
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = (if (isOnline) PassEmerald else MoltenAmber).copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = if (isOnline) "ONLINE" else "OFFLINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOnline) PassEmerald else MoltenAmber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                onClick = onToggleLang,
                modifier = Modifier.testTag("top_lang_toggle_btn"),
                shape = RoundedCornerShape(50),
                color = ArcCyan.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.45f))
            ) {
                Text(
                    text = if (langLabel == "EN") "EN / বাংলা" else "বাংলা / EN",
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcCyan,
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
                    tint = TextPrimary
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
