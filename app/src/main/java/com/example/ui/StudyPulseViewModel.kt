package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.StudySoundPlayer
import com.example.data.remote.FirestoreStudyRepository
import com.example.model.AlarmTone
import com.example.model.ChapterGoal
import com.example.model.DateTimeHelper
import com.example.model.GoalSpan
import com.example.model.RepeatMode
import com.example.model.SUBJECT_ALL
import com.example.model.StudyCurriculum
import com.example.model.StudyPreferences
import com.example.model.StudyStream
import com.example.model.StudyTab
import com.example.model.StudyTask
import com.example.model.TaskFilter
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date

data class EphemeralUiState(
    val currentTab: StudyTab = StudyTab.DASHBOARD,
    val taskFilter: TaskFilter = TaskFilter.ALL,
    val selectedCalendarDate: String = DateTimeHelper.todayString(),
    val clockTimeDisplay: String = DateTimeHelper.formatDigitalClock(),
    val headerDateDisplay: String = DateTimeHelper.formatHeaderDate(),
    val isTaskModalOpen: Boolean = false,
    val editingTask: StudyTask? = null,
    val ringingTask: StudyTask? = null,
    val celebrationTitle: String? = null,
    val celebrationNonce: Long = 0L,
    val isAccountDialogOpen: Boolean = false,
    val syncErrorMessage: String? = null
)

data class StudyPulseUiState(
    val preferences: StudyPreferences = StudyPreferences(),
    val accountDisplayName: String = "Student",
    val accountEmail: String? = null,
    val isSyncingCloud: Boolean = true,
    val syncErrorMessage: String? = null,
    val allTasks: List<StudyTask> = emptyList(),
    val todayTasks: List<StudyTask> = emptyList(),
    val filteredAllTasks: List<StudyTask> = emptyList(),
    val calendarTasks: List<StudyTask> = emptyList(),
    val goals: List<ChapterGoal> = emptyList(),
    val todayTotalCount: Int = 0,
    val todayDoneCount: Int = 0,
    val todayPendingCount: Int = 0,
    val todayProgressPercent: Int = 0,
    val dailyGoalPercent: Int = 0,
    val currentTab: StudyTab = StudyTab.DASHBOARD,
    val taskFilter: TaskFilter = TaskFilter.ALL,
    val selectedCalendarDate: String = DateTimeHelper.todayString(),
    val clockTimeDisplay: String = DateTimeHelper.formatDigitalClock(),
    val headerDateDisplay: String = DateTimeHelper.formatHeaderDate(),
    val isTaskModalOpen: Boolean = false,
    val editingTask: StudyTask? = null,
    val ringingTask: StudyTask? = null,
    val celebrationTitle: String? = null,
    val celebrationNonce: Long = 0L,
    val isAccountDialogOpen: Boolean = false
)

