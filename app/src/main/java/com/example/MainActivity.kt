package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.local.ThemeMode
import com.example.data.local.ThemePreferencesStore
import com.example.data.remote.AiStudyRepository
import com.example.data.remote.JeetRepository
import com.example.firebase.configureAppCheck
import com.example.ui.JeetUiState
import com.example.ui.JeetViewModel
import com.example.ui.PrimaryTab
import com.example.ui.StreamState
import com.example.ui.SubDestination
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PdfLibraryScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.JeetXToppersTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or ViewModel creation.
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themeStore = remember(context) { ThemePreferencesStore(context.applicationContext) }
            val themeMode by themeStore.themeModeFlow.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

            JeetXToppersTheme(themeMode = themeMode) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    JeetAuthGate(themeStore = themeStore)
                }
            }
        }
    }
}

@Composable
fun JeetAuthGate(themeStore: ThemePreferencesStore) {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(
            onAuthSuccess = { /* authStateFlow updates reactively */ }
        )
    } else {
        key(user.uid) {
            AuthenticatedJeetApp(
                currentUserId = user.uid,
                initialDisplayName = user.displayName,
                initialEmail = user.email,
                themeStore = themeStore
            )
        }
    }
}

@Composable
private fun AuthenticatedJeetApp(
    currentUserId: String,
    initialDisplayName: String?,
    initialEmail: String?,
    themeStore: ThemePreferencesStore
) {
    val viewModel: JeetViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                JeetViewModel(
                    repository = JeetRepository(db),
                    aiRepository = AiStudyRepository(),
                    themeStore = themeStore,
                    appContext = app,
                    currentUserId = currentUserId,
                    initialDisplayName = initialDisplayName,
                    initialEmail = initialEmail
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val handleSignOut: () -> Unit = {
        signOut(
            credentialManager = credentialManager,
            scope = scope
        )
    }

    when (val pState = uiState.profileState) {
        is StreamState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
        is StreamState.Error -> {
            JeetMainScaffold(
                uiState = uiState,
                viewModel = viewModel,
                onSignOut = handleSignOut
            )
        }
        is StreamState.Success -> {
            val profile = pState.data
            if (profile != null && !profile.onboardingCompleted) {
                OnboardingScreen(
                    profile = profile,
                    onComplete = { name, board, studentClass, stream, exam ->
                        viewModel.saveAcademicProfile(name, board, studentClass, stream, exam)
                    }
                )
            } else {
                JeetMainScaffold(
                    uiState = uiState,
                    viewModel = viewModel,
                    onSignOut = handleSignOut
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JeetMainScaffold(
    uiState: JeetUiState,
    viewModel: JeetViewModel,
    onSignOut: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val profile = (uiState.profileState as? StreamState.Success)?.data
    val notes = (uiState.notesState as? StreamState.Success)?.data.orEmpty()
    val pdfs = (uiState.pdfsState as? StreamState.Success)?.data.orEmpty()
    val attempts = (uiState.attemptsState as? StreamState.Success)?.data.orEmpty()

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatus()
        }
    }

    if (uiState.subDestination != SubDestination.NONE) {
        BackHandler {
            viewModel.closeSubDestination()
        }
    } else if (uiState.activeTest != null) {
        BackHandler {
            viewModel.exitActiveTest()
        }
    } else if (uiState.primaryTab != PrimaryTab.HOME) {
        BackHandler {
            viewModel.selectPrimaryTab(PrimaryTab.HOME)
        }
    }

    val topBarTitle = when (uiState.subDestination) {
        SubDestination.NOTES -> "Class & My Notes"
        SubDestination.PDF_LIBRARY -> "PDF Library"
        SubDestination.PROFILE -> "Profile"
        SubDestination.SETTINGS -> "Settings"
        SubDestination.NONE -> stringResource(R.string.brand_title)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        if (useNavigationRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryTab.entries.forEach { tab ->
                        val selected = uiState.subDestination == SubDestination.NONE && uiState.primaryTab == tab
                        NavigationRailItem(
                            selected = selected,
                            onClick = { viewModel.selectPrimaryTab(tab) },
                            icon = { Icon(tabIcon(tab, selected), contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag("nav_rail_${tab.route}")
                        )
                    }
                }
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        JeetTopBar(
                            title = topBarTitle,
                            initialLetter = profile?.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "S",
                            onOpenSettings = { viewModel.openSubDestination(SubDestination.SETTINGS) },
                            onOpenProfile = { viewModel.openSubDestination(SubDestination.PROFILE) }
                        )
                    }
                ) { innerPadding ->
                    JeetScrollBody(
                        uiState = uiState,
                        viewModel = viewModel,
                        profile = profile,
                        notes = notes,
                        pdfs = pdfs,
                        attempts = attempts,
                        onSignOut = onSignOut,
                        modifier = Modifier
                            .padding(innerPadding)
                            .navigationBarsPadding()
                    )
                }
            }
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    JeetTopBar(
                        title = topBarTitle,
                        initialLetter = profile?.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "S",
                        onOpenSettings = { viewModel.openSubDestination(SubDestination.SETTINGS) },
                        onOpenProfile = { viewModel.openSubDestination(SubDestination.PROFILE) }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        PrimaryTab.entries.forEach { tab ->
                            val selected = uiState.subDestination == SubDestination.NONE && uiState.primaryTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectPrimaryTab(tab) },
                                icon = { Icon(tabIcon(tab, selected), contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                JeetScrollBody(
                    uiState = uiState,
                    viewModel = viewModel,
                    profile = profile,
                    notes = notes,
                    pdfs = pdfs,
                    attempts = attempts,
                    onSignOut = onSignOut,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JeetTopBar(
    title: String,
    initialLetter: String,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.brand_tagline),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("topbar_settings_button")
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings")
            }
            IconButton(
                onClick = onOpenProfile,
                modifier = Modifier.testTag("topbar_profile_button")
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initialLetter,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun JeetScrollBody(
    uiState: JeetUiState,
    viewModel: JeetViewModel,
    profile: com.example.data.remote.UserProfile?,
    notes: List<com.example.data.remote.StudyNote>,
    pdfs: List<com.example.data.remote.PdfDocumentMeta>,
    attempts: List<com.example.data.remote.TestAttempt>,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.subDestination != SubDestination.NONE) {
                when (uiState.subDestination) {
                    SubDestination.NOTES -> NotesScreen(
                        notes = notes,
                        defaultStudentClass = profile?.studentClass,
                        onBack = viewModel::closeSubDestination,
                        onSaveNote = viewModel::saveNote,
                        onSaveClassNote = viewModel::saveClassNoteToMyNotes,
                        onTogglePin = viewModel::toggleNotePinned,
                        onDeleteNote = viewModel::deleteNote,
                        onAskAiExplain = { note ->
                            viewModel.askAiAboutTopic(
                                prompt = "Summarize this ${note.subject} note '${note.title}' and give me 2 challenging practice questions with solutions based on it:\n\n${note.content}",
                                subjectHint = note.subject
                            )
                        },
                        onAskAiClassNote = { clsNote ->
                            viewModel.askAiAboutTopic(
                                prompt = "Explain '${clsNote.chapterTitle}' (${clsNote.classLevel}, ${clsNote.subject}, ${clsNote.track}) clearly with 1 solved difficult numerical/application problem.",
                                subjectHint = clsNote.subject
                            )
                        }
                    )
                    SubDestination.PDF_LIBRARY -> PdfLibraryScreen(
                        pdfs = pdfs,
                        onBack = viewModel::closeSubDestination,
                        onAddPdf = viewModel::addPdfDocument,
                        onDeletePdf = viewModel::deletePdfDocument
                    )
                    SubDestination.PROFILE -> ProfileScreen(
                        profile = profile,
                        onBack = viewModel::closeSubDestination,
                        onSaveProfile = viewModel::saveAcademicProfile,
                        onSignOut = onSignOut
                    )
                    SubDestination.SETTINGS -> SettingsScreen(
                        themeMode = uiState.themeMode,
                        onBack = viewModel::closeSubDestination,
                        onSelectTheme = viewModel::setThemeMode
                    )
                    SubDestination.NONE -> Unit
                }
            } else {
                Crossfade(
                    targetState = uiState.primaryTab,
                    animationSpec = tween(durationMillis = 180),
                    label = "primary_tab_crossfade"
                ) { tab ->
                    when (tab) {
                        PrimaryTab.HOME -> DashboardScreen(
                            profile = profile,
                            notesCount = notes.size,
                            pdfsCount = pdfs.size,
                            attempts = attempts,
                            onNavigateTab = viewModel::selectPrimaryTab,
                            onOpenSubDestination = viewModel::openSubDestination,
                            onOpenTrack = viewModel::openTrackInLibrary
                        )
                        PrimaryTab.LIBRARY -> LibraryScreen(
                            selectedTrack = uiState.libraryTrackFilter,
                            selectedSubject = uiState.librarySubjectFilter,
                            notesCount = notes.size,
                            pdfsCount = pdfs.size,
                            onFilterChange = viewModel::setLibraryFilters,
                            onOpenSubDestination = viewModel::openSubDestination,
                            onStartChapterQuiz = { track, subject ->
                                viewModel.startCuratedQuiz(track, subject, isExamMode = false, negativeMarking = true)
                            },
                            onAskAiChapter = { ch ->
                                viewModel.askAiAboutTopic(
                                    prompt = "Explain '${ch.title}' (${ch.subject}, ${ch.track}) with key formulas and one solved difficult problem.",
                                    subjectHint = ch.subject
                                )
                            }
                        )
                        PrimaryTab.PRACTICE -> PracticeScreen(
                            activeTest = uiState.activeTest,
                            isGeneratingAiQuiz = uiState.isGeneratingAiQuiz,
                            recentAttempts = attempts,
                            onStartCuratedQuiz = viewModel::startCuratedQuiz,
                            onGenerateAiQuiz = viewModel::generateAndStartAiQuiz,
                            onSelectAnswer = viewModel::selectAnswer,
                            onGoToQuestion = viewModel::goToQuestion,
                            onSubmitTest = viewModel::submitActiveTest,
                            onExitTest = viewModel::exitActiveTest
                        )
                        PrimaryTab.AI -> AiAssistantScreen(
                            aiState = uiState.aiTutor,
                            onSelectMode = viewModel::setAiSolveMode,
                            onSelectSubjectHint = viewModel::setAiSubjectHint,
                            onSendMessage = viewModel::sendAiMessage,
                            onSaveSolutionAsNote = viewModel::saveAiReplyAsNote,
                            onClearChat = viewModel::clearAiChat
                        )
                        PrimaryTab.TOOLS -> ToolsScreen(
                            pomodoro = uiState.pomodoro,
                            calculator = uiState.calculator,
                            onSelectPreset = viewModel::setPomodoroPreset,
                            onToggleTimer = viewModel::togglePomodoroRunning,
                            onResetTimer = viewModel::resetPomodoro,
                            onCalculatorInput = viewModel::onCalculatorInput
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun tabIcon(tab: PrimaryTab, selected: Boolean) = when (tab) {
    PrimaryTab.HOME -> if (selected) Icons.Rounded.Home else Icons.Outlined.Home
    PrimaryTab.LIBRARY -> if (selected) Icons.Rounded.MenuBook else Icons.Outlined.MenuBook
    PrimaryTab.PRACTICE -> if (selected) Icons.Rounded.Quiz else Icons.Outlined.Quiz
    PrimaryTab.AI -> if (selected) Icons.Rounded.AutoAwesome else Icons.Outlined.AutoAwesome
    PrimaryTab.TOOLS -> if (selected) Icons.Rounded.Build else Icons.Outlined.Build
}
