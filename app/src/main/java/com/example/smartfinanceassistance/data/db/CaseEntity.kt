package com.example.smartfinanceassistance.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "case_table")
data class CaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val content: String
)