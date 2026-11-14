package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.StudyPreferencesStore
import com.example.data.local.StudyPulseDatabase
import com.example.data.local.StudyPulseRepository
import com.example.model.ChapterGoal
import com.example.model.DateTimeHelper
import com.example.model.GoalSpan
import com.example.model.RepeatMode
import com.example.model.StudyStream
import com.example.model.StudyTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Study Pulse`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Study Pulse", appName)
    }

    @Test
    fun `repository seeds initial tasks and persists updates`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, StudyPulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val prefsStore = StudyPreferencesStore(context)
            val repo = StudyPulseRepository(db.studyDao(), prefsStore)

            repo.ensureSeeded()
            val seededTasks = repo.tasksFlow.first()
            val seededGoals = repo.goalsFlow.first()
            assertEquals(3, seededTasks.size)
            assertEquals(2, seededGoals.size)

            val newTask = StudyTask(
                id = "test_task_1",
                title = "Electrostatics NCERT Revision",
                subject = "Physics",
                date = DateTimeHelper.todayString(),
                time = "18:30",
                repeatMode = RepeatMode.ONCE,
                notes = "Solve 25 PYQs"
            )
            repo.saveTask(newTask)
            repo.saveGoal(
                ChapterGoal(
                    id = "test_goal_1",
                    title = "Optics Full Chapter",
                    span = GoalSpan.NEXT_2_DAYS,
                    subject = "Physics"
                )
            )
            repo.setSelectedStream(StudyStream.JEE)

            val updatedTasks = repo.tasksFlow.first()
            val updatedGoals = repo.goalsFlow.first()
            val prefs = repo.preferencesFlow.first()

            assertTrue(updatedTasks.any { it.id == "test_task_1" && it.title == "Electrostatics NCERT Revision" })
            assertTrue(updatedGoals.any { it.id == "test_goal_1" })
            assertEquals(StudyStream.JEE, prefs.selectedStream)
        } finally {
            db.close()
        }
    }
}
