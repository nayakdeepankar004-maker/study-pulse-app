package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.remote.TestAttempt
import com.example.model.StudyCatalog
import com.example.ui.ActiveTestState
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.RoseDanger

@Composable
fun PracticeScreen(
    activeTest: ActiveTestState?,
    isGeneratingAiQuiz: Boolean,
    recentAttempts: List<TestAttempt>,
    onStartCuratedQuiz: (track: String, subject: String, isExamMode: Boolean, negativeMarking: Boolean) -> Unit,
    onGenerateAiQuiz: (topic: String, subject: String, track: String, count: Int, isExamMode: Boolean, negativeMarking: Boolean) -> Unit,
    onSelectAnswer: (questionIndex: Int, optionIndex: Int) -> Unit,
    onGoToQuestion: (Int) -> Unit,
    onSubmitTest: () -> Unit,
    onExitTest: () -> Unit
) {
    if (activeTest != null) {
        if (activeTest.isFinished) {
            QuizResultsView(
                state = activeTest,
                onExit = onExitTest
            )
        } else {
            ActiveQuizView(
                state = activeTest,
                onSelectAnswer = onSelectAnswer,
                onGoToQuestion = onGoToQuestion,
                onSubmitTest = onSubmitTest,
                onExitTest = onExitTest
            )
        }
        return
    }

    var selectedTrack by remember { mutableStateOf("NEET") }
    var selectedSubject by remember { mutableStateOf("All") }
    var isExamMode by remember { mutableStateOf(false) }
    var negativeMarking by remember { mutableStateOf(true) }

    var aiTopic by remember { mutableStateOf("") }
    var aiSubject by remember { mutableStateOf("Physics") }
    var aiCount by remember { mutableIntStateOf(5) }

    val tracks = listOf("All", "Board Exams", "NEET", "JEE Main", "Foundation")
    val subjects = listOf("All") + StudyCatalog.subjects

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Practice & Mock Test Engine",
            style = MaterialTheme.typography.titleLarge
        )

        // Curated Mock Test Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Standard Syllabus Mock", style = MaterialTheme.typography.titleMedium)

                Text("Track", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    tracks.forEach { track ->
                        FilterChip(
                            selected = selectedTrack == track,
                            onClick = { selectedTrack = track },
                            label = { Text(track) }
                        )
                    }
                }

                Text("Subject", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    subjects.forEach { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj) }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Timed Exam Mode", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "Hide answers until submission with countdown timer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isExamMode,
                        onCheckedChange = { isExamMode = it }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("NEET/JEE Marking (+4 / -1)", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "Deduct 1 mark for incorrect answers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = negativeMarking,
                        onCheckedChange = { negativeMarking = it }
                    )
                }

                Button(
                    onClick = {
                        onStartCuratedQuiz(selectedTrack, selectedSubject, isExamMode, negativeMarking)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_curated_quiz_button")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Start Mock Test")
                }
            }
        }

        // AI Question Generator Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("AI Custom Quiz Generator", style = MaterialTheme.typography.titleMedium)
                }

                Text(
                    text = "Generate fresh syllabus-aligned MCQs with step-by-step solutions on any chapter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = aiTopic,
                    onValueChange = { aiTopic = it },
                    label = { Text("Chapter or Topic (e.g., Gauss Law, Aldol Condensation)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_quiz_topic_input")
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    StudyCatalog.subjects.forEach { subj ->
                        FilterChip(
                            selected = aiSubject == subj,
                            onClick = { aiSubject = subj },
                            label = { Text(subj) }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Questions:", style = MaterialTheme.typography.labelMedium)
                    listOf(3, 5, 10).forEach { count ->
                        FilterChip(
                            selected = aiCount == count,
                            onClick = { aiCount = count },
                            label = { Text("$count Qs") }
                        )
                    }
                }

                Button(
                    onClick = {
                        onGenerateAiQuiz(
                            aiTopic,
                            aiSubject,
                            if (selectedTrack == "All") "NEET" else selectedTrack,
                            aiCount,
                            isExamMode,
                            negativeMarking
                        )
                    },
                    enabled = !isGeneratingAiQuiz && aiTopic.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("generate_ai_quiz_button")
                ) {
                    if (isGeneratingAiQuiz) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Generating AI MCQs...")
                    } else {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Generate & Start AI Quiz")
                    }
                }
            }
        }

        if (recentAttempts.isNotEmpty()) {
            Text("Past Attempts", style = MaterialTheme.typography.titleMedium)
            recentAttempts.take(5).forEach { attempt ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(attempt.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${attempt.score}/${attempt.maxScore} marks • ${attempt.accuracyPercent}% accuracy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = attempt.track,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveQuizView(
    state: ActiveTestState,
    onSelectAnswer: (Int, Int) -> Unit,
    onGoToQuestion: (Int) -> Unit,
    onSubmitTest: () -> Unit,
    onExitTest: () -> Unit
) {
    val qIndex = state.currentIndex
    val question = state.questions.getOrNull(qIndex) ?: return
    val selectedOption = state.selectedAnswers[qIndex]
    val mins = (if (state.isExamMode) state.secondsRemaining else state.elapsedSeconds) / 60
    val secs = (if (state.isExamMode) state.secondsRemaining else state.elapsedSeconds) % 60
    val timeLabel = "%02d:%02d".format(mins, secs)

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(state.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Question ${qIndex + 1} of ${state.questions.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(timeLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        LinearProgressIndicator(
            progress = { (qIndex + 1).toFloat() / state.questions.size.coerceAtLeast(1).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${question.subject} • ${question.chapter}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleMedium
                )

                question.options.forEachIndexed { optIdx, optionText ->
                    val isSelected = selectedOption == optIdx
                    val showInstant = !state.isExamMode && selectedOption != null
                    val isCorrectOpt = optIdx == question.correctIndex

                    val borderColor = when {
                        showInstant && isCorrectOpt -> EmeraldSecondary
                        showInstant && isSelected && !isCorrectOpt -> RoseDanger
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    }
                    val bgColor = when {
                        showInstant && isCorrectOpt -> EmeraldSecondary.copy(alpha = 0.12f)
                        showInstant && isSelected && !isCorrectOpt -> RoseDanger.copy(alpha = 0.12f)
                        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = BorderStroke(1.5.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectAnswer(qIndex, optIdx) }
                            .testTag("quiz_option_$optIdx")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = "${'A' + optIdx}. $optionText",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (showInstant && isCorrectOpt) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = "Correct", tint = EmeraldSecondary)
                            } else if (showInstant && isSelected) {
                                Icon(Icons.Rounded.Close, contentDescription = "Incorrect", tint = RoseDanger)
                            }
                        }
                    }
                }

                if (!state.isExamMode && selectedOption != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Explanation",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(question.explanation, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { onGoToQuestion(qIndex - 1) },
                enabled = qIndex > 0,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = null)
                Text("Previous")
            }

            if (qIndex + 1 < state.questions.size) {
                Button(
                    onClick = { onGoToQuestion(qIndex + 1) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Next")
                    Icon(Icons.AutoMirrored.Rounded.NavigateNext, contentDescription = null)
                }
            } else {
                Button(
                    onClick = onSubmitTest,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("submit_quiz_button")
                ) {
                    Text("Submit Test")
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onExitTest,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Exit")
            }
            if (qIndex + 1 < state.questions.size) {
                OutlinedButton(
                    onClick = onSubmitTest,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Finish Early")
                }
            }
        }
    }
}

@Composable
private fun QuizResultsView(
    state: ActiveTestState,
    onExit: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, EmeraldSecondary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("Test Complete!", style = MaterialTheme.typography.headlineSmall)
                Text(state.title, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${state.lastScore} / ${state.lastMaxScore} Marks",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${state.lastCorrectCount} of ${state.questions.size} correct • ${state.lastAccuracy}% Accuracy",
                    style = MaterialTheme.typography.titleSmall,
                    color = EmeraldSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onExit,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("done_quiz_results_button")
                ) {
                    Text("Back to Practice Hub")
                }
            }
        }

        Text("Detailed Solutions", style = MaterialTheme.typography.titleMedium)

        state.questions.forEachIndexed { idx, q ->
            val picked = state.selectedAnswers[idx]
            val isCorrect = picked == q.correctIndex
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (isCorrect) EmeraldSecondary.copy(alpha = 0.5f) else RoseDanger.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "Q${idx + 1}. ${q.question}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Your Answer: ${picked?.let { q.options.getOrNull(it) } ?: "Skipped"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCorrect) EmeraldSecondary else RoseDanger
                    )
                    Text(
                        text = "Correct Answer: ${q.options[q.correctIndex]}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSecondary
                    )
                    Text(
                        text = q.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
