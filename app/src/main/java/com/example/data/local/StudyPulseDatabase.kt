package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [StudyTaskEntity::class, ChapterGoalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StudyPulseDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: StudyPulseDatabase? = null

        fun getInstance(context: Context): StudyPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyPulseDatabase::class.java,
                    "study_pulse.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
