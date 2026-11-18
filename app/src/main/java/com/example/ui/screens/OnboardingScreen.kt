package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.remote.UserProfile
import com.example.model.StudyCatalog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    profile: UserProfile,
    onComplete: (displayName: String, board: String, studentClass: String, stream: String, targetExam: String) -> Unit
) {
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var selectedBoard by remember(profile.board) { mutableStateOf(profile.board) }
    var selectedClass by remember(profile.studentClass) { mutableStateOf(profile.studentClass) }
    var selectedStream by remember(profile.stream) { mutableStateOf(profile.stream) }
    var selectedExam by remember(profile.targetExam) { mutableStateOf(profile.targetExam) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Set up your study profile",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Personalize your curriculum, question banks, and AI Tutor explanations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input")
                )

                Text("Select Board", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StudyCatalog.boards.forEach { board ->
                        FilterChip(
                            selected = selectedBoard == board,
                            onClick = { selectedBoard = board },
                            label = { Text(board) },
                            modifier = Modifier.testTag("onboarding_board_$board")
                        )
                    }
                }

                Text("Select Class", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StudyCatalog.classes.forEach { cls ->
                        FilterChip(
                            selected = selectedClass == cls,
                            onClick = { selectedClass = cls },
                            label = { Text(cls) },
                            modifier = Modifier.testTag("onboarding_class_$cls")
                        )
                    }
                }

                Text("Select Stream", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StudyCatalog.streams.forEach { stream ->
                        FilterChip(
                            selected = selectedStream == stream,
                            onClick = { selectedStream = stream },
                            label = { Text(stream) },
                            modifier = Modifier.testTag("onboarding_stream_$stream")
                        )
                    }
                }

                Text("Primary Exam Focus", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StudyCatalog.targetExams.forEach { exam ->
                        FilterChip(
                            selected = selectedExam == exam,
                            onClick = { selectedExam = exam },
                            label = { Text(exam) },
                            modifier = Modifier.testTag("onboarding_exam_$exam")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        onComplete(
                            displayName.ifBlank { "Student" },
                            selectedBoard,
                            selectedClass,
                            selectedStream,
                            selectedExam
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("complete_onboarding_button")
                ) {
                    Text("Go to Dashboard", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
