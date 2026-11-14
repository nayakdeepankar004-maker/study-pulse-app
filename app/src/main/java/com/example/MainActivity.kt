package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.audio.StudySoundPlayer
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.remote.FirestoreStudyRepository
import com.example.model.StudyTab
import com.example.ui.StudyPulseUiState
import com.example.ui.StudyPulseViewModel
import com.example.ui.components.AccountProfileDialog
import com.example.ui.components.AlarmRingDialog
import com.example.ui.components.CelebrationOverlay
import com.example.ui.components.StreamAndSubjectSelectorCard
import com.example.ui.components.StudyPulseTopBar
import com.example.ui.components.TaskEditorDialog
import com.example.ui.screens.AlarmsSection
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalendarSection
import com.example.ui.screens.DashboardSection
import com.example.ui.screens.GoalsSection
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.MidnightHeader
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseBlueLight
import com.example.ui.theme.PulsePurple
import com.example.ui.theme.PulseRose
import com.example.ui.theme.StudyPulseTheme
import com.example.ui.theme.TextMuted
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            StudyPulseTheme {
                StudyPulseAuthGate()
            }
        }
    }
}

@Composable
fun StudyPulseAuthGate() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(
            onAuthSuccess = { /* authStateFlow updates reactively */ }
        )
    } else {
        key(user.uid) {
            AuthenticatedStudyPulseApp(
                currentUserId = user.uid,
                initialDisplayName = user.displayName,
                initialEmail = user.email
            )
        }
    }
}

@Composable
private fun AuthenticatedStudyPulseApp(
    currentUserId: String,
    initialDisplayName: String?,
    initialEmail: String?
) {
    val viewModel: StudyPulseViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val repository = FirestoreStudyRepository(db)
                val soundPlayer = StudySoundPlayer(app)
                StudyPulseViewModel(
                    repository = repository,
                    soundPlayer = soundPlayer,
                    currentUserId = currentUserId,
                    initialDisplayName = initialDisplayName,
                    initialEmail = initialEmail
                )
            }
        }
    )

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val handleSignOut: () -> Unit = {
        viewModel.closeAccountDialog()
        signOut(
            credentialManager = credentialManager,
            scope = coroutineScope
        )
    }

    StudyPulseApp(
        viewModel = viewModel,
        onSignOut = handleSignOut
    )
}

@Composable
fun StudyPulseApp(
    viewModel: StudyPulseViewModel,
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.currentTab != StudyTab.DASHBOARD &&
        !uiState.isTaskModalOpen &&
        uiState.ringingTask == null &&
        !uiState.isAccountDialogOpen
    ) {
        BackHandler {
            viewModel.selectTab(StudyTab.DASHBOARD)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MidnightBg,
                        Color(0xFF0A1024),
                        MidnightBg
                    )
                )
            )
    ) {
        val useNavigationRail = maxWidth >= 600.dp

        if (useNavigationRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                StudyPulseNavigationRail(
                    currentTab = uiState.currentTab,
                    onSelectTab = viewModel::selectTab
                )
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        StudyPulseTopBar(
                            headerDate = uiState.headerDateDisplay,
                            streakDays = uiState.preferences.streak,
                            onTestAlarmClick = viewModel::triggerTestAlarm,
                            onAccountClick = viewModel::openAccountDialog
                        )
                    }
                ) { innerPadding ->
                    MainScrollableBody(
                        uiState = uiState,
                        viewModel = viewModel,
                        onSignOut = onSignOut,
                        modifier = Modifier
                            .padding(innerPadding)
                            .navigationBarsPadding()
                    )
                }
            }
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    StudyPulseTopBar(
                        headerDate = uiState.headerDateDisplay,
                        streakDays = uiState.preferences.streak,
                        onTestAlarmClick = viewModel::triggerTestAlarm,
                        onAccountClick = viewModel::openAccountDialog
                    )
                },
                bottomBar = {
                    StudyPulseBottomBar(
                        currentTab = uiState.currentTab,
                        onSelectTab = viewModel::selectTab
                    )
                }
            ) { innerPadding ->
                MainScrollableBody(
                    uiState = uiState,
                    viewModel = viewModel,
                    onSignOut = onSignOut,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        CelebrationOverlay(
            celebrationTitle = uiState.celebrationTitle,
            celebrationNonce = uiState.celebrationNonce,
            onBannerFinished = viewModel::clearCelebrationBanner
        )
    }

    if (uiState.isAccountDialogOpen) {
        AccountProfileDialog(
            displayName = uiState.accountDisplayName,
            email = uiState.accountEmail,
            streamLabel = uiState.preferences.selectedStream.batchName,
            onSignOut = onSignOut,
            onDismiss = viewModel::closeAccountDialog
        )
    }

    if (uiState.isTaskModalOpen) {
        TaskEditorDialog(
            selectedStream = uiState.preferences.selectedStream,
            selectedSubjectFilter = uiState.preferences.selectedSubjectFilter,
            editingTask = uiState.editingTask,
            onDismiss = viewModel::closeTaskModal,
            onSave = viewModel::saveTask
        )
    }

    val ringing = uiState.ringingTask
    if (ringing != null) {
        AlarmRingDialog(
            stream = uiState.preferences.selectedStream,
            task = ringing,
            onMarkCompleted = viewModel::completeFromRingingAlarm,
            onSnooze5m = { viewModel.snoozeRingingAlarm(5) },
            onSnooze10m = { viewModel.snoozeRingingAlarm(10) },
            onDismiss = viewModel::dismissRingingAlarm
        )
    }
}

