package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ChapterGoal
import com.example.model.GoalSpan
import com.example.model.RepeatMode
import com.example.model.StudyTask

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val date: String,
    val time: String,
    val repeatCode: String,
    val notes: String,
    val completed: Boolean,
    val celebrated: Boolean,
    val firedKey: String,
    val createdAt: Long
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
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(task: StudyTask): StudyTaskEntity = StudyTaskEntity(
            id = task.id,
            title = task.title,
            subject = task.subject,
            date = task.date,
            time = task.time,
            repeatCode = task.repeatMode.code,
            notes = task.notes,
            completed = task.completed,
            celebrated = task.celebrated,
            firedKey = task.firedKey,
            createdAt = task.createdAt
        )
    }
}

@Entity(tableName = "chapter_goals")
data class ChapterGoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val spanLabel: String,
    val subject: String,
    val done: Boolean,
    val celebrated: Boolean,
    val createdAt: Long
) {
    fun toDomain(): ChapterGoal = ChapterGoal(
        id = id,
        title = title,
        span = GoalSpan.fromLabel(spanLabel),
        subject = subject,
        done = done,
        celebrated = celebrated,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(goal: ChapterGoal): ChapterGoalEntity = ChapterGoalEntity(
            id = goal.id,
            title = goal.title,
            spanLabel = goal.span.label,
            subject = goal.subject,
            done = goal.done,
            celebrated = goal.celebrated,
            createdAt = goal.createdAt
        )
    }
}
