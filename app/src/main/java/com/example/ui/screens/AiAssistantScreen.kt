package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.data.remote.AiSolveMode
import com.example.ui.AiTutorState

@Composable
fun AiAssistantScreen(
    aiState: AiTutorState,
    onSelectMode: (AiSolveMode) -> Unit,
    onSelectSubjectHint: (String) -> Unit,
    onSendMessage: (prompt: String, image: Bitmap?) -> Unit,
    onSaveSolutionAsNote: (title: String, subject: String, content: String) -> Unit,
    onClearChat: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var promptText by remember { mutableStateOf("") }
    var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Zero-permission Android Photo Picker for high-res multimodal doubt images
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedBitmap = loadScaledBitmap(context, uri)
        }
    }

    val subjectHints = listOf("Auto", "Physics", "Chemistry", "Mathematics", "Biology", "Science")
    val quickPrompts = listOf(
        "Solve tough JEE numerical step-by-step",
        "Show mechanism & stereochemistry trick",
        "Hard definite integration problem",
        "NEET Biology high-yield exceptions"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text("Pro AI Study Solver", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = aiState.solveMode.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onClearChat,
                modifier = Modifier.testTag("ai_clear_chat_button")
            ) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear conversation")
            }
        }

        // Reasoning / Solve Mode Selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            AiSolveMode.entries.forEach { mode ->
                FilterChip(
                    selected = aiState.solveMode == mode,
                    onClick = { onSelectMode(mode) },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(mode.label) },
                    modifier = Modifier.testTag("ai_mode_${mode.name}")
                )
            }
        }

        // Subject Context Filter
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            subjectHints.forEach { subj ->
                FilterChip(
                    selected = aiState.subjectHint == subj,
                    onClick = { onSelectSubjectHint(subj) },
                    label = { Text(if (subj == "Auto") "Subject: Auto" else subj) },
                    modifier = Modifier.testTag("ai_subject_$subj")
                )
            }
        }

        // Quick difficult-question prompts
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            quickPrompts.forEach { suggestion ->
                AssistChip(
                    onClick = {
                        if (!aiState.isGenerating) {
                            onSendMessage(suggestion, null)
                        }
                    },
                    label = { Text(suggestion) }
                )
            }
        }

        // Conversation messages
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            aiState.messages.forEachIndexed { index, msg ->
                val isUser = msg.isUser
                Row(
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isUser) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 0.96f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (msg.hasImage) {
                                Text(
                                    text = "[Attached doubt photo]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = formatMarkdownBold(msg.text),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (!isUser && msg.id != "welcome") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { clipboard.setText(AnnotatedString(msg.text)) },
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.ContentCopy,
                                            contentDescription = "Copy answer",
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text("Copy", style = MaterialTheme.typography.labelMedium)
                                    }
                                    val prevPrompt = aiState.messages
                                        .take(index)
                                        .lastOrNull { it.isUser }
                                        ?.text
                                        ?: "AI Solved Question"
                                    TextButton(
                                        onClick = {
                                            onSaveSolutionAsNote(
                                                prevPrompt.lineSequence().firstOrNull().orEmpty().take(70),
                                                aiState.subjectHint,
                                                msg.text
                                            )
                                        },
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.BookmarkAdd,
                                            contentDescription = "Save to notes",
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text("Save Note", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Follow-up verification / alternative method chips after latest AI answer
            val lastMessage = aiState.messages.lastOrNull()
            if (!aiState.isGenerating && lastMessage != null && !lastMessage.isUser && lastMessage.id != "welcome") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    val followUps = listOf(
                        "Verify Solution" to "Independently verify every calculation, unit, sign convention, and edge case in your previous solution. Confirm if the final answer is 100% accurate.",
                        "Shortcut Trick" to "Give me a fast JEE/NEET exam-hall shortcut or elimination method for this exact question.",
                        "Explain in Hinglish" to "Explain this exact solution step-by-step in simple Hinglish so it is crystal clear.",
                        "Similar Hard Question" to "Give me 1 tougher follow-up practice problem based on the same concept with its answer hidden at the end."
                    )
                    followUps.forEach { (label, prompt) ->
                        AssistChip(
                            onClick = { onSendMessage(prompt, null) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (aiState.isGenerating) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth(0.96f)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${aiState.solveMode.label} analyzing...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            val streaming = aiState.streamingReply
                            if (!streaming.isNullOrBlank()) {
                                Text(
                                    text = formatMarkdownBold(streaming),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            if (!aiState.errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = aiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Attached photo preview
        val bmp = attachedBitmap
        if (bmp != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Attached question photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    IconButton(
                        onClick = { attachedBitmap = null },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Remove attached photo",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Input bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = !aiState.isGenerating,
                modifier = Modifier.testTag("ai_attach_photo_button")
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = "Attach question image")
            }

            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                placeholder = { Text("Paste any hard question or upload photo...") },
                maxLines = 5,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input")
            )

            IconButton(
                onClick = {
                    val img = attachedBitmap
                    val text = promptText
                    promptText = ""
                    attachedBitmap = null
                    onSendMessage(text, img)
                },
                enabled = !aiState.isGenerating && (promptText.isNotBlank() || attachedBitmap != null),
                modifier = Modifier.testTag("ai_send_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send prompt",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatMarkdownBold(raw: String): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    while (cursor < raw.length) {
        val start = raw.indexOf("**", cursor)
        if (start == -1) {
            append(raw.substring(cursor))
            break
        }
        val end = raw.indexOf("**", start + 2)
        if (end == -1) {
            append(raw.substring(cursor))
            break
        }
        if (start > cursor) {
            append(raw.substring(cursor, start))
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(raw.substring(start + 2, end))
        }
        cursor = end + 2
    }
}

private fun loadScaledBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return runCatching {
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        val maxDim = 1600
        if (raw.width <= maxDim && raw.height <= maxDim) {
            raw
        } else {
            val ratio = minOf(maxDim.toFloat() / raw.width, maxDim.toFloat() / raw.height)
            Bitmap.createScaledBitmap(
                raw,
                (raw.width * ratio).toInt().coerceAtLeast(1),
                (raw.height * ratio).toInt().coerceAtLeast(1),
                true
            )
        }
    }.getOrNull()
}
