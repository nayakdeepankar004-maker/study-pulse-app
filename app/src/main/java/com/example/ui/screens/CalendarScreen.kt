package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AlarmTone
import com.example.model.DateTimeHelper
import com.example.model.StudyTask
import com.example.ui.StudyPulseUiState
import com.example.ui.components.StudyTaskCard
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.MidnightInputBg
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceHigh
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseBlueLight
import com.example.ui.theme.PulseBlueSoft
import com.example.ui.theme.PulseGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSection(
    uiState: StudyPulseUiState,
    onSelectCalendarDate: (String) -> Unit,
    onSelectAlarmTone: (AlarmTone) -> Unit,
    onToggleTask: (StudyTask) -> Unit,
    onEditTask: (StudyTask) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val today = remember { DateTimeHelper.todayString() }
    val tomorrow = remember { DateTimeHelper.offsetDateString(1) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Schedule By Date Card
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = PulseBlueLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Schedule By Date",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MidnightInputBg,
                        border = BorderStroke(1.dp, PulseBlue.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .testTag("calendar_pick_date_button")
                            .clickable { showDatePicker = true }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = DateTimeHelper.formatDateDisplay(uiState.selectedCalendarDate),
                                style = MaterialTheme.typography.labelMedium,
                                color = PulseBlueSoft
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickDateChip(
                        label = "Today",
                        selected = uiState.selectedCalendarDate == today,
                        onClick = { onSelectCalendarDate(today) },
                        testTag = "calendar_chip_today"
                    )
                    QuickDateChip(
                        label = "Tomorrow",
                        selected = uiState.selectedCalendarDate == tomorrow,
                        onClick = { onSelectCalendarDate(tomorrow) },
                        testTag = "calendar_chip_tomorrow"
                    )
                    QuickDateChip(
                        label = "Pick Date",
                        selected = uiState.selectedCalendarDate != today && uiState.selectedCalendarDate != tomorrow,
                        onClick = { showDatePicker = true },
                        testTag = "calendar_chip_pick"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (uiState.calendarTasks.isEmpty()) {
                    EmptyStudyIllustrationCard(
                        message = "No sessions scheduled for ${DateTimeHelper.formatDateDisplay(uiState.selectedCalendarDate)}."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.calendarTasks.forEach { task ->
                            StudyTaskCard(
                                task = task,
                                onToggleComplete = { onToggleTask(task) },
                                onEdit = { onEditTask(task) },
                                onDelete = { onDeleteTask(task.id) }
                            )
                        }
                    }
                }
            }
        }

        // Alarm Tone & Credits Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MidnightSurface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, CardBorderBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = PulseBlueLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Alarm Tone & Credits",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlarmTone.entries.forEach { tone ->
                        val isSelected = uiState.preferences.alarmTone == tone
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) PulseBlue.copy(alpha = 0.16f) else MidnightInputBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) PulseBlue else MidnightSurfaceHigh
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .testTag("tone_option_${tone.code}")
                                .clickable { onSelectAlarmTone(tone) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (tone == AlarmTone.CYBER) {
                                            Icons.Rounded.NotificationsActive
                                        } else {
                                            Icons.Rounded.GraphicEq
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) PulseBlueLight else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = tone.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) Color.White else TextSecondary
                                        )
                                        Text(
                                            text = tone.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectAlarmTone(tone) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = PulseBlueLight,
                                        unselectedColor = TextMuted
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CardBorderSubtle)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                            tint = PulseGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.credits_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PulseGold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.credits_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        onSelectCalendarDate(sdf.format(Date(millis)))
                    }
                    showDatePicker = false
                }) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun QuickDateChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) PulseBlue else MidnightSurfaceHigh,
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else TextSecondary
            )
        }
    }
}
