package com.example.smartfinanceassistance.ui.quiz

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.smartfinanceassistance.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ResultFragment : Fragment(R.layout.fragment_result) {
    private lateinit var viewModel: QuizViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[QuizViewModel::class.java]

        val (correct, total) = viewModel.getOverallScore()
        val weakTypes = viewModel.getWeakTypes()
        val mostWeakType = viewModel.getMostWeakType()

        // UI 업데이트
        setupResultUI(view, correct, total, mostWeakType, weakTypes)

        // Firebase에 저장 (모든 유형별 점수 저장)
        saveResultsToFirebase(correct, total, weakTypes)

        // 취약 유형 사례 확인 버튼 설정
        setupWeakTypeCaseButtons(view, weakTypes)
    }

    private fun setupResultUI(view: View, correct: Int, total: Int, mostWeakType: Map<String, Any>?, weakTypes: List<String>) {
        // 정답 수 표시
        view.findViewById<TextView>(R.id.correctAnswersText).text = "$correct / $total"

        // 정답률 표시
        val accuracy = if (total > 0) (correct * 100 / total) else 0
        view.findViewById<TextView>(R.id.accuracyText).text = "$accuracy%"

        // 정답률에 따른 색상 변경
        val accuracyTextView = view.findViewById<TextView>(R.id.accuracyText)
        when {
            accuracy >= 80 -> accuracyTextView.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            accuracy >= 60 -> accuracyTextView.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
            else -> accuracyTextView.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        }

        // 취약 유형 표시 (Map에서 안전하게 값 추출)
        val weakTypeTextView = view.findViewById<TextView>(R.id.weakTypeText)
        if (mostWeakType != null) {
            val type = mostWeakType["type"] as? String ?: "알 수 없음"
            val percentage = mostWeakType["percentage"] as? Int ?: 0
            weakTypeTextView.text = "${type} (${percentage}%)"
        } else {
            weakTypeTextView.text = "없음\n(모든 유형 양호)"
            weakTypeTextView.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        }
    }

    private fun setupWeakTypeCaseButtons(view: View, weakTypes: List<String>) {
        val weakTypeCaseContainer = view.findViewById<LinearLayout>(R.id.weakTypeCaseContainer)
        val weakTypeCaseTitle = view.findViewById<TextView>(R.id.weakTypeCaseTitle)
        val buttonYesCase = view.findViewById<Button>(R.id.buttonYesCase)
        val buttonNoCase = view.findViewById<Button>(R.id.buttonNoCase)

        if (weakTypes.isNotEmpty()) {
            // 취약한 유형이 있는 경우 - 버튼 컨테이너 표시
            weakTypeCaseContainer.visibility = View.VISIBLE

            // 취약한 유형들을 문자열로 표시
            val weakTypeNames = weakTypes.joinToString(", ")
            weakTypeCaseTitle.text = "취약 유형(${weakTypeNames})의\n사례를 확인하시겠습니까?"

            // 예 버튼 - ViewPager로 이동
            buttonYesCase.setOnClickListener {
                Log.d("ResultFragment", "취약 유형 사례 확인 - 유형들: $weakTypes")
                val bundle = bundleOf("weakTypes" to weakTypes.toTypedArray())
                findNavController().navigate(R.id.action_resultFragment_to_weakTypePagerFragment, bundle)
            }

            // 아니요 버튼 - 홈으로 이동
            buttonNoCase.setOnClickListener {
                Log.d("ResultFragment", "홈으로 이동")
                findNavController().navigate(R.id.action_resultFragment_to_homeFragment)
            }
        } else {
            // 취약한 유형이 없는 경우 - 버튼 컨테이너 숨김
            weakTypeCaseContainer.visibility = View.GONE

            // 홈으로 돌아가는 버튼만 표시 (기존 버튼 활용 가능)
            view.findViewById<Button>(R.id.buttonFinish)?.apply {
                visibility = View.VISIBLE
                text = "홈으로"
                setOnClickListener {
                    findNavController().navigate(R.id.action_resultFragment_to_homeFragment)
                }
            }
        }
    }

    private fun saveResultsToFirebase(correct: Int, total: Int, weakTypes: List<String>) {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = prefs.getString("nickname", null)

        if (nickname != null) {
            val db = FirebaseFirestore.getInstance()
            val allScores = viewModel.getAllTypeScores()
            val scoreData = allScores.associate { scoreMap ->
                val type = scoreMap["type"] as? String ?: "알 수 없음"
                val correctCount = scoreMap["correctCount"] as? Int ?: 0
                val totalCount = scoreMap["totalCount"] as? Int ?: 0
                val percentage = scoreMap["percentage"] as? Int ?: 0

                type to mapOf(
                    "correct" to correctCount,
                    "total" to totalCount,
                    "percentage" to percentage
                )
            }

            db.collection("quiz_scores")
                .document(nickname)
                .set(mapOf(
                    "weakTypes" to weakTypes,
                    "typeScores" to scoreData,
                    "overallScore" to mapOf("correct" to correct, "total" to total),
                    "timestamp" to System.currentTimeMillis()
                ), SetOptions.merge())
                .addOnSuccessListener { Log.d("Firebase", "퀴즈 결과 저장 성공") }
                .addOnFailureListener { Log.e("Firebase", "저장 실패", it) }
        }
    }
}