@Composable
private fun MainScrollableBody(
    uiState: StudyPulseUiState,
    viewModel: StudyPulseViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (!uiState.syncErrorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PulseRose.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, PulseRose.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("sync_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = PulseRose,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = uiState.syncErrorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFDA4AF)
                            )
                        }
                        TextButton(onClick = viewModel::clearSyncError) {
                            Text("Dismiss", color = Color(0xFFFDA4AF))
                        }
                    }
                }
            }

            StreamAndSubjectSelectorCard(
                selectedStream = uiState.preferences.selectedStream,
                selectedSubjectFilter = uiState.preferences.selectedSubjectFilter,
                onSelectStream = viewModel::selectStream,
                onSelectSubjectFilter = viewModel::selectSubjectFilter
            )

            Spacer(modifier = Modifier.height(14.dp))

            Crossfade(
                targetState = uiState.currentTab,
                animationSpec = tween(durationMillis = 200),
                label = "study_tab_crossfade"
            ) { tab ->
                when (tab) {
                    StudyTab.DASHBOARD -> DashboardSection(
                        uiState = uiState,
                        onOpenNewTask = viewModel::openNewTaskModal,
                        onToggleTask = viewModel::toggleTaskCompleted,
                        onEditTask = viewModel::openEditTaskModal,
                        onDeleteTask = viewModel::deleteTask
                    )
                    StudyTab.ALARMS -> AlarmsSection(
                        uiState = uiState,
                        onSelectFilter = viewModel::selectTaskFilter,
                        onOpenNewTask = viewModel::openNewTaskModal,
                        onToggleTask = viewModel::toggleTaskCompleted,
                        onEditTask = viewModel::openEditTaskModal,
                        onDeleteTask = viewModel::deleteTask
                    )
                    StudyTab.GOALS -> GoalsSection(
                        uiState = uiState,
                        onUpdateTargetLectures = viewModel::updateTargetLectures,
                        onUpdateTargetHours = viewModel::updateTargetHours,
                        onAdjustLoggedHours = viewModel::adjustLoggedHours,
                        onAddChapterGoal = viewModel::addChapterGoal,
                        onToggleGoal = viewModel::toggleGoalCompleted,
                        onDeleteGoal = viewModel::deleteGoal
                    )
                    StudyTab.CALENDAR -> CalendarSection(
                        uiState = uiState,
                        onSelectCalendarDate = viewModel::selectCalendarDate,
                        onSelectAlarmTone = viewModel::selectAlarmTone,
                        onToggleTask = viewModel::toggleTaskCompleted,
                        onEditTask = viewModel::openEditTaskModal,
                        onDeleteTask = viewModel::deleteTask,
                        onSignOut = onSignOut
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StudyPulseBottomBar(
    currentTab: StudyTab,
    onSelectTab: (StudyTab) -> Unit
) {
    NavigationBar(
        containerColor = MidnightHeader.copy(alpha = 0.96f),
        contentColor = TextMuted,
        tonalElevation = 0.dp
    ) {
        StudyTab.entries.forEach { tab ->
            val selected = tab == currentTab
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                modifier = Modifier.testTag("nav_tab_${tab.route}"),
                icon = {
                    Icon(
                        imageVector = tabIcon(tab, selected),
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PulseBlueLight,
                    selectedTextColor = PulseBlueLight,
                    indicatorColor = PulseBlue.copy(alpha = 0.18f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

@Composable
private fun StudyPulseNavigationRail(
    currentTab: StudyTab,
    onSelectTab: (StudyTab) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MidnightHeader.copy(alpha = 0.96f),
        contentColor = TextMuted
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        StudyTab.entries.forEach { tab ->
            val selected = tab == currentTab
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                modifier = Modifier.testTag("nav_rail_${tab.route}"),
                icon = {
                    Icon(
                        imageVector = tabIcon(tab, selected),
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = PulseBlueLight,
                    selectedTextColor = PulseBlueLight,
                    indicatorColor = PulsePurple.copy(alpha = 0.22f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

private fun tabIcon(tab: StudyTab, selected: Boolean) = when (tab) {
    StudyTab.DASHBOARD -> if (selected) Icons.Rounded.Bolt else Icons.Outlined.Bolt
    StudyTab.ALARMS -> if (selected) Icons.Rounded.Alarm else Icons.Outlined.Alarm
    StudyTab.GOALS -> if (selected) Icons.Rounded.TrackChanges else Icons.Outlined.TrackChanges
    StudyTab.CALENDAR -> if (selected) Icons.Rounded.CalendarMonth else Icons.Outlined.CalendarMonth
}
