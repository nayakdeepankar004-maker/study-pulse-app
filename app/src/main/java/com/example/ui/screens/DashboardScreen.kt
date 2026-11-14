package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SUBJECT_ALL
import com.example.model.StudyTask
import com.example.ui.StudyPulseUiState
import com.example.ui.components.StudyTaskCard
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MidnightInputBg
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceHigh
import com.example.ui.theme.PulseAmberLight
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseBlueLight
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.PulseEmeraldLight
import com.example.ui.theme.PulsePurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardSection(
    uiState: StudyPulseUiState,
    onOpenNewTask: () -> Unit,
    onToggleTask: (StudyTask) -> Unit,
    onEditTask: (StudyTask) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.todayProgressPercent / 100f,
        animationSpec = tween(durationMillis = 500),
        label = "today_ring_progress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Hero Live Clock & Progress Ring Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MidnightSurface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, CardBorderBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = PulseBlueLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "LIVE STUDY CLOCK",
                                style = MaterialTheme.typography.labelSmall,
                                color = PulseBlueLight
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.clockTimeDisplay,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 26.sp
                            ),
                            color = Color.White,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"Success is the sum of small efforts, repeated day in and day out.\"",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier.size(84.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val ringColor = if (uiState.todayProgressPercent == 100) PulseEmerald else PulseBlue
                        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                            val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            drawArc(
                                color = MidnightSurfaceHigh,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = stroke
                            )
                            drawArc(
                                brush = Brush.sweepGradient(listOf(ringColor, PulsePurple, ringColor)),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = stroke
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.todayProgressPercent}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "DONE",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = TextMuted
                            )
                        }
                    }
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile(
                        value = uiState.todayTotalCount.toString(),
                        label = "Today",
                        valueColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = uiState.todayDoneCount.toString(),
                        label = "Done",
                        valueColor = PulseEmeraldLight,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = uiState.todayPendingCount.toString(),
                        label = "Pending",
                        valueColor = PulseAmberLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Primary CTA Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(PulseBlue, PulsePurple)))
                .testTag("schedule_task_button")
                .clickable(onClick = onOpenNewTask),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Schedule New Chapter / Study Alarm",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Section Header
        val subjectFilter = uiState.preferences.selectedSubjectFilter
        val sectionTitle = if (subjectFilter == SUBJECT_ALL) {
            "Today's Sessions"
        } else {
            "$subjectFilter Today"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = "${uiState.todayTasks.size} session${if (uiState.todayTasks.size == 1) "" else "s"}",
                style = MaterialTheme.typography.labelMedium,
                color = PulseBlueLight
            )
        }

        if (uiState.todayTasks.isEmpty()) {
            EmptyStudyIllustrationCard(
                message = "No study sessions for this filter yet. Tap Schedule New Chapter above to set your next study alarm!"
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.todayTasks.forEach { task ->
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

@Composable
private fun StatTile(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MidnightInputBg,
        border = BorderStroke(1.dp, MidnightSurfaceHigh)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = valueColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun EmptyStudyIllustrationCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MidnightSurface,
        border = BorderStroke(1.dp, MidnightSurfaceHigh)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_study_empty_1794635209912),
                contentDescription = "Quiet study desk illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
