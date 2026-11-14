package com.example.data.remote

import com.example.model.AlarmTone
import com.example.model.ChapterGoal
import com.example.model.DateTimeHelper
import com.example.model.GoalSpan
import com.example.model.RepeatMode
import com.example.model.SUBJECT_ALL
import com.example.model.StudyPreferences
import com.example.model.StudyStream
import com.example.model.StudyTask
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Student",
    val email: String? = null,
    val selectedStream: String = StudyStream.NEET.code,
    val selectedSubjectFilter: String = SUBJECT_ALL,
    val dailyTargetLectures: Int = 4,
    val dailyTargetHours: Int = 6,
    val loggedHoursToday: Double = 0.0,
    val streak: Int = 1,
    val alarmTone: String = AlarmTone.NEON.code,
    val lastLoggedDate: String = DateTimeHelper.todayString(),
    val isSeeded: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toPreferences(): StudyPreferences = StudyPreferences(
        selectedStream = StudyStream.fromCode(selectedStream),
        selectedSubjectFilter = selectedSubjectFilter.ifBlank { SUBJECT_ALL },
        dailyTargetLectures = dailyTargetLectures.coerceIn(1, 20),
        dailyTargetHours = dailyTargetHours.coerceIn(1, 18),
        loggedHoursToday = loggedHoursToday.toFloat().coerceIn(0f, 24f),
        streak = streak.coerceAtLeast(0),
        alarmTone = AlarmTone.fromCode(alarmTone),
        lastLoggedDate = lastLoggedDate.ifBlank { DateTimeHelper.todayString() },
        isSeeded = isSeeded
    )
}

@IgnoreExtraProperties
data class FirestoreStudyTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val date: String = "",
    val time: String = "",
    val repeatCode: String = RepeatMode.ONCE.code,
    val notes: String = "",
    val completed: Boolean = false,
    val celebrated: Boolean = false,
    val firedKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain(): StudyTask = StudyTask(
        id = id,
        title = title,
        subject = subject,
        date = date,
        time = time,
        repeatMode = RepeatMode.fromCode(repeatCode),
        notes = notes,
        completed = completed,
        celebrated = celebrated,
        firedKey = firedKey,
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis()
    )
}

@IgnoreExtraProperties
data class FirestoreChapterGoal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val spanLabel: String = GoalSpan.TODAY.label,
    val subject: String = "",
    val done: Boolean = false,
    val celebrated: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain(): ChapterGoal = ChapterGoal(
        id = id,
        title = title,
        span = GoalSpan.fromLabel(spanLabel),
        subject = subject,
        done = done,
        celebrated = celebrated,
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis()
    )
}
