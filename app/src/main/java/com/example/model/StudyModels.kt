package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class StudyStream(val code: String, val label: String, val batchName: String) {
    CLASS_10("10th", "10th", "10th Batch"),
    CLASS_11("11th", "11th", "11th Batch"),
    CLASS_12("12th", "12th", "12th Batch"),
    NEET("NEET", "NEET", "NEET Batch"),
    JEE("JEE", "JEE", "JEE Batch");

    companion object {
        fun fromCode(code: String): StudyStream =
            entries.firstOrNull { it.code == code } ?: NEET
    }
}

data class SubjectSpec(
    val name: String,
    val icon: ImageVector
)

object StudyCurriculum {
    private val map: Map<StudyStream, List<SubjectSpec>> = mapOf(
        StudyStream.CLASS_10 to listOf(
            SubjectSpec("Mathematics", Icons.Rounded.Calculate),
            SubjectSpec("Science", Icons.Rounded.Science),
            SubjectSpec("Social Science", Icons.Rounded.Public),
            SubjectSpec("English", Icons.Rounded.AutoStories),
            SubjectSpec("Hindi / Sanskrit", Icons.Rounded.Create)
        ),
        StudyStream.CLASS_11 to listOf(
            SubjectSpec("Physics", Icons.Rounded.Bolt),
            SubjectSpec("Chemistry", Icons.Rounded.Science),
            SubjectSpec("Mathematics", Icons.Rounded.Calculate),
            SubjectSpec("Biology", Icons.Rounded.Biotech),
            SubjectSpec("English", Icons.Rounded.AutoStories)
        ),
        StudyStream.CLASS_12 to listOf(
            SubjectSpec("Physics", Icons.Rounded.Bolt),
            SubjectSpec("Chemistry", Icons.Rounded.Science),
            SubjectSpec("Mathematics", Icons.Rounded.Calculate),
            SubjectSpec("Biology", Icons.Rounded.Biotech),
            SubjectSpec("English", Icons.Rounded.AutoStories)
        ),
        StudyStream.NEET to listOf(
            SubjectSpec("Physics", Icons.Rounded.Bolt),
            SubjectSpec("Chemistry", Icons.Rounded.Science),
            SubjectSpec("Botany", Icons.Rounded.Eco),
            SubjectSpec("Zoology", Icons.Rounded.Biotech)
        ),
        StudyStream.JEE to listOf(
            SubjectSpec("Physics", Icons.Rounded.Bolt),
            SubjectSpec("Chemistry", Icons.Rounded.Science),
            SubjectSpec("Mathematics", Icons.Rounded.Calculate)
        )
    )

    fun subjectsFor(stream: StudyStream): List<SubjectSpec> =
        map[stream].orEmpty()

    fun iconFor(subjectName: String): ImageVector = when (subjectName) {
        "Physics" -> Icons.Rounded.Bolt
        "Chemistry", "Science" -> Icons.Rounded.Science
        "Mathematics" -> Icons.Rounded.Calculate
        "Botany" -> Icons.Rounded.Eco
        "Zoology", "Biology" -> Icons.Rounded.Biotech
        "Social Science" -> Icons.Rounded.Public
        "Hindi / Sanskrit" -> Icons.Rounded.Create
        else -> Icons.Rounded.AutoStories
    }
}

enum class RepeatMode(val code: String, val label: String) {
    ONCE("once", "Once"),
    DAILY("daily", "Daily");

    companion object {
        fun fromCode(code: String): RepeatMode =
            entries.firstOrNull { it.code == code } ?: ONCE
    }
}

enum class GoalSpan(val label: String) {
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    NEXT_2_DAYS("Next 2 Days"),
    NEXT_7_DAYS("Next 7 Days");

    companion object {
        fun fromLabel(label: String): GoalSpan =
            entries.firstOrNull { it.label == label } ?: TODAY
    }
}

enum class AlarmTone(val code: String, val label: String, val subtitle: String) {
    NEON("neon", "Pulse Neon Synth", "Warm harmonic sine pulse"),
    CYBER("cyber", "Cyber Bell", "Crisp dual-tone triangle chime"),
    URGENT("urgent", "Urgent Lab Beep", "High-focus sawtooth alert");

    companion object {
        fun fromCode(code: String): AlarmTone =
            entries.firstOrNull { it.code == code } ?: NEON
    }
}

enum class TaskFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    COMPLETED("Completed")
}

enum class StudyTab(val route: String, val label: String) {
    DASHBOARD("pulse", "Dashboard"),
    ALARMS("alarms", "Alarms"),
    GOALS("goals", "Goals"),
    CALENDAR("calendar", "Calendar")
}

data class StudyTask(
    val id: String,
    val title: String,
    val subject: String,
    val date: String,
    val time: String,
    val repeatMode: RepeatMode = RepeatMode.ONCE,
    val notes: String = "",
    val completed: Boolean = false,
    val celebrated: Boolean = false,
    val firedKey: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ChapterGoal(
    val id: String,
    val title: String,
    val span: GoalSpan,
    val subject: String,
    val done: Boolean = false,
    val celebrated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class StudyPreferences(
    val selectedStream: StudyStream = StudyStream.NEET,
    val selectedSubjectFilter: String = SUBJECT_ALL,
    val dailyTargetLectures: Int = 4,
    val dailyTargetHours: Int = 6,
    val loggedHoursToday: Float = 2.5f,
    val streak: Int = 7,
    val alarmTone: AlarmTone = AlarmTone.NEON,
    val lastLoggedDate: String = DateTimeHelper.todayString(),
    val isSeeded: Boolean = false
)

const val SUBJECT_ALL = "ALL"

object DateTimeHelper {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val headerDateFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.US)
    private val clockFormatter = SimpleDateFormat("hh:mm:ss a", Locale.US)
    private val hmFormatter = SimpleDateFormat("HH:mm", Locale.US)

    fun todayString(): String = synchronized(dateFormatter) {
        dateFormatter.format(Date())
    }

    fun offsetDateString(daysOffset: Int): String = synchronized(dateFormatter) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        dateFormatter.format(cal.time)
    }

    fun formatDateDisplay(yyyyMmDd: String): String = synchronized(dateFormatter) {
        val parsed = runCatching { dateFormatter.parse(yyyyMmDd) }.getOrNull()
        if (parsed != null) headerDateFormatter.format(parsed) else yyyyMmDd
    }

    fun formatHeaderDate(now: Date = Date()): String = synchronized(headerDateFormatter) {
        headerDateFormatter.format(now)
    }

    fun formatDigitalClock(now: Date = Date()): String = synchronized(clockFormatter) {
        clockFormatter.format(now)
    }

    fun formatCurrentHM(now: Date = Date()): String = synchronized(hmFormatter) {
        hmFormatter.format(now)
    }

    fun defaultNextHourHM(): String = synchronized(hmFormatter) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        hmFormatter.format(cal.time)
    }

    fun snoozeTimeHM(minutes: Int): String = synchronized(hmFormatter) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, minutes)
        hmFormatter.format(cal.time)
    }

    fun format12h(time24: String): String {
        val parts = time24.split(":")
        if (parts.size < 2) return time24
        val hour24 = parts[0].toIntOrNull() ?: return time24
        val min = parts[1].padStart(2, '0')
        val amPm = if (hour24 >= 12) "PM" else "AM"
        val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
        return "$hour12:$min $amPm"
    }
}
