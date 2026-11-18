package com.example.data.remote

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Student",
    val email: String? = null,
    val board: String = "CBSE",
    val studentClass: String = "Class 11",
    val stream: String = "Science PCM",
    val targetExam: String = "JEE Main",
    val onboardingCompleted: Boolean = false,
    val totalTestsTaken: Long = 0L,
    val bestAccuracy: Long = 0L,
    val streakDays: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val raw = mutableMapOf<String, Any?>(
            "userId" to userId,
            "displayName" to displayName.trim().ifBlank { "Student" }.take(80),
            "email" to email?.trim()?.takeIf { it.isNotEmpty() }?.take(200),
            "board" to board,
            "studentClass" to studentClass,
            "stream" to stream,
            "targetExam" to targetExam,
            "onboardingCompleted" to onboardingCompleted,
            "totalTestsTaken" to totalTestsTaken.coerceIn(0L, 100000L),
            "bestAccuracy" to bestAccuracy.coerceIn(0L, 100L),
            "streakDays" to streakDays.coerceIn(0L, 10000L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        @Suppress("UNCHECKED_CAST")
        return raw.filterValues { it != null } as Map<String, Any>
    }
}

@Keep
@IgnoreExtraProperties
data class StudyNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "Physics",
    val content: String = "",
    val pinned: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.trim().take(160),
        "subject" to subject.trim().take(60),
        "content" to content.take(10000),
        "pinned" to pinned,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class PdfDocumentMeta(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "General",
    val uriString: String = "",
    val pageCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.trim().take(200),
        "subject" to subject.trim().take(60),
        "uriString" to uriString.take(1000),
        "pageCount" to pageCount.coerceIn(0L, 50000L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class TestAttempt(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val track: String = "Board Exams",
    val subject: String = "Physics",
    val totalQuestions: Long = 1L,
    val correctCount: Long = 0L,
    val score: Long = 0L,
    val maxScore: Long = 1L,
    val accuracyPercent: Long = 0L,
    val durationSeconds: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.trim().take(160),
        "track" to track,
        "subject" to subject.trim().take(60),
        "totalQuestions" to totalQuestions.coerceIn(1L, 200L),
        "correctCount" to correctCount.coerceIn(0L, totalQuestions.coerceIn(1L, 200L)),
        "score" to score.coerceIn(-200L, 800L),
        "maxScore" to maxScore.coerceIn(1L, 800L),
        "accuracyPercent" to accuracyPercent.coerceIn(0L, 100L),
        "durationSeconds" to durationSeconds.coerceIn(0L, 86400L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}