class StudyPulseViewModel(
    private val repository: FirestoreStudyRepository,
    private val soundPlayer: StudySoundPlayer,
    private val currentUserId: String,
    private val initialDisplayName: String?,
    private val initialEmail: String?
) : ViewModel() {

    private val ephemeral = MutableStateFlow(EphemeralUiState())

    private fun unwrapRootError(error: Throwable): Throwable? {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return null
        return firestoreError ?: error
    }

    private val profileFlow = repository.observeUserProfile(currentUserId)
        .catch { error ->
            val root = unwrapRootError(error) ?: throw error
            Log.w(TAG, "Error observing user profile", root)
            ephemeral.update {
                it.copy(syncErrorMessage = root.localizedMessage ?: "Failed to sync profile")
            }
            emit(null)
        }

    private val tasksFlow = repository.observeTasks(currentUserId)
        .catch { error ->
            val root = unwrapRootError(error) ?: throw error
            Log.w(TAG, "Error observing study tasks", root)
            ephemeral.update {
                it.copy(syncErrorMessage = root.localizedMessage ?: "Failed to sync tasks")
            }
            emit(emptyList())
        }

    private val goalsFlow = repository.observeGoals(currentUserId)
        .catch { error ->
            val root = unwrapRootError(error) ?: throw error
            Log.w(TAG, "Error observing chapter goals", root)
            ephemeral.update {
                it.copy(syncErrorMessage = root.localizedMessage ?: "Failed to sync goals")
            }
            emit(emptyList())
        }

    val uiState: StateFlow<StudyPulseUiState> = combine(
        profileFlow,
        tasksFlow,
        goalsFlow,
        ephemeral
    ) { profile, tasks, goals, eph ->
        val prefs = profile?.toPreferences() ?: StudyPreferences()
        val displayName = profile?.displayName?.ifBlank { null }
            ?: initialDisplayName?.ifBlank { null }
            ?: initialEmail?.substringBefore("@")?.ifBlank { null }
            ?: "Student"
        val email = profile?.email ?: initialEmail

        val today = DateTimeHelper.todayString()
        val subjectFilter = prefs.selectedSubjectFilter

        val todayUnfiltered = tasks.filter { it.date == today || it.repeatMode == RepeatMode.DAILY }
        val todayTotal = todayUnfiltered.size
        val todayDone = todayUnfiltered.count { it.completed }
        val todayPending = (todayTotal - todayDone).coerceAtLeast(0)
        val todayPct = if (todayTotal > 0) ((todayDone * 100f) / todayTotal).toInt().coerceIn(0, 100) else 0

        val todayFiltered = todayUnfiltered.filter {
            subjectFilter == SUBJECT_ALL || it.subject == subjectFilter
        }.sortedBy { it.time }

        val alarmsFiltered = tasks.filter { task ->
            val filterMatch = when (eph.taskFilter) {
                TaskFilter.ALL -> true
                TaskFilter.PENDING -> !task.completed
                TaskFilter.COMPLETED -> task.completed
            }
            val subMatch = subjectFilter == SUBJECT_ALL || task.subject == subjectFilter
            filterMatch && subMatch
        }.sortedBy { it.time }

        val calTasks = tasks.filter {
            it.date == eph.selectedCalendarDate || it.repeatMode == RepeatMode.DAILY
        }.sortedBy { it.time }

        val lectureProgress = (todayDone.toFloat() / prefs.dailyTargetLectures.coerceAtLeast(1)) * 50f
        val hoursProgress = (prefs.loggedHoursToday / prefs.dailyTargetHours.coerceAtLeast(1).toFloat()) * 50f
        val dailyGoalPct = (lectureProgress + hoursProgress).toInt().coerceIn(0, 100)

        StudyPulseUiState(
            preferences = prefs,
            accountDisplayName = displayName,
            accountEmail = email,
            isSyncingCloud = profile == null,
            syncErrorMessage = eph.syncErrorMessage,
            allTasks = tasks,
            todayTasks = todayFiltered,
            filteredAllTasks = alarmsFiltered,
            calendarTasks = calTasks,
            goals = goals.sortedByDescending { it.createdAt },
            todayTotalCount = todayTotal,
            todayDoneCount = todayDone,
            todayPendingCount = todayPending,
            todayProgressPercent = todayPct,
            dailyGoalPercent = dailyGoalPct,
            currentTab = eph.currentTab,
            taskFilter = eph.taskFilter,
            selectedCalendarDate = eph.selectedCalendarDate,
            clockTimeDisplay = eph.clockTimeDisplay,
            headerDateDisplay = eph.headerDateDisplay,
            isTaskModalOpen = eph.isTaskModalOpen,
            editingTask = eph.editingTask,
            ringingTask = eph.ringingTask,
            celebrationTitle = eph.celebrationTitle,
            celebrationNonce = eph.celebrationNonce,
            isAccountDialogOpen = eph.isAccountDialogOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = StudyPulseUiState(
            accountDisplayName = initialDisplayName ?: "Student",
            accountEmail = initialEmail
        )
    )

    init {
        viewModelScope.launch {
            runCatching {
                repository.ensureUserInitialized(
                    displayName = initialDisplayName,
                    email = initialEmail
                )
            }.onFailure { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, "Cloud profile initialization failed", error)
                ephemeral.update {
                    it.copy(syncErrorMessage = error.localizedMessage ?: "Failed to initialize cloud profile")
                }
            }
        }
        startClockTicker()
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                val now = Date()
                val clockText = DateTimeHelper.formatDigitalClock(now)
                val dateText = DateTimeHelper.formatHeaderDate(now)
                ephemeral.update {
                    it.copy(clockTimeDisplay = clockText, headerDateDisplay = dateText)
                }

                val currentState = uiState.value
                val currentDate = DateTimeHelper.todayString()
                val currentHM = DateTimeHelper.formatCurrentHM(now)
                val stamp = "${currentDate}_$currentHM"

                if (currentState.ringingTask == null) {
                    val dueTask = currentState.allTasks.firstOrNull { task ->
                        val dateMatch = (task.date == currentDate) || (task.repeatMode == RepeatMode.DAILY)
                        !task.completed && dateMatch && task.time == currentHM && task.firedKey != stamp
                    }
                    if (dueTask != null) {
                        val stamped = dueTask.copy(firedKey = stamp)
                        runCatching { repository.updateTask(stamped) }
                        triggerAlarm(stamped, currentState.preferences.alarmTone)
                    }
                }
                delay(1000L)
            }
        }
    }

    fun selectTab(tab: StudyTab) {
        ephemeral.update { it.copy(currentTab = tab) }
    }

    fun openAccountDialog() {
        ephemeral.update { it.copy(isAccountDialogOpen = true) }
    }

    fun closeAccountDialog() {
        ephemeral.update { it.copy(isAccountDialogOpen = false) }
    }

    fun clearSyncError() {
        ephemeral.update { it.copy(syncErrorMessage = null) }
    }

    fun selectStream(stream: StudyStream) {
        launchWrite("Failed to update exam stream") {
            repository.setSelectedStream(stream)
        }
    }

    fun selectSubjectFilter(subject: String) {
        launchWrite("Failed to update subject filter") {
            repository.setSelectedSubjectFilter(subject)
        }
    }

    fun selectTaskFilter(filter: TaskFilter) {
        ephemeral.update { it.copy(taskFilter = filter) }
    }

    fun selectCalendarDate(date: String) {
        ephemeral.update { it.copy(selectedCalendarDate = date) }
    }

    fun openNewTaskModal() {
        ephemeral.update { it.copy(isTaskModalOpen = true, editingTask = null) }
    }

    fun openEditTaskModal(task: StudyTask) {
        ephemeral.update { it.copy(isTaskModalOpen = true, editingTask = task) }
    }

    fun closeTaskModal() {
        ephemeral.update { it.copy(isTaskModalOpen = false, editingTask = null) }
    }

    fun saveTask(
        title: String,
        subject: String,
        date: String,
        time: String,
        repeatMode: RepeatMode,
        notes: String
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val existing = ephemeral.value.editingTask
        launchWrite("Failed to save study session") {
            if (existing != null) {
                val updated = existing.copy(
                    title = cleanTitle,
                    subject = subject,
                    date = date.ifBlank { DateTimeHelper.todayString() },
                    time = time.ifBlank { "10:00" },
                    repeatMode = repeatMode,
                    notes = notes.trim(),
                    firedKey = ""
                )
                repository.updateTask(updated)
            } else {
                val created = StudyTask(
                    id = "t_${System.currentTimeMillis()}",
                    title = cleanTitle,
                    subject = subject,
                    date = date.ifBlank { DateTimeHelper.todayString() },
                    time = time.ifBlank { "10:00" },
                    repeatMode = repeatMode,
                    notes = notes.trim(),
                    completed = false,
                    celebrated = false,
                    firedKey = ""
                )
                repository.createTask(created)
            }
            closeTaskModal()
        }
    }

    fun toggleTaskCompleted(task: StudyTask) {
        val nowCompleted = !task.completed
        val shouldCelebrate = nowCompleted && !task.celebrated
        val updated = task.copy(
            completed = nowCompleted,
            celebrated = if (shouldCelebrate) true else task.celebrated
        )
        launchWrite("Failed to update session status") {
            repository.updateTask(updated)
            if (shouldCelebrate) {
                fireCelebration(task.title)
            }
        }
    }

    fun deleteTask(taskId: String) {
        launchWrite("Failed to delete session") {
            repository.deleteTask(taskId)
        }
    }

    fun addChapterGoal(title: String, span: GoalSpan, subject: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        val goal = ChapterGoal(
            id = "g_${System.currentTimeMillis()}",
            title = clean,
            span = span,
            subject = subject,
            done = false,
            celebrated = false
        )
        launchWrite("Failed to save chapter goal") {
            repository.createGoal(goal)
        }
    }

    fun toggleGoalCompleted(goal: ChapterGoal) {
        val nowDone = !goal.done
        val shouldCelebrate = nowDone && !goal.celebrated
        val updated = goal.copy(
            done = nowDone,
            celebrated = if (shouldCelebrate) true else goal.celebrated
        )
        launchWrite("Failed to update goal status") {
            repository.updateGoal(updated)
            if (shouldCelebrate) {
                fireCelebration(goal.title)
            }
        }
    }

    fun deleteGoal(goalId: String) {
        launchWrite("Failed to delete goal") {
            repository.deleteGoal(goalId)
        }
    }

    fun updateTargetLectures(count: Int) {
        launchWrite("Failed to update lecture target") {
            repository.setDailyTargetLectures(count)
        }
    }

    fun updateTargetHours(hours: Int) {
        launchWrite("Failed to update hours target") {
            repository.setDailyTargetHours(hours)
        }
    }

    fun adjustLoggedHours(delta: Float) {
        val currentHours = uiState.value.preferences.loggedHoursToday
        launchWrite("Failed to update logged hours") {
            repository.adjustLoggedHours(currentHours, delta)
        }
    }

    fun selectAlarmTone(tone: AlarmTone) {
        launchWrite("Failed to update alarm tone") {
            repository.setAlarmTone(tone)
            soundPlayer.previewTone(tone)
        }
    }

    fun triggerTestAlarm() {
        val prefs = uiState.value.preferences
        val firstSubject = StudyCurriculum.subjectsFor(prefs.selectedStream)
            .firstOrNull()?.name ?: "Physics"
        val testTask = StudyTask(
            id = "test",
            title = "Complete Chapter 1 – $firstSubject Masterclass",
            subject = firstSubject,
            date = DateTimeHelper.todayString(),
            time = DateTimeHelper.formatCurrentHM(),
            notes = "Test Alarm by Prince – Sound & Flower Burst Active!"
        )
        triggerAlarm(testTask, prefs.alarmTone)
    }

    private fun triggerAlarm(task: StudyTask, tone: AlarmTone) {
        ephemeral.update { it.copy(ringingTask = task) }
        soundPlayer.startAlarmLoop(tone)
    }

    fun completeFromRingingAlarm() {
        val ringing = ephemeral.value.ringingTask
        dismissRingingAlarm()
        if (ringing != null) {
            if (ringing.id == "test") {
                fireCelebration(ringing.title)
            } else {
                val latest = uiState.value.allTasks.firstOrNull { it.id == ringing.id } ?: ringing
                if (!latest.completed) {
                    toggleTaskCompleted(latest)
                }
            }
        }
    }

    fun snoozeRingingAlarm(minutes: Int) {
        val ringing = ephemeral.value.ringingTask
        dismissRingingAlarm()
        if (ringing != null && ringing.id != "test") {
            val newTime = DateTimeHelper.snoozeTimeHM(minutes)
            launchWrite("Failed to snooze alarm") {
                repository.updateTask(ringing.copy(time = newTime, firedKey = ""))
            }
        }
    }

    fun dismissRingingAlarm() {
        soundPlayer.stopAlarm()
        ephemeral.update { it.copy(ringingTask = null) }
    }

    private fun fireCelebration(title: String) {
        soundPlayer.playCelebrationChime()
        ephemeral.update {
            it.copy(
                celebrationTitle = title,
                celebrationNonce = System.currentTimeMillis()
            )
        }
    }

    fun clearCelebrationBanner() {
        ephemeral.update { it.copy(celebrationTitle = null) }
    }

    private fun launchWrite(fallbackMsg: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }.onFailure { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, fallbackMsg, error)
                ephemeral.update {
                    it.copy(syncErrorMessage = error.localizedMessage ?: fallbackMsg)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.stopAlarm()
    }

    private companion object {
        const val TAG = "StudyPulseVM"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
