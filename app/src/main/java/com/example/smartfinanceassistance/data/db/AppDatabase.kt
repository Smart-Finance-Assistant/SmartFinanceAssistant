package com.example.smartfinanceassistance.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [QuizEntity::class, CaseEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun caseDao(): CaseDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiz_database"
                )
                    .fallbackToDestructiveMigration() // 개발 중이면 이걸로 충분함
                    .build()
            }
            return INSTANCE!!
        }
    }
}
