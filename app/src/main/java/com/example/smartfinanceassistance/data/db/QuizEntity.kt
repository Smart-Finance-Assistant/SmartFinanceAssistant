package com.example.smartfinanceassistance.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_table")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: Boolean,   // true = O, false = X
    val type: String       // ex: "보이스피싱", "스미싱"
)
