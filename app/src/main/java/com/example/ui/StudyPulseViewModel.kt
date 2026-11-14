package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.StudySoundPlayer
import com.example.data.local.StudyPreferencesStore
import com.example.data.local.StudyPulseDatabase
import com.example.data.local.StudyPulseRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    val celebrationNonce: Long = 0L
)

data class StudyPulseUiState(
    val preferences: StudyPreferences = StudyPreferences(),
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
    val celebrationNonce: Long = 0L
)

class StudyPulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyPulseRepository(
        dao = StudyPulseDatabase.getInstance(application).studyDao(),
        preferencesStore = StudyPreferencesStore(application)
    )
    private val soundPlayer = StudySoundPlayer(application)
    private val ephemeral = MutableStateFlow(EphemeralUiState())

    val uiState: StateFlow<StudyPulseUiState> = combine(
        repository.preferencesFlow,
        repository.tasksFlow,
        repository.goalsFlow,
        ephemeral
    ) { prefs, tasks, goals, eph ->
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
            allTasks = tasks,
            todayTasks = todayFiltered,
            filteredAllTasks = alarmsFiltered,
            calendarTasks = calTasks,
            goals = goals,
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
            celebrationNonce = eph.celebrationNonce
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = StudyPulseUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
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
                        repository.saveTask(stamped)
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

    fun selectStream(stream: StudyStream) {
        viewModelScope.launch {
            repository.setSelectedStream(stream)
        }
    }

    fun selectSubjectFilter(subject: String) {
        viewModelScope.launch {
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
        val payload = if (existing != null) {
            existing.copy(
                title = cleanTitle,
                subject = subject,
                date = date.ifBlank { DateTimeHelper.todayString() },
                time = time.ifBlank { "10:00" },
                repeatMode = repeatMode,
                notes = notes.trim(),
                firedKey = ""
            )
        } else {
            StudyTask(
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
        }
        viewModelScope.launch {
            repository.saveTask(payload)
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
        viewModelScope.launch {
            repository.saveTask(updated)
            if (shouldCelebrate) {
                fireCelebration(task.title)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.saveGoal(goal)
        }
    }

    fun toggleGoalCompleted(goal: ChapterGoal) {
        val nowDone = !goal.done
        val shouldCelebrate = nowDone && !goal.celebrated
        val updated = goal.copy(
            done = nowDone,
            celebrated = if (shouldCelebrate) true else goal.celebrated
        )
        viewModelScope.launch {
            repository.saveGoal(updated)
            if (shouldCelebrate) {
                fireCelebration(goal.title)
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun updateTargetLectures(count: Int) {
        viewModelScope.launch {
            repository.setDailyTargetLectures(count)
        }
    }

    fun updateTargetHours(hours: Int) {
        viewModelScope.launch {
            repository.setDailyTargetHours(hours)
        }
    }

    fun adjustLoggedHours(delta: Float) {
        viewModelScope.launch {
            repository.adjustLoggedHours(delta)
        }
    }

    fun selectAlarmTone(tone: AlarmTone) {
        viewModelScope.launch {
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
            viewModelScope.launch {
                repository.saveTask(ringing.copy(time = newTime, firedKey = ""))
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

    override fun onCleared() {
        super.onCleared()
        soundPlayer.stopAlarm()
    }
}
