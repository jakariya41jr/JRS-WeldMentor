package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.content.VivaKnowledgeBase
import com.example.data.local.ChecklistProgressEntity
import com.example.data.local.DailyTaskEntity
import com.example.data.local.DEFAULT_GEMINI_MODEL
import com.example.data.local.PreferencesManager
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.UserPreferences
import com.example.data.local.WeldJobEntity
import com.example.data.model.ChatMessage
import com.example.data.model.QuizQuestion
import com.example.data.remote.ApiKeyStatus
import com.example.data.remote.InvalidApiKeyException
import com.example.data.remote.QuotaExceededException
import com.example.data.repository.WeldRepository
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TopicStat(
    val topicEn: String,
    val topicBn: String,
    val total: Int,
    val correct: Int
) {
    val accuracyPct: Int get() = if (total == 0) 0 else (correct * 100) / total
}

data class LogbookFilter(
    val searchQuery: String = "",
    val processFilter: String = "ALL",
    val positionFilter: String = "ALL",
    val materialFilter: String = "ALL",
    val statusFilter: String = "ALL"
)

data class AiChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class VivaDelegateState(
    val currentIndex: Int = 0,
    val isEvaluating: Boolean = false,
    val lastPassed: Boolean? = null,
    val lastFeedback: String? = null,
    val evaluatedByAi: Boolean = false,
    val autoRecorded: Boolean = false,
    val completedQuestionIds: Set<String> = emptySet()
)

data class WeldDashboardMetrics(
    val totalJobs: Int = 0,
    val todayJobs: Int = 0,
    val passCount: Int = 0,
    val passRatePct: Int = 0,
    val uniquePositions: List<String> = emptyList(),
    val totalQuizAnswered: Int = 0,
    val todayQuizAnswered: Int = 0,
    val quizAccuracyPct: Int = 0,
    val weakTopics: List<TopicStat> = emptyList(),
    val topicStats: List<TopicStat> = emptyList(),
    val practicalProgress: Float = 0f,
    val vivaProgress: Float = 0f
)

data class WeldUiState(
    val preferences: UserPreferences = UserPreferences(),
    val jobs: List<WeldJobEntity> = emptyList(),
    val filteredJobs: List<WeldJobEntity> = emptyList(),
    val logFilter: LogbookFilter = LogbookFilter(),
    val tasks: List<DailyTaskEntity> = emptyList(),
    val checklistMap: Map<String, Boolean> = emptyMap(),
    val metrics: WeldDashboardMetrics = WeldDashboardMetrics(),
    val isOnline: Boolean = false,
    val isRefreshingRates: Boolean = false,
    val aiChat: AiChatState = AiChatState(),
    val vivaDelegate: VivaDelegateState = VivaDelegateState(),
    val apiKeyStatus: ApiKeyStatus = ApiKeyStatus.Missing,
    val statusMessage: String? = null
)

class WeldMentorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeldRepository(application.applicationContext)

    private val _logFilter = MutableStateFlow(LogbookFilter())
    private val _aiChat = MutableStateFlow(AiChatState())
    private val _vivaDelegate = MutableStateFlow(VivaDelegateState())
    private val _apiKeyStatus = MutableStateFlow<ApiKeyStatus>(ApiKeyStatus.Missing)
    private val _statusMessage = MutableStateFlow<String?>(null)
    private val _isRefreshingRates = MutableStateFlow(false)
    private var announceNextValidation = false

    init {
        viewModelScope.launch {
            repository.ensureInitialDailyTasks()
        }
        viewModelScope.launch {
            combine(
                repository.preferencesFlow.map { it.geminiApiKey.trim() }.distinctUntilChanged(),
                repository.isOnlineFlow.distinctUntilChanged()
            ) { key, online -> key to online }
                .collectLatest { (key, online) ->
                    runAutoKeyValidation(key, online)
                }
        }
    }

    private suspend fun runAutoKeyValidation(key: String, online: Boolean) {
        val shouldAnnounce = announceNextValidation
        announceNextValidation = false

        val lang = uiState.value.preferences.language
        if (key.isEmpty()) {
            _apiKeyStatus.value = ApiKeyStatus.Missing
            if (shouldAnnounce) {
                _statusMessage.value = if (lang == AppLanguage.BN) "API কী মুছে ফেলা হয়েছে।" else "API key cleared."
            }
            return
        }
        if (!online) {
            val savedModel = uiState.value.preferences.geminiModel.ifBlank { DEFAULT_GEMINI_MODEL }
            _apiKeyStatus.value = ApiKeyStatus.Offline(savedModel)
            if (shouldAnnounce) {
                _statusMessage.value = if (lang == AppLanguage.BN) {
                    "API কী সংরক্ষিত। অনলাইন হলেই স্বয়ংক্রিয়ভাবে চালু হবে।"
                } else {
                    "API key saved. Will auto-activate once online."
                }
            }
            return
        }
        _apiKeyStatus.value = ApiKeyStatus.Validating
        val status = repository.validateAndActivateGeminiKey(key)
        _apiKeyStatus.value = status
        if (shouldAnnounce) {
            _statusMessage.value = when (status) {
                is ApiKeyStatus.Active -> status.detail
                is ApiKeyStatus.QuotaLimited -> status.detail
                is ApiKeyStatus.Invalid -> status.detail
                is ApiKeyStatus.Offline -> if (lang == AppLanguage.BN) "অফলাইন — কী সংরক্ষিত" else "Offline — Key saved"
                else -> null
            }
        }
    }

    private data class CoreSnapshot(
        val prefs: UserPreferences,
        val jobs: List<WeldJobEntity>,
        val tasks: List<DailyTaskEntity>,
        val attempts: List<QuizAttemptEntity>,
        val checklists: List<ChecklistProgressEntity>
    )

    private val coreFlow = combine(
        repository.preferencesFlow,
        repository.jobsFlow,
        repository.tasksFlow,
        repository.quizAttemptsFlow,
        repository.checklistsFlow
    ) { prefs, jobs, tasks, attempts, checklists ->
        CoreSnapshot(prefs, jobs, tasks, attempts, checklists)
    }

    private data class UiExtras(
        val online: Boolean,
        val refreshingRates: Boolean,
        val chat: AiChatState,
        val delegate: VivaDelegateState,
        val keyStatus: ApiKeyStatus,
        val msg: String?
    )

    private val extrasFlow = combine(
        repository.isOnlineFlow,
        _isRefreshingRates,
        _aiChat,
        _vivaDelegate,
        _apiKeyStatus
    ) { online, refreshing, chat, delegate, keyStatus ->
        UiExtras(online, refreshing, chat, delegate, keyStatus, null)
    }.combine(_statusMessage) { extras, msg ->
        extras.copy(msg = msg)
    }

    val uiState: StateFlow<WeldUiState> = combine(
        coreFlow,
        _logFilter,
        extrasFlow
    ) { core, filter, extras ->
        val filtered = applyLogFilter(core.jobs, filter)
        val metrics = computeMetrics(core.jobs, core.attempts, core.prefs)
        val checkMap = core.checklists.associate { it.itemId to it.isChecked }
        val passedFromDb = core.attempts.filter { it.isCorrect }.map { it.questionId }.toSet()
        WeldUiState(
            preferences = core.prefs,
            jobs = core.jobs,
            filteredJobs = filtered,
            logFilter = filter,
            tasks = core.tasks,
            checklistMap = checkMap,
            metrics = metrics,
            isOnline = extras.online,
            isRefreshingRates = extras.refreshingRates,
            aiChat = extras.chat,
            vivaDelegate = extras.delegate.copy(
                completedQuestionIds = extras.delegate.completedQuestionIds + passedFromDb
            ),
            apiKeyStatus = extras.keyStatus,
            statusMessage = extras.msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeldUiState()
    )

    private fun applyLogFilter(jobs: List<WeldJobEntity>, filter: LogbookFilter): List<WeldJobEntity> {
        val q = filter.searchQuery.trim().lowercase()
        return jobs.filter { job ->
            val matchesQuery = q.isEmpty() || listOf(
                job.date, job.process, job.position, job.baseMaterial,
                job.jointType, job.rodFillerAndSize, job.measuringTools,
                job.defectTags, job.trainerFeedback, job.status
            ).any { it.lowercase().contains(q) }
            val matchesProcess = filter.processFilter == "ALL" || job.process == filter.processFilter
            val matchesPos = filter.positionFilter == "ALL" || job.position == filter.positionFilter
            val matchesMat = filter.materialFilter == "ALL" || job.baseMaterial == filter.materialFilter
            val matchesStatus = filter.statusFilter == "ALL" || job.status == filter.statusFilter
            matchesQuery && matchesProcess && matchesPos && matchesMat && matchesStatus
        }
    }

    private fun computeMetrics(
        jobs: List<WeldJobEntity>,
        attempts: List<QuizAttemptEntity>,
        prefs: UserPreferences
    ): WeldDashboardMetrics {
        val today = PreferencesManager.todayString()
        val todayStartCal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val totalJobs = jobs.size
        val todayJobs = jobs.count { it.date == today }
        val passCount = jobs.count { it.status == "Pass" }
        val passRate = if (totalJobs == 0) 0 else (passCount * 100) / totalJobs
        val positions = jobs.map { it.position }.distinct()

        val totalQuiz = attempts.size
        val todayQuiz = attempts.count { it.attemptedAt >= todayStartCal }
        val correctQuiz = attempts.count { it.isCorrect }
        val quizAcc = if (totalQuiz == 0) 0 else (correctQuiz * 100) / totalQuiz

        val topicStats = attempts.groupBy { it.topicEn }.map { (_, list) ->
            TopicStat(
                topicEn = list.first().topicEn,
                topicBn = list.first().topicBn,
                total = list.size,
                correct = list.count { it.isCorrect }
            )
        }.sortedBy { it.accuracyPct }

        val weakTopics = topicStats.filter { it.total >= 1 && it.accuracyPct < 75 }

        val practicalProgress = (positions.size.toFloat() / 6f).coerceIn(0f, 1f)
        val vivaProgress = if (totalQuiz == 0) 0f else {
            ((attempts.map { it.questionId }.distinct().size.toFloat() /
                VivaKnowledgeBase.quizQuestions.size.toFloat()) * 0.6f +
                (quizAcc / 100f) * 0.4f).coerceIn(0f, 1f)
        }

        return WeldDashboardMetrics(
            totalJobs = totalJobs,
            todayJobs = todayJobs,
            passCount = passCount,
            passRatePct = passRate,
            uniquePositions = positions,
            totalQuizAnswered = totalQuiz,
            todayQuizAnswered = todayQuiz,
            quizAccuracyPct = quizAcc,
            weakTopics = weakTopics,
            topicStats = topicStats,
            practicalProgress = practicalProgress,
            vivaProgress = vivaProgress
        )
    }

    fun toggleLanguage() {
        val current = uiState.value.preferences.language
        viewModelScope.launch {
            repository.setLanguage(current.toggled())
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun updateLogFilter(transform: (LogbookFilter) -> LogbookFilter) {
        _logFilter.update(transform)
    }

    fun saveJob(job: WeldJobEntity) {
        viewModelScope.launch { repository.saveJob(job) }
    }

    fun deleteJob(id: Long) {
        viewModelScope.launch { repository.deleteJob(id) }
    }

    fun addDailyTask(titleEn: String, titleBn: String, category: String) {
        viewModelScope.launch { repository.addDailyTask(titleEn, titleBn, category) }
    }

    fun toggleDailyTask(id: Long, done: Boolean) {
        viewModelScope.launch { repository.toggleDailyTask(id, done) }
    }

    fun deleteDailyTask(id: Long) {
        viewModelScope.launch { repository.deleteDailyTask(id) }
    }

    fun addPracticeMinutes(delta: Int) {
        viewModelScope.launch { repository.addPracticeMinutes(delta) }
    }

    fun recordQuizAttempt(questionId: String, topicEn: String, topicBn: String, isCorrect: Boolean) {
        viewModelScope.launch {
            repository.recordQuizAttempt(questionId, topicEn, topicBn, isCorrect)
        }
    }

    fun resetQuizStats() {
        viewModelScope.launch { repository.resetQuizStats() }
    }

    fun toggleMasteredFlashcard(cardId: String, mastered: Boolean) {
        viewModelScope.launch { repository.toggleMasteredFlashcard(cardId, mastered) }
    }

    fun toggleChecklist(itemId: String, groupName: String, checked: Boolean) {
        viewModelScope.launch { repository.toggleChecklist(itemId, groupName, checked) }
    }

    fun saveGeminiConfig(apiKey: String) {
        val trimmed = apiKey.trim()
        val existing = uiState.value.preferences.geminiApiKey.trim()
        viewModelScope.launch {
            announceNextValidation = true
            if (trimmed == existing) {
                runAutoKeyValidation(trimmed, uiState.value.isOnline)
            } else {
                repository.setGeminiConfig(trimmed, DEFAULT_GEMINI_MODEL)
            }
        }
    }

    fun saveProfile(name: String, targetTrade: String) {
        viewModelScope.launch {
            repository.setProfile(name, targetTrade)
        }
    }

    fun saveDailyGoals(jobGoal: Int, quizGoal: Int, minutesGoal: Int) {
        viewModelScope.launch {
            repository.setDailyGoals(jobGoal, quizGoal, minutesGoal)
        }
    }

    suspend fun buildJsonBackup(): String = repository.exportJsonBackup()

    suspend fun buildCsvBackup(): String = repository.exportJobsCsv()

    fun importBackup(rawContent: String) {
        viewModelScope.launch {
            val result = repository.importBackupContent(rawContent)
            _statusMessage.value = result.fold(
                onSuccess = { it },
                onFailure = { "Import failed: ${it.localizedMessage}" }
            )
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun showStatusMessage(msg: String) {
        _statusMessage.value = msg
    }

    fun sendAiMessage(prompt: String) {
        val clean = prompt.trim()
        if (clean.isEmpty() || _aiChat.value.isLoading) return

        val userMsg = ChatMessage(
            id = "u_${System.currentTimeMillis()}",
            isUser = true,
            text = clean
        )
        val priorHistory = _aiChat.value.messages
        _aiChat.update {
            it.copy(
                messages = it.messages + userMsg,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = repository.askAiMentor(priorHistory, clean)
            result.fold(
                onSuccess = { reply ->
                    val botMsg = ChatMessage(
                        id = "m_${System.currentTimeMillis()}",
                        isUser = false,
                        text = reply
                    )
                    _aiChat.update {
                        it.copy(
                            messages = it.messages + botMsg,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { err ->
                    when (err) {
                        is InvalidApiKeyException -> {
                            _apiKeyStatus.value = ApiKeyStatus.Invalid(err.localizedMessage ?: "Invalid API key")
                        }
                        is QuotaExceededException -> {
                            val activeModel = uiState.value.preferences.geminiModel.ifBlank { DEFAULT_GEMINI_MODEL }
                            _apiKeyStatus.value = ApiKeyStatus.QuotaLimited(
                                modelId = activeModel,
                                detail = err.localizedMessage ?: "API quota limit reached"
                            )
                        }
                    }
                    _aiChat.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "Failed to reach Gemini API."
                        )
                    }
                }
            )
        }
    }

    fun clearAiChat() {
        _aiChat.value = AiChatState()
    }

    fun refreshLiveRates() {
        if (_isRefreshingRates.value) return
        _isRefreshingRates.value = true
        viewModelScope.launch {
            val lang = uiState.value.preferences.language
            val result = repository.refreshLiveExchangeRates()
            _isRefreshingRates.value = false
            result.fold(
                onSuccess = {
                    _statusMessage.value = if (lang == AppLanguage.BN) {
                        "লাইভ এক্সচেঞ্জ রেট আপডেট ও সেভ হয়েছে।"
                    } else {
                        "Live exchange rates updated & cached offline."
                    }
                },
                onFailure = { err ->
                    _statusMessage.value = err.localizedMessage ?: "Could not refresh rates."
                }
            )
        }
    }

    fun setCustomRate(fromCode: String, toCode: String, rate: Double?) {
        viewModelScope.launch {
            repository.setCustomExchangeRate(fromCode, toCode, rate)
            val lang = uiState.value.preferences.language
            _statusMessage.value = if (rate == null) {
                if (lang == AppLanguage.BN) "অটো রেট চালু করা হয়েছে।" else "Switched back to auto rate."
            } else {
                if (lang == AppLanguage.BN) "কাস্টম রেট সংরক্ষিত হয়েছে।" else "Custom rate saved."
            }
        }
    }

    fun submitDelegateAnswer(question: QuizQuestion, userAnswer: String) {
        if (_vivaDelegate.value.isEvaluating) return
        _vivaDelegate.update {
            it.copy(
                isEvaluating = true,
                lastPassed = null,
                lastFeedback = null,
                autoRecorded = false
            )
        }
        viewModelScope.launch {
            val eval = repository.evaluateDelegateAnswer(question, userAnswer)
            var recordedNow = false
            if (eval.passed) {
                repository.recordQuizAttempt(
                    questionId = question.id,
                    topicEn = question.topic.en,
                    topicBn = question.topic.bn,
                    isCorrect = true
                )
                recordedNow = true
            }
            _vivaDelegate.update {
                it.copy(
                    isEvaluating = false,
                    lastPassed = eval.passed,
                    lastFeedback = eval.feedback,
                    evaluatedByAi = eval.evaluatedByAi,
                    autoRecorded = recordedNow,
                    completedQuestionIds = if (eval.passed) {
                        it.completedQuestionIds + question.id
                    } else {
                        it.completedQuestionIds
                    }
                )
            }
        }
    }

    fun nextDelegateQuestion(totalQuestions: Int) {
        val safeTotal = totalQuestions.coerceAtLeast(1)
        _vivaDelegate.update {
            it.copy(
                currentIndex = (it.currentIndex + 1) % safeTotal,
                lastPassed = null,
                lastFeedback = null,
                autoRecorded = false
            )
        }
    }

    fun resetDelegateFeedback() {
        _vivaDelegate.update {
            it.copy(
                lastPassed = null,
                lastFeedback = null,
                autoRecorded = false
            )
        }
    }
}
