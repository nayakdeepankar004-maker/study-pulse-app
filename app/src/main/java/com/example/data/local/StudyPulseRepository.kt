package com.example.data.local

import com.example.model.AlarmTone
import com.example.model.ChapterGoal
import com.example.model.DateTimeHelper
import com.example.model.GoalSpan
import com.example.model.RepeatMode
import com.example.model.StudyPreferences
import com.example.model.StudyStream
import com.example.model.StudyTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class StudyPulseRepository(
    private val dao: StudyDao,
    private val preferencesStore: StudyPreferencesStore
) {
    val tasksFlow: Flow<List<StudyTask>> = dao.observeTasks().map { list ->
        list.map { it.toDomain() }
    }

    val goalsFlow: Flow<List<ChapterGoal>> = dao.observeGoals().map { list ->
        list.map { it.toDomain() }
    }

    val preferencesFlow: Flow<StudyPreferences> = preferencesStore.preferencesFlow

    suspend fun ensureSeeded() {
        val prefs = preferencesStore.preferencesFlow.first()
        val today = DateTimeHelper.todayString()
        preferencesStore.rolloverDayIfNeeded(today)
        if (prefs.isSeeded) return

        val now = System.currentTimeMillis()
        val initialTasks = listOf(
            StudyTask(
                id = "t1",
                title = "Complete Lecture 5 – Motion in a Straight Line",
                subject = "Physics",
                date = today,
                time = "10:00",
                repeatMode = RepeatMode.DAILY,
                notes = "Formula revision + 15 MCQs",
                completed = false,
                celebrated = false,
                firedKey = "",
                createdAt = now
            ),
            StudyTask(
                id = "t2",
                title = "Chemical Bonding – Hybridization & VSEPR",
                subject = "Chemistry",
                date = today,
                time = "13:00",
                repeatMode = RepeatMode.ONCE,
                notes = "Solve NCERT Intext questions",
                completed = false,
                celebrated = false,
                firedKey = "",
                createdAt = now + 1
            ),
            StudyTask(
                id = "t3",
                title = "Cell: The Unit of Life – NCERT Line by Line",
                subject = "Botany",
                date = today,
                time = "16:00",
                repeatMode = RepeatMode.ONCE,
                notes = "Revise eukaryotic cell diagrams",
                completed = false,
                celebrated = false,
                firedKey = "",
                createdAt = now + 2
            )
        )

        val initialGoals = listOf(
            ChapterGoal(
                id = "g1",
                title = "Complete Kinematics Chapter + 50 Questions",
                span = GoalSpan.NEXT_2_DAYS,
                subject = "Physics",
                done = false,
                celebrated = false,
                createdAt = now + 10
            ),
            ChapterGoal(
                id = "g2",
                title = "Finish Periodic Table NCERT Notes",
                span = GoalSpan.NEXT_7_DAYS,
                subject = "Chemistry",
                done = false,
                celebrated = false,
                createdAt = now + 5
            )
        )

        dao.upsertTasks(initialTasks.map { StudyTaskEntity.fromDomain(it) })
        dao.upsertGoals(initialGoals.map { ChapterGoalEntity.fromDomain(it) })
        preferencesStore.markSeeded()
    }

    suspend fun saveTask(task: StudyTask) {
        dao.upsertTask(StudyTaskEntity.fromDomain(task))
    }

    suspend fun deleteTask(taskId: String) {
        dao.deleteTaskById(taskId)
    }

    suspend fun saveGoal(goal: ChapterGoal) {
        dao.upsertGoal(ChapterGoalEntity.fromDomain(goal))
    }

    suspend fun deleteGoal(goalId: String) {
        dao.deleteGoalById(goalId)
    }

    suspend fun setSelectedStream(stream: StudyStream) =
        preferencesStore.setSelectedStream(stream)

    suspend fun setSelectedSubjectFilter(subject: String) =
        preferencesStore.setSelectedSubjectFilter(subject)

    suspend fun setDailyTargetLectures(count: Int) =
        preferencesStore.setDailyTargetLectures(count)

    suspend fun setDailyTargetHours(hours: Int) =
        preferencesStore.setDailyTargetHours(hours)

    suspend fun adjustLoggedHours(delta: Float) =
        preferencesStore.adjustLoggedHours(delta)

    suspend fun setAlarmTone(tone: AlarmTone) =
        preferencesStore.setAlarmTone(tone)
}
