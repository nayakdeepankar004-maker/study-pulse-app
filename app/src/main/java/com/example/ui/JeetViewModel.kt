package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ThemeMode
import com.example.data.local.ThemePreferencesStore
import com.example.data.remote.AiSolveMode
import com.example.data.remote.AiStudyRepository
import com.example.data.remote.JeetRepository
import com.example.data.remote.PdfDocumentMeta
import com.example.data.remote.StudyNote
import com.example.data.remote.TestAttempt
import com.example.data.remote.UserProfile
import com.example.model.ChatMessage
import com.example.model.ClassNoteTemplate
import com.example.model.QuizQuestion
import com.example.model.StudyCatalog
import com.example.utils.FocusFeedback
import com.example.utils.ScientificMathEvaluator
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PrimaryTab(val route: String, val label: String) {
    HOME("home", "Home"),
    LIBRARY("library", "Study"),
    PRACTICE("practice", "Practice"),
    AI("ai", "AI Tutor"),
    TOOLS("tools", "Tools")
}

enum class SubDestination {
    NONE,
    NOTES,
    PDF_LIBRARY,
    PROFILE,
    SETTINGS
}

sealed interface StreamState<out T> {
    data object Loading : StreamState<Nothing>
    data class Success<T>(val data: T) : StreamState<T>
    data class Error(val message: String) : StreamState<Nothing>
}

data class ActiveTestState(
    val title: String,
    val track: String,
    val subject: String,
    val questions: List<QuizQuestion>,
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val isExamMode: Boolean = false,
    val negativeMarking: Boolean = false,
    val secondsRemaining: Int = 0,
    val elapsedSeconds: Int = 0,
    val isFinished: Boolean = false,
    val lastScore: Int = 0,
    val lastMaxScore: Int = 0,
    val lastCorrectCount: Int = 0,
    val lastAccuracy: Int = 0
)

data class PomodoroState(
    val presetMinutes: Int = 25,
    val secondsRemaining: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0,
    val isBreakMode: Boolean = false
)

data class CalculatorState(
    val expression: String = "",
    val result: String = "0",
    val useDegrees: Boolean = true
)

data class AiTutorState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            id = "welcome",
            isUser = false,
            text = "Hello! I'm your JeetXToppers Pro AI Assistant. Switch to **Deep Solver** for tough JEE/NEET numericals & multi-step proofs, attach a question photo, or ask in English/Hinglish!"
        )
    ),
    val solveMode: AiSolveMode = AiSolveMode.DEEP_SOLVER,
    val subjectHint: String = "Auto",
    val streamingReply: String? = null,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)

data class JeetUiState(
    val primaryTab: PrimaryTab = PrimaryTab.HOME,
    val subDestination: SubDestination = SubDestination.NONE,
    val profileState: StreamState<UserProfile?> = StreamState.Loading,
    val notesState: StreamState<List<StudyNote>> = StreamState.Loading,
    val pdfsState: StreamState<List<PdfDocumentMeta>> = StreamState.Loading,
    val attemptsState: StreamState<List<TestAttempt>> = StreamState.Loading,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val activeTest: ActiveTestState? = null,
    val isGeneratingAiQuiz: Boolean = false,
    val pomodoro: PomodoroState = PomodoroState(),
    val calculator: CalculatorState = CalculatorState(),
    val aiTutor: AiTutorState = AiTutorState(),
    val statusMessage: String? = null,
    val libraryTrackFilter: String = "All",
    val librarySubjectFilter: String = "All"
)

