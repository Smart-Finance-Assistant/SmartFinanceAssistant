package com.example.smartfinanceassistance.util

import com.example.smartfinanceassistance.data.db.QuizEntity

object QuizSeeder {
    fun getSample(): List<QuizEntity> = listOf(
        // 보이스 피싱 (5문제)
        QuizEntity(question = "보이스피싱은 경찰을 사칭한다.", answer = true, type = "보이스피싱"),
        QuizEntity(question = "검찰청에서 개인정보를 전화로 요구할 수 있다.", answer = false, type = "보이스피싱"),
        QuizEntity(question = "금융감독원에서 계좌이체를 요구할 수 있다.", answer = false, type = "보이스피싱"),
        QuizEntity(question = "보이스피싱범은 정부기관을 사칭하여 신뢰를 얻으려 한다.", answer = true, type = "보이스피싱"),
        QuizEntity(question = "수사기관에서 안전계좌로 돈을 옮기라고 할 수 있다.", answer = false, type = "보이스피싱"),

        // 스미싱 (5문제)
        QuizEntity(question = "스미싱은 문자 링크를 클릭해도 안전하다.", answer = false, type = "스미싱"),
        QuizEntity(question = "택배 문자의 모든 링크는 안전하다.", answer = false, type = "스미싱"),
        QuizEntity(question = "스미싱은 SMS를 이용한 피싱 공격이다.", answer = true, type = "스미싱"),
        QuizEntity(question = "출처가 불분명한 문자의 링크는 클릭하지 않아야 한다.", answer = true, type = "스미싱"),
        QuizEntity(question = "은행에서 문자로 개인정보 입력을 요구할 수 있다.", answer = false, type = "스미싱"),

        // 메신저 피싱 (5문제)
        QuizEntity(question = "메신저 피싱은 지인을 사칭한다.", answer = true, type = "메신저 피싱"),
        QuizEntity(question = "카카오톡으로 온 돈 요구 메시지는 모두 진짜다.", answer = false, type = "메신저 피싱"),
        QuizEntity(question = "지인이 급하게 돈을 요구하면 전화로 확인해야 한다.", answer = true, type = "메신저 피싱"),
        QuizEntity(question = "메신저 피싱범은 해킹된 계정을 이용한다.", answer = true, type = "메신저 피싱"),
        QuizEntity(question = "메신저로 받은 링크는 항상 안전하다.", answer = false, type = "메신저 피싱"),

        // 투자 사기 (5문제)
        QuizEntity(question = "투자 사기는 원금 보장을 약속한다.", answer = false, type = "투자 사기"),
        QuizEntity(question = "고수익을 보장하는 투자는 의심해야 한다.", answer = true, type = "투자 사기"),
        QuizEntity(question = "투자 전 금융당국 등록 여부를 확인해야 한다.", answer = true, type = "투자 사기"),
        QuizEntity(question = "투자 사기범은 급하게 결정을 요구한다.", answer = true, type = "투자 사기"),
        QuizEntity(question = "모든 투자는 원금 손실 위험이 있다.", answer = true, type = "투자 사기")
    )
}