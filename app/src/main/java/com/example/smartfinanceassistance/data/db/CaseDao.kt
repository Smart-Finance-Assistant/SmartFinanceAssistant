package com.example.smartfinanceassistance.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CaseDao {
    @Query("SELECT * FROM case_table WHERE type = :type")
    suspend fun getCasesByType(type: String): List<CaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cases: List<CaseEntity>)
}
