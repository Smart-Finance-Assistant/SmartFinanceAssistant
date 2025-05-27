package com.example.smartfinanceassistance.util

import android.content.Context
import com.example.smartfinanceassistance.data.db.AppDatabase
import com.example.smartfinanceassistance.data.db.CaseEntity

object CaseSeeder {
    fun getSample(): List<CaseEntity> {
        return listOf(
            CaseEntity(type = "투자 사기", content = "고수익 보장 문자 후 입금 유도"),
            CaseEntity(type = "투자 사기", content = "가짜 앱 설치 유도 후 개인정보 탈취"),
            CaseEntity(type = "스미싱", content = "택배 문자로 악성 앱 설치 유도")
        )
    }
}