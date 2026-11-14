package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.AlarmTone
import com.example.model.ChapterGoal
import com.example.model.DateTimeHelper
import com.example.model.GoalSpan
import com.example.model.RepeatMode
import com.example.model.StudyStream
import com.example.model.StudyTask
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class StudyRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureUserInitialized_createsProfileAndStarterStudyData() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreStudyRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserInitialized(displayName = "Prince", email = ALICE_EMAIL)
        }

        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getUserProfile(aliceUid)
        }
        assertNotNull(profile)
        assertEquals(aliceUid, profile?.userId)
        assertEquals("Prince", profile?.displayName)

        val tasks = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTasks(aliceUid).first { it.isNotEmpty() }
        }
        assertTrue(tasks.isNotEmpty())

        val goals = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeGoals(aliceUid).first { it.isNotEmpty() }
        }
        assertTrue(goals.isNotEmpty())
    }

    @Test
    fun ownerTaskAndGoalCrud_succeedsAndEmitsRealtimeUpdates() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreStudyRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserInitialized(displayName = "Prince", email = ALICE_EMAIL)
            repository.setSelectedStream(StudyStream.JEE)
            repository.setAlarmTone(AlarmTone.CYBER)
        }

        val taskId = "task_${UUID.randomUUID().toString().take(8)}"
        val newTask = StudyTask(
            id = taskId,
            title = "Rotational Motion Lecture 3",
            subject = "Physics",
            date = DateTimeHelper.todayString(),
            time = "17:30",
            repeatMode = RepeatMode.ONCE,
            notes = "Solve 20 PYQs"
        )
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTask(newTask)
        }

        val observedTasks = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTasks(aliceUid).first { list -> list.any { it.id == taskId } }
        }
        assertTrue(observedTasks.any { it.id == taskId && it.title == "Rotational Motion Lecture 3" })

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateTask(newTask.copy(completed = true, celebrated = true))
        }
        val completedTasks = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTasks(aliceUid).first { list ->
                list.any { it.id == taskId && it.completed }
            }
        }
        assertTrue(completedTasks.any { it.id == taskId && it.completed })

        val goalId = "goal_${UUID.randomUUID().toString().take(8)}"
        val newGoal = ChapterGoal(
            id = goalId,
            title = "Electrodynamics Full Revision",
            span = GoalSpan.NEXT_7_DAYS,
            subject = "Physics"
        )
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createGoal(newGoal)
            repository.updateGoal(newGoal.copy(done = true, celebrated = true))
        }
        val observedGoals = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeGoals(aliceUid).first { list ->
                list.any { it.id == goalId && it.done }
            }
        }
        assertTrue(observedGoals.any { it.id == goalId && it.done })

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteTask(taskId)
            repository.deleteGoal(goalId)
        }
    }

    @Test
    fun crossUserReadAndObserve_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreStudyRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.ensureUserInitialized(displayName = "Alice", email = ALICE_EMAIL)
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = FirestoreStudyRepository(firestore, auth)

        val getEx = assertThrowsType<FirebaseFirestoreException> {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                bobRepo.getUserProfile(aliceUid)
            }
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, getEx.code)

        val listEx = assertThrowsType<Exception> {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeTasks(aliceUid).first()
            }
        }
        val firestoreEx = checkNotNull(listEx.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $listEx"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }

    @Test
    fun unauthenticatedObserve_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()

        val repository = FirestoreStudyRepository(firestore, auth)
        val ex = assertThrowsType<Exception> {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeTasks(aliceUid).first()
            }
        }
        val firestoreEx = checkNotNull(ex.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $ex"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_studypulse@test.com"
        const val BOB_EMAIL = "bob_studypulse@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
