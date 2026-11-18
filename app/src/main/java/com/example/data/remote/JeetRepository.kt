package com.example.data.remote

import android.content.Context
import com.example.R
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class JeetRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun validateId(id: String): String {
        val cleaned = id.trim()
        require(cleaned.isNotEmpty() && cleaned.length <= 128 && cleaned.matches(ID_REGEX)) {
            "Invalid document ID format."
        }
        return cleaned
    }

    fun observeProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user profile observation." }
        val docRef = db.collection("users").document(uid)
        docRef.snapshots()
            .map { snapshot ->
                snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                }
                throw error
            }
            .collect { emit(it) }
    }

    fun observeNotes(userId: String): Flow<List<StudyNote>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user notes observation." }
        val colRef = db.collection("users").document(uid).collection("notes")
        colRef.snapshots()
            .map { snapshot ->
                snapshot.toObjects(StudyNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedWith(
                        compareByDescending<StudyNote> { it.pinned }
                            .thenByDescending { it.updatedAt?.seconds ?: 0L }
                    )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                }
                throw error
            }
            .collect { emit(it) }
    }

    fun observePdfs(userId: String): Flow<List<PdfDocumentMeta>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user PDFs observation." }
        val colRef = db.collection("users").document(uid).collection("pdfs")
        colRef.snapshots()
            .map { snapshot ->
                snapshot.toObjects(PdfDocumentMeta::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                }
                throw error
            }
            .collect { emit(it) }
    }

    fun observeTestAttempts(userId: String): Flow<List<TestAttempt>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user testAttempts observation." }
        val colRef = db.collection("users").document(uid).collection("testAttempts")
        colRef.snapshots()
            .map { snapshot ->
                snapshot.toObjects(TestAttempt::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                }
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun ensureUserProfile(
        displayName: String?,
        email: String?
    ): Result<Unit> {
        var docPath = "users/unknown"
        return try {
            val uid = validateId(requireUserId())
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val profile = UserProfile(
                    userId = uid,
                    displayName = displayName?.trim()?.takeIf { it.isNotEmpty() }?.take(80) ?: "Student",
                    email = email?.trim()?.takeIf { it.isNotEmpty() }?.take(200),
                    board = "CBSE",
                    studentClass = "Class 11",
                    stream = "Science PCM",
                    targetExam = "JEE Main",
                    onboardingCompleted = false,
                    totalTestsTaken = 0L,
                    bestAccuracy = 0L,
                    streakDays = 1L
                )
                docRef.set(profile.toCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun saveAcademicProfile(
        displayName: String,
        board: String,
        studentClass: String,
        stream: String,
        targetExam: String,
        onboardingCompleted: Boolean = true
    ): Result<Unit> {
        var docPath = "users/unknown"
        return try {
            val uid = validateId(requireUserId())
            require(board in ALLOWED_BOARDS) { "Invalid board." }
            require(studentClass in ALLOWED_CLASSES) { "Invalid class." }
            require(stream in ALLOWED_STREAMS) { "Invalid stream." }
            require(targetExam in ALLOWED_EXAMS) { "Invalid exam track." }
            val cleanName = displayName.trim().ifBlank { "Student" }.take(80)
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "displayName" to cleanName,
                    "board" to board,
                    "studentClass" to studentClass,
                    "stream" to stream,
                    "targetExam" to targetExam,
                    "onboardingCompleted" to onboardingCompleted,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun saveNote(
        existingNoteId: String?,
        title: String,
        subject: String,
        content: String,
        pinned: Boolean
    ): Result<Unit> {
        var docPath = "users/unknown/notes"
        return try {
            val uid = validateId(requireUserId())
            val cleanTitle = title.trim()
            val cleanSubject = subject.trim().ifBlank { "General" }
            require(cleanTitle.isNotEmpty() && cleanTitle.length <= 160) {
                "Note title must be 1 to 160 characters."
            }
            require(cleanSubject.length <= 60) { "Subject must be <= 60 characters." }
            require(content.length <= 10000) { "Note content must be <= 10,000 characters." }

            val colRef = db.collection("users").document(uid).collection("notes")
            if (existingNoteId.isNullOrBlank()) {
                val docId = UUID.randomUUID().toString()
                val docRef = colRef.document(docId)
                docPath = docRef.path
                val note = StudyNote(
                    userId = uid,
                    title = cleanTitle,
                    subject = cleanSubject,
                    content = content,
                    pinned = pinned
                )
                docRef.set(note.toCreateMap()).await()
            } else {
                val validId = validateId(existingNoteId)
                val docRef = colRef.document(validId)
                docPath = docRef.path
                docRef.update(
                    mapOf(
                        "title" to cleanTitle,
                        "subject" to cleanSubject,
                        "content" to content,
                        "pinned" to pinned,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleNotePinned(noteId: String, newPinned: Boolean): Result<Unit> {
        var docPath = "users/unknown/notes"
        return try {
            val uid = validateId(requireUserId())
            val validId = validateId(noteId)
            val docRef = db.collection("users").document(uid).collection("notes").document(validId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "pinned" to newPinned,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        var docPath = "users/unknown/notes"
        return try {
            val uid = validateId(requireUserId())
            val validId = validateId(noteId)
            val docRef = db.collection("users").document(uid).collection("notes").document(validId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addPdfDocument(
        title: String,
        subject: String,
        uriString: String,
        pageCount: Long
    ): Result<Unit> {
        var docPath = "users/unknown/pdfs"
        return try {
            val uid = validateId(requireUserId())
            val cleanTitle = title.trim()
            val cleanSubject = subject.trim().ifBlank { "General" }
            require(cleanTitle.isNotEmpty() && cleanTitle.length <= 200) {
                "PDF title must be 1 to 200 characters."
            }
            require(uriString.isNotBlank() && uriString.length <= 1000) {
                "Invalid PDF URI."
            }
            val docId = UUID.randomUUID().toString()
            val docRef = db.collection("users").document(uid).collection("pdfs").document(docId)
            docPath = docRef.path
            val meta = PdfDocumentMeta(
                userId = uid,
                title = cleanTitle,
                subject = cleanSubject.take(60),
                uriString = uriString,
                pageCount = pageCount.coerceIn(0L, 50000L)
            )
            docRef.set(meta.toCreateMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deletePdfDocument(pdfId: String): Result<Unit> {
        var docPath = "users/unknown/pdfs"
        return try {
            val uid = validateId(requireUserId())
            val validId = validateId(pdfId)
            val docRef = db.collection("users").document(uid).collection("pdfs").document(validId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun recordTestAttempt(
        title: String,
        track: String,
        subject: String,
        totalQuestions: Int,
        correctCount: Int,
        score: Int,
        maxScore: Int,
        durationSeconds: Int,
        currentProfile: UserProfile?
    ): Result<Unit> {
        var docPath = "users/unknown/testAttempts"
        return try {
            val uid = validateId(requireUserId())
            val safeTotal = totalQuestions.coerceIn(1, 200)
            val safeCorrect = correctCount.coerceIn(0, safeTotal)
            val accuracy = ((safeCorrect.toDouble() / safeTotal.toDouble()) * 100.0).toInt().coerceIn(0, 100)
            val safeTrack = if (track in ALLOWED_TRACKS) track else "Board Exams"

            val docId = UUID.randomUUID().toString()
            val attemptRef = db.collection("users").document(uid).collection("testAttempts").document(docId)
            docPath = attemptRef.path
            val attempt = TestAttempt(
                userId = uid,
                title = title.trim().ifBlank { "Practice Quiz" }.take(160),
                track = safeTrack,
                subject = subject.trim().ifBlank { "Mixed" }.take(60),
                totalQuestions = safeTotal.toLong(),
                correctCount = safeCorrect.toLong(),
                score = score.coerceIn(-200, 800).toLong(),
                maxScore = maxScore.coerceIn(1, 800).toLong(),
                accuracyPercent = accuracy.toLong(),
                durationSeconds = durationSeconds.coerceIn(0, 86400).toLong()
            )
            attemptRef.set(attempt.toCreateMap()).await()

            val userRef = db.collection("users").document(uid)
            val newTotal = ((currentProfile?.totalTestsTaken ?: 0L) + 1L).coerceIn(1L, 100000L)
            val newBest = maxOf(currentProfile?.bestAccuracy ?: 0L, accuracy.toLong()).coerceIn(0L, 100L)
            userRef.update(
                mapOf(
                    "totalTestsTaken" to newTotal,
                    "bestAccuracy" to newBest,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    companion object {
        private val ID_REGEX = Regex("^[a-zA-Z0-9_\\-]+$")
        val ALLOWED_BOARDS = setOf("CBSE", "ICSE", "BSE Odisha", "Other State Boards")
        val ALLOWED_CLASSES = setOf(
            "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12"
        )
        val ALLOWED_STREAMS = setOf(
            "Science PCM", "Science PCB", "Commerce", "Arts/Humanities", "Foundation"
        )
        val ALLOWED_EXAMS = setOf("Board Exams", "NEET", "JEE Main", "Foundation")
        val ALLOWED_TRACKS = setOf("Board Exams", "NEET", "JEE Main", "Foundation", "AI Generated")
    }
}
