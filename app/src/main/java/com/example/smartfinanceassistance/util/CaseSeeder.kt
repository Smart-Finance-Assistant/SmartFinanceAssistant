package com.example.smartfinanceassistance.util

import android.content.Context
import com.example.smartfinanceassistance.data.db.AppDatabase
import com.example.smartfinanceassistance.data.db.CaseEntity

object CaseSeeder {
    fun getSample(): List<CaseEntity> {
        return listOf(
            // 투자 사기 (5개)
            CaseEntity(type = "투자 사기", content = "고수익 보장 문자 후 입금 유도"),
            CaseEntity(type = "투자 사기", content = "가짜 앱 설치 유도 후 개인정보 탈취"),
            CaseEntity(type = "투자 사기", content = "유명인 사칭 투자 권유 후 사기"),
            CaseEntity(type = "투자 사기", content = "가상화폐 투자 사기로 수억원 피해"),
            CaseEntity(type = "투자 사기", content = "해외선물 투자 사기로 전 재산 손실"),

            // 스미싱 (5개)
            CaseEntity(type = "스미싱", content = "택배 문자로 악성 앱 설치 유도"),
            CaseEntity(type = "스미싱", content = "은행 사칭 문자로 개인정보 탈취"),
            CaseEntity(type = "스미싱", content = "코로나 지원금 문자로 피싱 사이트 접속 유도"),
            CaseEntity(type = "스미싱", content = "교통법규 위반 과태료 문자 사기"),
            CaseEntity(type = "스미싱", content = "카드 사용 알림 문자로 앱 설치 유도"),

            // 메신저 피싱 (5개)
            CaseEntity(type = "메신저 피싱", content = "지인 사칭 카카오톡으로 돈 요구"),
            CaseEntity(type = "메신저 피싱", content = "해킹된 계정으로 급전 요구 메시지"),
            CaseEntity(type = "메신저 피싱", content = "가족 사칭 응급상황 핑계로 송금 요구"),
            CaseEntity(type = "메신저 피싱", content = "동창 사칭으로 사업자금 명목 사기"),
            CaseEntity(type = "메신저 피싱", content = "연인 사칭으로 생활비 명목 피해"),

            // 보이스피싱 (5개)
            CaseEntity(type = "보이스 피싱", content = "경찰 사칭 전화로 안전계좌 이체 요구"),
            CaseEntity(type = "보이스 피싱", content = "검찰청 사칭으로 수사협조 명목 사기"),
            CaseEntity(type = "보이스 피싱", content = "금융감독원 사칭 계좌조사 명목 피해"),
            CaseEntity(type = "보이스 피싱", content = "국정원 사칭 국가기밀 누설 협박"),
            CaseEntity(type = "보이스 피싱", content = "은행 직원 사칭 대출 명목 개인정보 탈취")
        )
    }
}