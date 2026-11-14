package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ChapterGoal
import com.example.model.GoalSpan
import com.example.model.StudyCurriculum
import com.example.ui.StudyPulseUiState
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.CardBorderDone
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MidnightInputBg
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceHigh
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseBlueLight
import com.example.ui.theme.PulseBlueSoft
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.PulseEmeraldDark
import com.example.ui.theme.PulseEmeraldLight
import com.example.ui.theme.PulsePurple
import com.example.ui.theme.PulseRoseLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GoalsSection(
    uiState: StudyPulseUiState,
    onUpdateTargetLectures: (Int) -> Unit,
    onUpdateTargetHours: (Int) -> Unit,
    onAdjustLoggedHours: (Float) -> Unit,
    onAddChapterGoal: (String, GoalSpan, String) -> Unit,
    onToggleGoal: (ChapterGoal) -> Unit,
    onDeleteGoal: (String) -> Unit
) {
    val prefs = uiState.preferences
    val subjects = StudyCurriculum.subjectsFor(prefs.selectedStream)

    var goalTitle by remember { mutableStateOf("") }
    var goalSpan by remember { mutableStateOf(GoalSpan.TODAY) }
    var goalSubject by remember(prefs.selectedStream) {
        mutableStateOf(subjects.firstOrNull()?.name ?: "Physics")
    }
    var showGoalError by remember { mutableStateOf(false) }
    var showSpanMenu by remember { mutableStateOf(false) }
    var showSubjectMenu by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Daily Study Target Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MidnightSurface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, CardBorderBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrackChanges,
                                contentDescription = null,
                                tint = PulseBlueLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Daily Study Target",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Track daily lectures & self-study hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = PulseBlue.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, PulseBlue)
                    ) {
                        Text(
                            text = "${uiState.dailyGoalPercent}% Done",
                            style = MaterialTheme.typography.labelMedium,
                            color = PulseBlueSoft,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TargetStepperBox(
                        label = "TARGET LECTURES",
                        value = prefs.dailyTargetLectures,
                        subText = "Done: ${uiState.todayDoneCount}",
                        subColor = PulseEmeraldLight,
                        onDecrement = { onUpdateTargetLectures((prefs.dailyTargetLectures - 1).coerceAtLeast(1)) },
                        onIncrement = { onUpdateTargetLectures((prefs.dailyTargetLectures + 1).coerceAtMost(20)) },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "target_lectures"
                    )

                    val hoursStr = if (prefs.loggedHoursToday % 1f == 0f) {
                        "${prefs.loggedHoursToday.toInt()}h"
                    } else {
                        "${prefs.loggedHoursToday}h"
                    }
                    TargetStepperBox(
                        label = "TARGET HOURS",
                        value = prefs.dailyTargetHours,
                        subText = "Logged: $hoursStr",
                        subColor = PulseBlueLight,
                        onDecrement = { onUpdateTargetHours((prefs.dailyTargetHours - 1).coerceAtLeast(1)) },
                        onIncrement = { onUpdateTargetHours((prefs.dailyTargetHours + 1).coerceAtMost(18)) },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "target_hours"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CardBorderSubtle)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log self-study hours:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        HourLogChip(
                            label = "-0.5h",
                            containerColor = MidnightSurfaceHigh,
                            textColor = TextSecondary,
                            onClick = { onAdjustLoggedHours(-0.5f) },
                            testTag = "log_hours_minus_half"
                        )
                        HourLogChip(
                            label = "+0.5h",
                            containerColor = PulseBlue,
                            textColor = Color.White,
                            onClick = { onAdjustLoggedHours(0.5f) },
                            testTag = "log_hours_plus_half"
                        )
                        HourLogChip(
                            label = "+1h",
                            containerColor = PulsePurple,
                            textColor = Color.White,
                            onClick = { onAdjustLoggedHours(1f) },
                            testTag = "log_hours_plus_one"
                        )
                    }
                }
            }
        }

        // Add Chapter / Multi-Day Goal Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MidnightSurface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, CardBorderBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Chapter / Multi-Day Goal",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = {
                        goalTitle = it
                        if (showGoalError && it.isNotBlank()) showGoalError = false
                    },
                    placeholder = {
                        Text("e.g., Complete Rotational Motion + 50 PYQs", color = TextMuted)
                    },
                    isError = showGoalError,
                    supportingText = if (showGoalError) {
                        { Text("Please enter a chapter goal") }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MidnightInputBg,
                        unfocusedContainerColor = MidnightInputBg,
                        focusedBorderColor = PulseBlue,
                        unfocusedBorderColor = MidnightSurfaceHigh,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        GoalDropdownBox(
                            text = goalSpan.label,
                            onClick = { showSpanMenu = true },
                            testTag = "goal_span_selector"
                        )
                        DropdownMenu(
                            expanded = showSpanMenu,
                            onDismissRequest = { showSpanMenu = false }
                        ) {
                            GoalSpan.entries.forEach { span ->
                                DropdownMenuItem(
                                    text = { Text(span.label) },
                                    onClick = {
                                        goalSpan = span
                                        showSpanMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        GoalDropdownBox(
                            text = goalSubject,
                            onClick = { showSubjectMenu = true },
                            testTag = "goal_subject_selector"
                        )
                        DropdownMenu(
                            expanded = showSubjectMenu,
                            onDismissRequest = { showSubjectMenu = false }
                        ) {
                            subjects.forEach { spec ->
                                DropdownMenuItem(
                                    text = { Text(spec.name) },
                                    onClick = {
                                        goalSubject = spec.name
                                        showSubjectMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(PulseBlue, PulsePurple)))
                        .testTag("save_goal_button")
                        .clickable {
                            if (goalTitle.isBlank()) {
                                showGoalError = true
                            } else {
                                onAddChapterGoal(goalTitle, goalSpan, goalSubject)
                                goalTitle = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Save Chapter Goal",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Chapter Targets List
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = PulseEmeraldLight,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Your Chapter Targets (Tap to Complete)",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        if (uiState.goals.isEmpty()) {
            EmptyStudyIllustrationCard(
                message = "No chapter goals yet. Add a multi-day revision or PYQ target above!"
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.goals.forEach { goal ->
                    ChapterGoalCard(
                        goal = goal,
                        onToggle = { onToggleGoal(goal) },
                        onDelete = { onDeleteGoal(goal.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetStepperBox(
    label: String,
    value: Int,
    subText: String,
    subColor: Color,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MidnightInputBg,
        border = BorderStroke(1.dp, MidnightSurfaceHigh)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MidnightSurfaceHigh,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("${testTagPrefix}_dec")
                        .clickable(onClick = onDecrement)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease $label",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MidnightSurfaceHigh,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("${testTagPrefix}_inc")
                        .clickable(onClick = onIncrement)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase $label",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subText,
                style = MaterialTheme.typography.labelMedium,
                color = subColor
            )
        }
    }
}

@Composable
private fun HourLogChip(
    label: String,
    containerColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun GoalDropdownBox(
    text: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MidnightInputBg,
        border = BorderStroke(1.dp, MidnightSurfaceHigh),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ChapterGoalCard(
    goal: ChapterGoal,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (goal.done) 0.80f else 1f)
            .clip(RoundedCornerShape(18.dp))
            .testTag("goal_card_${goal.id}")
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(18.dp),
        color = MidnightSurface,
        border = BorderStroke(1.dp, if (goal.done) CardBorderDone else MidnightSurfaceHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (goal.done) PulseEmerald else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (goal.done) PulseEmerald else Color(0xFF475569),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (goal.done) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Goal completed",
                        tint = PulseEmeraldDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PulseBlue.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, PulseBlue.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${goal.span.label} • ${goal.subject}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PulseBlueSoft,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (goal.done) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (goal.done) TextMuted else TextPrimary
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_goal_${goal.id}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete goal ${goal.title}",
                    tint = PulseRoseLight,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
