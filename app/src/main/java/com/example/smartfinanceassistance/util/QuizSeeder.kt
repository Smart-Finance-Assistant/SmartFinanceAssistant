package com.example.smartfinanceassistance.util

import com.example.smartfinanceassistance.data.db.QuizEntity

object QuizSeeder {
    fun getSample(): List<QuizEntity> = listOf(
        QuizEntity(question = "보이스피싱은 경찰을 사칭한다.", answer = true, type = "보이스피싱"),
        QuizEntity(question = "스미싱은 문자 링크를 클릭해도 안전하다.", answer = false, type = "스미싱"),
        QuizEntity(question = "메신저 피싱은 지인을 사칭한다.", answer = true, type = "메신저 피싱"),
        QuizEntity(question = "투자 사기는 원금 보장을 약속한다.", answer = false, type = "투자 사기"),
        // 16개 더 추가해서 총 20개 만들기!
    )
}