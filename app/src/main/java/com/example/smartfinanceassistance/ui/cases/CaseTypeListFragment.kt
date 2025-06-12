package com.example.smartfinanceassistance.ui.cases

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.ui.quiz.QuizViewModel
import com.google.firebase.firestore.FirebaseFirestore

class CaseTypeListFragment : Fragment(R.layout.fragment_case_type_list) {

    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 닉네임 및 취약 유형 설정
        setupUserInfo(view)

        // 각 카드에 클릭 리스너 설정
        setupCardClickListeners(view)
    }

    private fun setupUserInfo(view: View) {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedNickname = prefs.getString("nickname", null)

        // 닉네임 표시
        val displayName = savedNickname ?: "사용자"
        view.findViewById<TextView>(R.id.greetingText).text =
            "${displayName}님, 오늘도 금융사기 조심하세요!"

        // 취약 유형 분석 설정
        setupWeakTypeAnalysis(view, savedNickname)
    }

    private fun setupWeakTypeAnalysis(view: View, nickname: String?) {
        val weakTypeText = view.findViewById<TextView>(R.id.weakTypeText)

        if (nickname == null) {
            weakTypeText.text = "취약 유형 분석(퀴즈를 먼저 풀어보세요)"
            return
        }

        // 1. 먼저 QuizViewModel에서 현재 세션 데이터 확인
        try {
            val quizViewModel = ViewModelProvider(requireActivity())[QuizViewModel::class.java]
            if (quizViewModel.userAnswers.isNotEmpty()) {
                // 방금 퀴즈를 완료한 경우
                val mostWeakType = quizViewModel.getMostWeakType()
                if (mostWeakType != null) {
                    val weakType = mostWeakType["type"] as String
                    weakTypeText.text = "취약 유형 분석($weakType)"
                    Log.d("CaseTypeList", "ViewModel에서 취약유형: $weakType")
                    return
                }
            }
        } catch (e: Exception) {
            Log.w("CaseTypeList", "QuizViewModel 접근 실패: $e")
        }

        // 2. Firebase에서 저장된 취약 유형 가져오기
        db.collection("quiz_scores").document(nickname).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val weakTypes = document.get("weakTypes") as? List<*>
                    if (!weakTypes.isNullOrEmpty()) {
                        val primaryWeakType = weakTypes[0].toString()
                        weakTypeText.text = "취약 유형 분석($primaryWeakType)"
                        Log.d("CaseTypeList", "Firebase에서 취약유형: $primaryWeakType")
                    } else {
                        weakTypeText.text = "취약 유형 분석(없음)"
                        Log.d("CaseTypeList", "취약 유형 없음")
                    }
                } else {
                    weakTypeText.text = "취약 유형 분석(퀴즈를 먼저 풀어보세요)"
                    Log.d("CaseTypeList", "Firebase 문서 없음")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CaseTypeList", "Firebase 조회 실패", exception)
                weakTypeText.text = "취약 유형 분석(표)"
            }
    }

    private fun setupCardClickListeners(view: View) {
        // 보이스피싱 카드 (LinearLayout으로 변경)
        view.findViewById<LinearLayout>(R.id.cardVoicePhishing).setOnClickListener {
            navigateToCaseList("보이스피싱")
        }

        // 스미싱 카드
        view.findViewById<LinearLayout>(R.id.cardSmishing).setOnClickListener {
            navigateToCaseList("스미싱")
        }

        // 메신저 피싱 카드
        view.findViewById<LinearLayout>(R.id.cardMessengerPhishing).setOnClickListener {
            navigateToCaseList("메신저 피싱")
        }

        // 투자 사기 카드
        view.findViewById<LinearLayout>(R.id.cardInvestmentFraud).setOnClickListener {
            navigateToCaseList("투자 사기")
        }
    }

    private fun navigateToCaseList(caseType: String) {
        val action = CaseTypeListFragmentDirections
            .actionCaseTypeListFragmentToCaseListFragment(caseType)
        findNavController().navigate(action)
    }
}