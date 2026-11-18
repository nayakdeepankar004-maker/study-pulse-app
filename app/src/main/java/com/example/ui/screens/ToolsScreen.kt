package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.CalculatorState
import com.example.ui.PomodoroState

@Composable
fun ToolsScreen(
    pomodoro: PomodoroState,
    calculator: CalculatorState,
    onSelectPreset: (minutes: Int, isBreak: Boolean) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onCalculatorInput: (String) -> Unit
) {
    val mins = pomodoro.secondsRemaining / 60
    val secs = pomodoro.secondsRemaining % 60
    val formattedTime = "%02d:%02d".format(mins, secs)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Study Tools", style = MaterialTheme.typography.titleLarge)

        // Pomodoro Focus Timer Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Pomodoro Focus Timer", style = MaterialTheme.typography.titleMedium)
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${pomodoro.completedSessions} done",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = !pomodoro.isBreakMode && pomodoro.presetMinutes == 25,
                        onClick = { onSelectPreset(25, false) },
                        label = { Text("25m Focus") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !pomodoro.isBreakMode && pomodoro.presetMinutes == 50,
                        onClick = { onSelectPreset(50, false) },
                        label = { Text("50m Deep") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = pomodoro.isBreakMode && pomodoro.presetMinutes == 5,
                        onClick = { onSelectPreset(5, true) },
                        label = { Text("5m Break") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("pomodoro_clock_display")
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onToggleTimer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pomodoro_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (pomodoro.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(if (pomodoro.isRunning) "Pause" else "Start")
                    }
                    OutlinedButton(
                        onClick = onResetTimer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pomodoro_reset_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Reset")
                    }
                }
            }
        }

        // Scientific Calculator Card
        Card(
            shape = RoundedCornerShape(20.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Scientific Calculator", style = MaterialTheme.typography.titleMedium)
                    }
                    FilterChip(
                        selected = calculator.useDegrees,
                        onClick = { onCalculatorInput("DEG_RAD") },
                        label = { Text(if (calculator.useDegrees) "DEG" else "RAD") },
                        modifier = Modifier.testTag("calc_deg_rad_button")
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = calculator.expression.ifEmpty { "0" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = calculator.result,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("calc_result_display")
                        )
                    }
                }

                val rows = listOf(
                    listOf("sin(" to "sin", "cos(" to "cos", "tan(" to "tan", "AC" to "AC"),
                    listOf("log(" to "log", "ln(" to "ln", "√(" to "√", "DEL" to "DEL"),
                    listOf("(" to "(", ")" to ")", "^" to "^", "÷" to "÷"),
                    listOf("7" to "7", "8" to "8", "9" to "9", "×" to "×"),
                    listOf("4" to "4", "5" to "5", "6" to "6", "−" to "−"),
                    listOf("1" to "1", "2" to "2", "3" to "3", "+" to "+"),
                    listOf("π" to "π", "0" to "0", "." to ".", "=" to "=")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { (token, label) ->
                                val isPrimary = token == "="
                                val isDanger = token == "AC" || token == "DEL"
                                Button(
                                    onClick = { onCalculatorInput(token) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            isPrimary -> MaterialTheme.colorScheme.primary
                                            isDanger -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        contentColor = when {
                                            isPrimary -> MaterialTheme.colorScheme.onPrimary
                                            isDanger -> MaterialTheme.colorScheme.onErrorContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("calc_btn_$label")
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
