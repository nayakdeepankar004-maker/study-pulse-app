package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ChapterGuide
import com.example.model.StudyCatalog
import com.example.ui.SubDestination

@Composable
fun LibraryScreen(
    selectedTrack: String,
    selectedSubject: String,
    notesCount: Int,
    pdfsCount: Int,
    onFilterChange: (track: String, subject: String) -> Unit,
    onOpenSubDestination: (SubDestination) -> Unit,
    onStartChapterQuiz: (track: String, subject: String) -> Unit,
    onAskAiChapter: (ChapterGuide) -> Unit
) {
    val tracks = listOf("All", "Board Exams", "NEET", "JEE Main", "Foundation")
    val subjects = listOf("All") + StudyCatalog.subjects

    val filteredChapters = StudyCatalog.chapters.filter { ch ->
        val trackOk = selectedTrack == "All" || ch.track == selectedTrack
        val subjectOk = selectedSubject == "All" || ch.subject == selectedSubject
        trackOk && subjectOk
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Study Library & Competitive Prep",
            style = MaterialTheme.typography.titleLarge
        )

        // Notes & PDF quick access strip
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { onOpenSubDestination(SubDestination.NOTES) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("library_open_notes_button")
            ) {
                Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Class Notes ($notesCount)")
            }
            OutlinedButton(
                onClick = { onOpenSubDestination(SubDestination.PDF_LIBRARY) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("library_open_pdfs_button")
            ) {
                Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("PDFs ($pdfsCount)")
            }
        }

        // Track Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            tracks.forEach { track ->
                FilterChip(
                    selected = selectedTrack == track,
                    onClick = { onFilterChange(track, selectedSubject) },
                    label = { Text(track) },
                    modifier = Modifier.testTag("library_track_$track")
                )
            }
        }

        // Subject Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            subjects.forEach { subject ->
                FilterChip(
                    selected = selectedSubject == subject,
                    onClick = { onFilterChange(selectedTrack, subject) },
                    label = { Text(subject) },
                    modifier = Modifier.testTag("library_subject_$subject")
                )
            }
        }

        if (filteredChapters.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "No matching chapters",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Try switching the track or subject filter, or ask the AI Tutor for custom notes on any topic.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            filteredChapters.forEach { chapter ->
                ChapterGuideCard(
                    chapter = chapter,
                    onPractice = { onStartChapterQuiz(chapter.track, chapter.subject) },
                    onAskAi = { onAskAiChapter(chapter) }
                )
            }
        }
    }
}

@Composable
private fun ChapterGuideCard(
    chapter: ChapterGuide,
    onPractice: () -> Unit,
    onAskAi: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${chapter.track} • ${chapter.subject}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "${chapter.classLevel} • ${chapter.weightage}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = chapter.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Key Formulas & Facts",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    chapter.keyFormulas.forEach { formula ->
                        Text(
                            text = "• $formula",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onPractice,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("chapter_practice_${chapter.id}")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Practice Quiz")
                }
                OutlinedButton(
                    onClick = onAskAi,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("chapter_ai_${chapter.id}")
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Ask AI Tutor")
                }
            }
        }
    }
}