class JeetViewModel(
    private val repository: JeetRepository,
    private val aiRepository: AiStudyRepository,
    private val themeStore: ThemePreferencesStore,
    private val appContext: Context,
    private val currentUserId: String,
    initialDisplayName: String?,
    initialEmail: String?
) : ViewModel() {

    private val _localState = MutableStateFlow(JeetUiState())
    private var timerJob: Job? = null
    private var testTimerJob: Job? = null

    private val profileStream = repository.observeProfile(currentUserId)
        .map<UserProfile?, StreamState<UserProfile?>> { StreamState.Success(it) }
        .catch { emit(StreamState.Error(unwrapFirestoreMessage(it, "Failed to load profile"))) }

    private val notesStream = repository.observeNotes(currentUserId)
        .map<List<StudyNote>, StreamState<List<StudyNote>>> { StreamState.Success(it) }
        .catch { emit(StreamState.Error(unwrapFirestoreMessage(it, "Failed to load notes"))) }

    private val pdfsStream = repository.observePdfs(currentUserId)
        .map<List<PdfDocumentMeta>, StreamState<List<PdfDocumentMeta>>> { StreamState.Success(it) }
        .catch { emit(StreamState.Error(unwrapFirestoreMessage(it, "Failed to load PDFs"))) }

    private val attemptsStream = repository.observeTestAttempts(currentUserId)
        .map<List<TestAttempt>, StreamState<List<TestAttempt>>> { StreamState.Success(it) }
        .catch { emit(StreamState.Error(unwrapFirestoreMessage(it, "Failed to load test history"))) }

    val uiState: StateFlow<JeetUiState> = combine(
        _localState,
        profileStream,
        notesStream,
        pdfsStream,
        attemptsStream,
        themeStore.themeModeFlow
    ) { values ->
        val local = values[0] as JeetUiState
        @Suppress("UNCHECKED_CAST")
        val profile = values[1] as StreamState<UserProfile?>
        @Suppress("UNCHECKED_CAST")
        val notes = values[2] as StreamState<List<StudyNote>>
        @Suppress("UNCHECKED_CAST")
        val pdfs = values[3] as StreamState<List<PdfDocumentMeta>>
        @Suppress("UNCHECKED_CAST")
        val attempts = values[4] as StreamState<List<TestAttempt>>
        val theme = values[5] as ThemeMode

        local.copy(
            profileState = profile,
            notesState = notes,
            pdfsState = pdfs,
            attemptsState = attempts,
            themeMode = theme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = JeetUiState()
    )

    init {
        viewModelScope.launch {
            val res = repository.ensureUserProfile(initialDisplayName, initialEmail)
            if (res.isFailure) {
                setStatus(res.exceptionOrNull()?.localizedMessage ?: "Failed to initialize profile")
            }
        }
    }

    private fun unwrapFirestoreMessage(error: Throwable, fallback: String): String {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w(TAG, fallback, root)
        return root.message ?: fallback
    }

    fun selectPrimaryTab(tab: PrimaryTab) {
        _localState.update { it.copy(primaryTab = tab, subDestination = SubDestination.NONE) }
    }

    fun openSubDestination(dest: SubDestination) {
        _localState.update { it.copy(subDestination = dest) }
    }

    fun closeSubDestination() {
        _localState.update { it.copy(subDestination = SubDestination.NONE) }
    }

    fun openTrackInLibrary(track: String) {
        _localState.update {
            it.copy(
                primaryTab = PrimaryTab.LIBRARY,
                subDestination = SubDestination.NONE,
                libraryTrackFilter = track,
                librarySubjectFilter = "All"
            )
        }
    }

    fun setLibraryFilters(track: String, subject: String) {
        _localState.update { it.copy(libraryTrackFilter = track, librarySubjectFilter = subject) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeStore.setThemeMode(mode)
        }
    }

    fun clearStatus() {
        _localState.update { it.copy(statusMessage = null) }
    }

    private fun setStatus(msg: String) {
        _localState.update { it.copy(statusMessage = msg) }
    }

    // --- ONBOARDING & PROFILE ---

    fun saveAcademicProfile(
        displayName: String,
        board: String,
        studentClass: String,
        stream: String,
        targetExam: String
    ) {
        viewModelScope.launch {
            val result = repository.saveAcademicProfile(
                displayName = displayName,
                board = board,
                studentClass = studentClass,
                stream = stream,
                targetExam = targetExam,
                onboardingCompleted = true
            )
            if (result.isSuccess) {
                setStatus("Academic profile saved!")
            } else {
                setStatus(result.exceptionOrNull()?.localizedMessage ?: "Could not save profile")
            }
        }
    }

    // --- NOTES ---

    fun saveNote(
        existingId: String?,
        title: String,
        subject: String,
        content: String,
        pinned: Boolean
    ) {
        viewModelScope.launch {
            val result = repository.saveNote(existingId, title, subject, content, pinned)
            if (result.isSuccess) {
                setStatus(if (existingId == null) "Note saved" else "Note updated")
            } else {
                setStatus(result.exceptionOrNull()?.localizedMessage ?: "Failed to save note")
            }
        }
    }

    fun toggleNotePinned(note: StudyNote) {
        viewModelScope.launch {
            val result = repository.toggleNotePinned(note.id, !note.pinned)
            if (result.isFailure) {
                setStatus(result.exceptionOrNull()?.localizedMessage ?: "Failed to update note")
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            val result = repository.deleteNote(noteId)
            if (result.isSuccess) {
                setStatus("Note deleted")
            } else {
                setStatus(result.exceptionOrNull()?.localizedMessage ?: "Failed to delete note")
            }
        }
    }

    fun saveClassNoteToMyNotes(template: ClassNoteTemplate) {
        viewModelScope.launch {
            val result = repository.saveNote(
                existingNoteId = null,
                title = "${template.classLevel}: ${template.chapterTitle}",
                subject = template.subject,
                content = template.toPlainNoteContent(),
                pinned = false
            )
            if (result.isSuccess) {
                setStatus("Saved '${template.chapterTitle}' to My Cloud Notes")
            } else {
                setStatus(result.exceptionOrNull()?.localizedMessage ?: "Failed to save note")
            }
        }
    }

    fun saveAiReplyAsNote(title: String, subject: String, content: String) {
        viewModelScope.launch {
            val cleanTitle = title.trim().ifBlank { "AI Solution Note" }.take(150)
            val cleanSubject = if (subject == "Auto") "Physics" else subject
            val result = repository.saveNote(
                existingNoteId = null,
                title = cleanTitle,
                subject = cleanSubject,
                content = content.take(9500),
                pinned = false
            )
            if (result.isSuccess) {
                setStatus("Saved AI solution to My Cloud Notes")
            } else {
                setStatus(result.exceptionOrNull()?.localizedMessage ?: "Failed to save AI solution")
            }
        }
    }

    fun askAiAboutTopic(prompt: String, subjectHint: String = "Auto", mode: AiSolveMode = AiSolveMode.DEEP_SOLVER) {
        _localState.update {
            it.copy(
                primaryTab = PrimaryTab.AI,
                subDestination = SubDestination.NONE,
                aiTutor = it.aiTutor.copy(solveMode = mode, subjectHint = subjectHint)
            )
        }
        sendAiMessage(prompt, attachedImage = null)
    }

    // --- PDF LIBRARY ---

    fun addPdfDocument(title: String, subject: String, uriString: String, pageCount: Long) {
        viewModelScope.launch {
            val result = repository.addPdfDocument(title, subject, uriString, pageCount)
            if (result.isSuccess) {
                setStatus("Added PDF: $title")
            } else {
                setStatus(result.exceptionOrNull()?.localizedMessage ?: "Failed to save PDF")
            }
        }
    }

    fun deletePdfDocument(pdfId: String) {
        viewModelScope.launch {
            val result = repository.deletePdfDocument(pdfId)
            if (result.isSuccess) {
                setStatus("Removed PDF from library")
            } else {
                setStatus(result.exceptionOrNull()?.localizedMessage ?: "Failed to remove PDF")
            }
        }
    }

    // --- PRACTICE & MOCK TEST ENGINE ---

    fun startCuratedQuiz(
        track: String,
        subject: String,
        isExamMode: Boolean,
        negativeMarking: Boolean
    ) {
        val filtered = StudyCatalog.questionBank.filter { q ->
            val trackMatch = track == "All" || q.track == track
            val subjectMatch = subject == "All" || q.subject == subject
            trackMatch && subjectMatch
        }.ifEmpty {
            StudyCatalog.questionBank.filter { subject == "All" || it.subject == subject }
        }.ifEmpty {
            StudyCatalog.questionBank
        }.shuffled()

        startTestWithQuestions(
            title = if (subject == "All") "$track Mock Test" else "$subject ($track)",
            track = if (track == "All") "Board Exams" else track,
            subject = if (subject == "All") "Mixed" else subject,
            questions = filtered,
            isExamMode = isExamMode,
            negativeMarking = negativeMarking
        )
    }

    fun generateAndStartAiQuiz(
        topic: String,
        subject: String,
        track: String,
        count: Int,
        isExamMode: Boolean,
        negativeMarking: Boolean
    ) {
        val cleanTopic = topic.trim()
        if (cleanTopic.isEmpty()) {
            setStatus("Enter a chapter or topic for AI question generation")
            return
        }
        viewModelScope.launch {
            _localState.update { it.copy(isGeneratingAiQuiz = true) }
            val result = aiRepository.generatePracticeQuestions(
                topic = cleanTopic,
                subject = subject,
                track = track,
                count = count
            )
            _localState.update { it.copy(isGeneratingAiQuiz = false) }
            result.onSuccess { questions ->
                startTestWithQuestions(
                    title = "AI Quiz: $cleanTopic",
                    track = "AI Generated",
                    subject = subject,
                    questions = questions,
                    isExamMode = isExamMode,
                    negativeMarking = negativeMarking
                )
            }.onFailure { err ->
                setStatus(err.localizedMessage ?: "AI quiz generation failed")
            }
        }
    }

    private fun startTestWithQuestions(
        title: String,
        track: String,
        subject: String,
        questions: List<QuizQuestion>,
        isExamMode: Boolean,
        negativeMarking: Boolean
    ) {
        testTimerJob?.cancel()
        val totalTime = questions.size * 60
        _localState.update {
            it.copy(
                primaryTab = PrimaryTab.PRACTICE,
                subDestination = SubDestination.NONE,
                activeTest = ActiveTestState(
                    title = title,
                    track = track,
                    subject = subject,
                    questions = questions,
                    isExamMode = isExamMode,
                    negativeMarking = negativeMarking,
                    secondsRemaining = totalTime,
                    elapsedSeconds = 0
                )
            )
        }
        testTimerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val current = _localState.value.activeTest ?: break
                if (current.isFinished) break
                val nextElapsed = current.elapsedSeconds + 1
                val nextRem = (current.secondsRemaining - 1).coerceAtLeast(0)
                _localState.update {
                    it.copy(
                        activeTest = it.activeTest?.copy(
                            elapsedSeconds = nextElapsed,
                            secondsRemaining = nextRem
                        )
                    )
                }
                if (current.isExamMode && nextRem == 0) {
                    submitActiveTest()
                    break
                }
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        _localState.update { state ->
            val active = state.activeTest ?: return@update state
            if (active.isFinished) return@update state
            val updatedMap = active.selectedAnswers.toMutableMap().apply {
                put(questionIndex, optionIndex)
            }
            state.copy(activeTest = active.copy(selectedAnswers = updatedMap))
        }
    }

    fun goToQuestion(index: Int) {
        _localState.update { state ->
            val active = state.activeTest ?: return@update state
            val clamped = index.coerceIn(0, (active.questions.size - 1).coerceAtLeast(0))
            state.copy(activeTest = active.copy(currentIndex = clamped))
        }
    }

    fun submitActiveTest() {
        val active = _localState.value.activeTest ?: return
        if (active.isFinished) return
        testTimerJob?.cancel()

        var correct = 0
        var wrong = 0
        active.questions.forEachIndexed { idx, q ->
            val selected = active.selectedAnswers[idx]
            if (selected != null) {
                if (selected == q.correctIndex) correct++ else wrong++
            }
        }
        val maxScore = active.questions.size * 4
        val rawScore = if (active.negativeMarking) {
            (correct * 4) - wrong
        } else {
            correct * 4
        }
        val accuracy = if (active.questions.isNotEmpty()) {
            ((correct.toDouble() / active.questions.size.toDouble()) * 100.0).toInt()
        } else 0

        _localState.update {
            it.copy(
                activeTest = active.copy(
                    isFinished = true,
                    lastScore = rawScore,
                    lastMaxScore = maxScore,
                    lastCorrectCount = correct,
                    lastAccuracy = accuracy
                )
            )
        }

        val currentProfile = (uiState.value.profileState as? StreamState.Success)?.data
        viewModelScope.launch {
            val res = repository.recordTestAttempt(
                title = active.title,
                track = active.track,
                subject = active.subject,
                totalQuestions = active.questions.size,
                correctCount = correct,
                score = rawScore,
                maxScore = maxScore,
                durationSeconds = active.elapsedSeconds,
                currentProfile = currentProfile
            )
            if (res.isFailure) {
                setStatus(res.exceptionOrNull()?.localizedMessage ?: "Failed to sync test result")
            }
        }
    }

    fun exitActiveTest() {
        testTimerJob?.cancel()
        _localState.update { it.copy(activeTest = null) }
    }

    // --- AI STUDY ASSISTANT ---

    fun setAiSolveMode(mode: AiSolveMode) {
        _localState.update { it.copy(aiTutor = it.aiTutor.copy(solveMode = mode)) }
    }

    fun setAiSubjectHint(subject: String) {
        _localState.update { it.copy(aiTutor = it.aiTutor.copy(subjectHint = subject)) }
    }

    fun sendAiMessage(prompt: String, attachedImage: Bitmap? = null) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty() && attachedImage == null) return
        val effectivePrompt = cleanPrompt.ifEmpty {
            "Carefully read the entire problem shown in this image (including all numbers, units, diagrams, and options) and solve it step by step with verification."
        }

        val userMsg = ChatMessage(
            id = "u_${System.currentTimeMillis()}",
            isUser = true,
            text = effectivePrompt,
            hasImage = attachedImage != null
        )

        val currentTutor = _localState.value.aiTutor
        val historyPairs = currentTutor.messages
            .filter { it.id != "welcome" }
            .map { it.isUser to it.text }

        val profile = (uiState.value.profileState as? StreamState.Success)?.data
        val studentContext = if (profile != null) {
            "${profile.board}, ${profile.studentClass}, ${profile.stream}, Target: ${profile.targetExam}"
        } else {
            "Indian Board / Competitive Exam Student"
        }

        _localState.update {
            it.copy(
                aiTutor = it.aiTutor.copy(
                    messages = it.aiTutor.messages + userMsg,
                    streamingReply = "",
                    isGenerating = true,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            var latestReply = ""
            try {
                aiRepository.streamTutorResponse(
                    historyMessages = historyPairs,
                    userPrompt = effectivePrompt,
                    attachedImage = attachedImage,
                    studentContext = studentContext,
                    solveMode = currentTutor.solveMode,
                    subjectHint = currentTutor.subjectHint
                ).collect { partial ->
                    latestReply = partial
                    _localState.update {
                        it.copy(
                            aiTutor = it.aiTutor.copy(
                                streamingReply = partial,
                                isGenerating = true
                            )
                        )
                    }
                }
                val finalMsg = ChatMessage(
                    id = "a_${System.currentTimeMillis()}",
                    isUser = false,
                    text = latestReply.ifBlank { "I could not generate a response. Please try again." }
                )
                _localState.update {
                    it.copy(
                        aiTutor = it.aiTutor.copy(
                            messages = it.aiTutor.messages + finalMsg,
                            streamingReply = null,
                            isGenerating = false
                        )
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI Tutor timed out", e)
                _localState.update {
                    it.copy(
                        aiTutor = it.aiTutor.copy(
                            streamingReply = null,
                            isGenerating = false,
                            errorMessage = "Response timed out after 60 seconds. Please try a shorter query."
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Tutor error", e)
                _localState.update {
                    it.copy(
                        aiTutor = it.aiTutor.copy(
                            streamingReply = null,
                            isGenerating = false,
                            errorMessage = e.localizedMessage ?: "AI Assistant failed to respond."
                        )
                    )
                }
            }
        }
    }

    fun clearAiChat() {
        _localState.update { state ->
            val currentMode = state.aiTutor.solveMode
            val currentSubj = state.aiTutor.subjectHint
            state.copy(
                aiTutor = AiTutorState(
                    solveMode = currentMode,
                    subjectHint = currentSubj
                )
            )
        }
    }

    // --- POMODORO TIMER ---

    fun setPomodoroPreset(minutes: Int, isBreak: Boolean) {
        timerJob?.cancel()
        _localState.update {
            it.copy(
                pomodoro = it.pomodoro.copy(
                    presetMinutes = minutes,
                    secondsRemaining = minutes * 60,
                    isRunning = false,
                    isBreakMode = isBreak
                )
            )
        }
    }

    fun togglePomodoroRunning() {
        val current = _localState.value.pomodoro
        if (current.isRunning) {
            timerJob?.cancel()
            _localState.update { it.copy(pomodoro = it.pomodoro.copy(isRunning = false)) }
        } else {
            _localState.update { it.copy(pomodoro = it.pomodoro.copy(isRunning = true)) }
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1_000L)
                    val p = _localState.value.pomodoro
                    if (!p.isRunning) break
                    if (p.secondsRemaining <= 1) {
                        FocusFeedback.pulseCompletion(appContext)
                        val newCount = if (!p.isBreakMode) p.completedSessions + 1 else p.completedSessions
                        _localState.update {
                            it.copy(
                                pomodoro = p.copy(
                                    secondsRemaining = p.presetMinutes * 60,
                                    isRunning = false,
                                    completedSessions = newCount
                                ),
                                statusMessage = if (p.isBreakMode) "Break complete! Back to focus." else "Focus session complete! Great work."
                            )
                        }
                        break
                    } else {
                        _localState.update {
                            it.copy(pomodoro = p.copy(secondsRemaining = p.secondsRemaining - 1))
                        }
                    }
                }
            }
        }
    }

    fun resetPomodoro() {
        timerJob?.cancel()
        _localState.update {
            val preset = it.pomodoro.presetMinutes
            it.copy(
                pomodoro = it.pomodoro.copy(
                    secondsRemaining = preset * 60,
                    isRunning = false
                )
            )
        }
    }

    // --- SCIENTIFIC CALCULATOR ---

    fun onCalculatorInput(token: String) {
        _localState.update { state ->
            val calc = state.calculator
            when (token) {
                "AC" -> state.copy(calculator = calc.copy(expression = "", result = "0"))
                "DEL" -> {
                    val nextExpr = if (calc.expression.isNotEmpty()) calc.expression.dropLast(1) else ""
                    state.copy(calculator = calc.copy(expression = nextExpr))
                }
                "=" -> {
                    val evaluated = ScientificMathEvaluator.evaluate(calc.expression, calc.useDegrees)
                    state.copy(calculator = calc.copy(result = evaluated))
                }
                "DEG_RAD" -> {
                    val nextDeg = !calc.useDegrees
                    val nextRes = if (calc.expression.isNotBlank()) {
                        ScientificMathEvaluator.evaluate(calc.expression, nextDeg)
                    } else calc.result
                    state.copy(calculator = calc.copy(useDegrees = nextDeg, result = nextRes))
                }
                else -> {
                    val nextExpr = calc.expression + token
                    state.copy(calculator = calc.copy(expression = nextExpr))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        testTimerJob?.cancel()
    }

    private companion object {
        const val TAG = "JeetViewModel"
    }
}
