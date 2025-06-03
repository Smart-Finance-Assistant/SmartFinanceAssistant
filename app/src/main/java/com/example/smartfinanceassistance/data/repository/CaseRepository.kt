package com.example.smartfinanceassistance.data.repository

import com.example.smartfinanceassistance.data.db.CaseDao
import com.example.smartfinanceassistance.data.db.CaseEntity

class CaseRepository(private val dao: CaseDao) {
    suspend fun getCasesByType(type: String): List<CaseEntity> = dao.getCasesByType(type)
}
