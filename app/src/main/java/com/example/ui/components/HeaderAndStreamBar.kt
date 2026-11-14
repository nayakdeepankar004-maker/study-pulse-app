package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SUBJECT_ALL
import com.example.model.StudyCurriculum
import com.example.model.StudyStream
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.MidnightHeader
import com.example.ui.theme.MidnightInputBg
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceHigh
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseBlueLight
import com.example.ui.theme.PulseBlueSoft
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.PulseGold
import com.example.ui.theme.PulsePurple
import com.example.ui.theme.PulsePurpleLight
import com.example.ui.theme.PulsePurpleSoft
import com.example.ui.theme.PulseRose
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StudyPulseTopBar(
    headerDate: String,
    streakDays: Int,
    onTestAlarmClick: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MidnightHeader.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, CardBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(PulseBlue, PulsePurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = "Study Pulse brand emblem",
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.brand_title),
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                brush = Brush.horizontalGradient(listOf(PulseBlueLight, PulsePurpleLight))
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = CircleShape,
                            color = PulseAmber.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, PulseAmber.copy(alpha = 0.40f))
                        ) {
                            Text(
                                text = "Prince",
                                style = MaterialTheme.typography.labelSmall,
                                color = PulseGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = headerDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PulseAmber.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, PulseAmber.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Study streak",
                            tint = PulseGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${streakDays}d",
                            style = MaterialTheme.typography.labelMedium,
                            color = PulseGold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PulsePurple.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, PulsePurple.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("test_alarm_button")
                        .clickable(onClick = onTestAlarmClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = "Test alarm sound and burst",
                            tint = PulsePurpleSoft,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Test",
                            style = MaterialTheme.typography.labelMedium,
                            color = PulsePurpleSoft
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = PulseBlue.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, PulseBlue.copy(alpha = 0.42f)),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .testTag("account_menu_button")
                        .clickable(onClick = onAccountClick)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "Student Account & Sign Out",
                            tint = PulseBlueSoft,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountProfileDialog(
    displayName: String,
    email: String?,
    streamLabel: String,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        icon = {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = null,
                tint = PulseBlueLight,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!email.isNullOrBlank()) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PulseEmerald.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, PulseEmerald.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDone,
                            contentDescription = null,
                            tint = PulseEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Cloud Backup Active",
                                style = MaterialTheme.typography.labelMedium,
                                color = PulseEmerald
                            )
                            Text(
                                text = "Active Batch: $streamLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PulseRose.copy(alpha = 0.22f),
                    contentColor = Color(0xFFFDA4AF)
                ),
                border = BorderStroke(1.dp, PulseRose.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("dialog_sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

@Composable
fun StreamAndSubjectSelectorCard(
    selectedStream: StudyStream,
    selectedSubjectFilter: String,
    onSelectStream: (StudyStream) -> Unit,
    onSelectSubjectFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjects = StudyCurriculum.subjectsFor(selectedStream)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MidnightSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, CardBorderBlue)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        imageVector = Icons.Rounded.School,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "CLASS / EXAM STREAM",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Text(
                    text = selectedStream.batchName,
                    style = MaterialTheme.typography.labelMedium,
                    color = PulseBlueLight
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StudyStream.entries.forEach { stream ->
                    val isActive = stream == selectedStream
                    val shape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(shape)
                            .then(
                                if (isActive) {
                                    Modifier.background(
                                        Brush.linearGradient(listOf(PulseBlue, PulsePurple)),
                                        shape = shape
                                    )
                                } else {
                                    Modifier
                                        .background(MidnightInputBg, shape = shape)
                                        .border(1.dp, MidnightSurfaceHigh, shape)
                                }
                            )
                            .testTag("stream_btn_${stream.code.lowercase()}")
                            .clickable { onSelectStream(stream) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stream.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isActive) Color.White else TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CardBorderSubtle)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val allActive = selectedSubjectFilter == SUBJECT_ALL
                SubjectFilterChip(
                    label = "All ${selectedStream.code}",
                    icon = Icons.Rounded.AutoAwesome,
                    selected = allActive,
                    onClick = { onSelectSubjectFilter(SUBJECT_ALL) },
                    testTag = "subject_chip_all"
                )
                subjects.forEach { spec ->
                    SubjectFilterChip(
                        label = spec.name,
                        icon = spec.icon,
                        selected = selectedSubjectFilter == spec.name,
                        onClick = { onSelectSubjectFilter(spec.name) },
                        testTag = "subject_chip_${spec.name.lowercase().replace(" ", "_")}"
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectFilterChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = CircleShape,
        color = if (selected) PulseBlue.copy(alpha = 0.22f) else MidnightInputBg,
        border = BorderStroke(1.dp, if (selected) PulseBlue else MidnightSurfaceHigh),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) PulseBlueSoft else TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) PulseBlueSoft else TextSecondary,
                maxLines = 1
            )
        }
    }
}
