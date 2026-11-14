package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.model.AlarmTone
import com.example.model.ChapterGoal
import com.example.model.DateTimeHelper
import com.example.model.GoalSpan
import com.example.model.RepeatMode
import com.example.model.SUBJECT_ALL
import com.example.model.StudyStream
import com.example.model.StudyTask
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreStudyRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)
    private fun tasksCollection(uid: String) = userDoc(uid).collection("tasks")
    private fun goalsCollection(uid: String) = userDoc(uid).collection("goals")

    fun observeUserProfile(userId: String): Flow<FirestoreUserProfile?> {
        val docRef = userDoc(userId)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        FirestoreUserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    fun observeTasks(userId: String): Flow<List<StudyTask>> {
        val colRef = tasksCollection(userId)
        return colRef.snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    FirestoreStudyTask::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).map { it.toDomain() }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, colRef.path)
                }
                throw error
            }
    }

    fun observeGoals(userId: String): Flow<List<ChapterGoal>> {
        val colRef = goalsCollection(userId)
        return colRef.snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    FirestoreChapterGoal::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).map { it.toDomain() }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, colRef.path)
                }
                throw error
            }
    }

    suspend fun getUserProfile(userId: String): FirestoreUserProfile? {
        val docRef = userDoc(userId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(
                    FirestoreUserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun ensureUserInitialized(
        displayName: String?,
        email: String?
    ) {
        val uid = requireUserId()
        val today = DateTimeHelper.todayString()
        val safeName = displayName?.trim()?.take(80)?.ifBlank { null }
            ?: email?.substringBefore("@")?.trim()?.take(80)?.ifBlank { null }
            ?: "Student"

        val docRef = userDoc(uid)
        val existingSnap = try {
            docRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }

        if (!existingSnap.exists()) {
            val profilePayload = buildMap<String, Any> {
                put("userId", uid)
                put("displayName", safeName)
                if (!email.isNullOrBlank()) put("email", email.take(200))
                put("selectedStream", StudyStream.NEET.code)
                put("selectedSubjectFilter", SUBJECT_ALL)
                put("dailyTargetLectures", 4)
                put("dailyTargetHours", 6)
                put("loggedHoursToday", 0.0)
                put("streak", 1)
                put("alarmTone", AlarmTone.NEON.code)
                put("lastLoggedDate", today)
                put("isSeeded", true)
                put("createdAt", FieldValue.serverTimestamp())
                put("updatedAt", FieldValue.serverTimestamp())
            }
            try {
                docRef.set(profilePayload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }

            val batch = db.batch()
            val starterTasks = listOf(
                mapOf(
                    "userId" to uid,
                    "title" to "Complete Lecture 5 – Motion in a Straight Line",
                    "subject" to "Physics",
                    "date" to today,
                    "time" to "10:00",
                    "repeatCode" to RepeatMode.DAILY.code,
                    "notes" to "Solve 15 NCERT Exemplar MCQs right after lecture",
                    "completed" to false,
                    "celebrated" to false,
                    "firedKey" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "title" to "GOC & Isomerism Quick Revision",
                    "subject" to "Chemistry",
                    "date" to today,
                    "time" to "16:30",
                    "repeatCode" to RepeatMode.ONCE.code,
                    "notes" to "Revise resonance stability order & inductive effects",
                    "completed" to false,
                    "celebrated" to false,
                    "firedKey" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "title" to "Cell Cycle & Division NCERT Line-by-Line",
                    "subject" to "Botany",
                    "date" to today,
                    "time" to "20:00",
                    "repeatCode" to RepeatMode.ONCE.code,
                    "notes" to "Highlight meiosis prophase-I sub-stages",
                    "completed" to false,
                    "celebrated" to false,
                    "firedKey" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            starterTasks.forEach { payload ->
                batch.set(tasksCollection(uid).document(), payload)
            }

            val starterGoals = listOf(
                mapOf(
                    "userId" to uid,
                    "title" to "Finish Kinematics + 80 PYQs",
                    "spanLabel" to GoalSpan.NEXT_2_DAYS.label,
                    "subject" to "Physics",
                    "done" to false,
                    "celebrated" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "title" to "Human Physiology Full Mock Test",
                    "spanLabel" to GoalSpan.NEXT_7_DAYS.label,
                    "subject" to "Zoology",
                    "done" to false,
                    "celebrated" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            starterGoals.forEach { payload ->
                batch.set(goalsCollection(uid).document(), payload)
            }

            try {
                batch.commit().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, tasksCollection(uid).path)
                throw e
            }
        } else {
            val profile = existingSnap.toObject(
                FirestoreUserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            if (profile != null && profile.lastLoggedDate != today) {
                val updates = mutableMapOf<String, Any>(
                    "lastLoggedDate" to today,
                    "loggedHoursToday" to 0.0,
                    "streak" to (profile.streak + 1).coerceAtMost(10000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (safeName != profile.displayName) {
                    updates["displayName"] = safeName
                }
                updateUserProfileFields(updates)
            }
        }
    }

    suspend fun updateUserProfileFields(updates: Map<String, Any>) {
        val uid = requireUserId()
        val docRef = userDoc(uid)
        val payload = updates.toMutableMap().apply {
            put("updatedAt", FieldValue.serverTimestamp())
        }
        try {
            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun setSelectedStream(stream: StudyStream) {
        updateUserProfileFields(
            mapOf(
                "selectedStream" to stream.code,
                "selectedSubjectFilter" to SUBJECT_ALL
            )
        )
    }

    suspend fun setSelectedSubjectFilter(subject: String) {
        updateUserProfileFields(
            mapOf("selectedSubjectFilter" to subject.ifBlank { SUBJECT_ALL }.take(60))
        )
    }

    suspend fun setDailyTargetLectures(count: Int) {
        updateUserProfileFields(
            mapOf("dailyTargetLectures" to count.coerceIn(1, 20))
        )
    }

    suspend fun setDailyTargetHours(hours: Int) {
        updateUserProfileFields(
            mapOf("dailyTargetHours" to hours.coerceIn(1, 18))
        )
    }

    suspend fun adjustLoggedHours(currentHours: Float, delta: Float) {
        val next = (currentHours + delta).coerceIn(0f, 18f)
        val rounded = (kotlin.math.round(next * 10f) / 10f).toDouble()
        updateUserProfileFields(
            mapOf(
                "loggedHoursToday" to rounded,
                "lastLoggedDate" to DateTimeHelper.todayString()
            )
        )
    }

    suspend fun setAlarmTone(tone: AlarmTone) {
        updateUserProfileFields(
            mapOf("alarmTone" to tone.code)
        )
    }

    suspend fun createTask(task: StudyTask): String {
        val uid = requireUserId()
        val docRef = if (task.id.isNotBlank() && task.id.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$"))) {
            tasksCollection(uid).document(task.id)
        } else {
            tasksCollection(uid).document()
        }
        val payload = mapOf(
            "userId" to uid,
            "title" to task.title.trim().take(200),
            "subject" to task.subject.trim().take(60),
            "date" to task.date.ifBlank { DateTimeHelper.todayString() }.take(20),
            "time" to task.time.ifBlank { "10:00" }.take(10),
            "repeatCode" to task.repeatMode.code,
            "notes" to task.notes.trim().take(500),
            "completed" to task.completed,
            "celebrated" to task.celebrated,
            "firedKey" to task.firedKey.take(30),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateTask(task: StudyTask) {
        val uid = requireUserId()
        require(task.id.isNotBlank()) { "Task ID must not be blank for update" }
        val docRef = tasksCollection(uid).document(task.id)
        val updates = mapOf(
            "title" to task.title.trim().take(200),
            "subject" to task.subject.trim().take(60),
            "date" to task.date.ifBlank { DateTimeHelper.todayString() }.take(20),
            "time" to task.time.ifBlank { "10:00" }.take(10),
            "repeatCode" to task.repeatMode.code,
            "notes" to task.notes.trim().take(500),
            "completed" to task.completed,
            "celebrated" to task.celebrated,
            "firedKey" to task.firedKey.take(30),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTask(taskId: String) {
        val uid = requireUserId()
        require(taskId.isNotBlank()) { "Task ID must not be blank for delete" }
        val docRef = tasksCollection(uid).document(taskId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun createGoal(goal: ChapterGoal): String {
        val uid = requireUserId()
        val docRef = if (goal.id.isNotBlank() && goal.id.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$"))) {
            goalsCollection(uid).document(goal.id)
        } else {
            goalsCollection(uid).document()
        }
        val payload = mapOf(
            "userId" to uid,
            "title" to goal.title.trim().take(200),
            "spanLabel" to goal.span.label,
            "subject" to goal.subject.trim().take(60),
            "done" to goal.done,
            "celebrated" to goal.celebrated,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateGoal(goal: ChapterGoal) {
        val uid = requireUserId()
        require(goal.id.isNotBlank()) { "Goal ID must not be blank for update" }
        val docRef = goalsCollection(uid).document(goal.id)
        val updates = mapOf(
            "title" to goal.title.trim().take(200),
            "spanLabel" to goal.span.label,
            "subject" to goal.subject.trim().take(60),
            "done" to goal.done,
            "celebrated" to goal.celebrated,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteGoal(goalId: String) {
        val uid = requireUserId()
        require(goalId.isNotBlank()) { "Goal ID must not be blank for delete" }
        val docRef = goalsCollection(uid).document(goalId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
