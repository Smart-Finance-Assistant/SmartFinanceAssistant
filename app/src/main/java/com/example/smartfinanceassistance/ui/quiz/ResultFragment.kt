package com.example.smartfinanceassistance.ui.quiz

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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

        // 결과 텍스트 업데이트
        val resultText = if (mostWeakType != null) {
            val type = mostWeakType["type"] as String
            val percentage = mostWeakType["percentage"] as Int
            "정답 수: $correct / $total\n정답률: ${if (total > 0) (correct * 100 / total) else 0}%\n가장 취약한 유형: ${type} (${percentage}%)"
        } else {
            "정답 수: $correct / $total\n정답률: ${if (total > 0) (correct * 100 / total) else 0}%\n취약한 유형이 없습니다!"
        }

        view.findViewById<TextView>(R.id.resultText).text = resultText

        // 🔽 Firebase에 저장 (모든 유형별 점수 저장)
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = prefs.getString("nickname", null)
        if (nickname != null) {
            val db = FirebaseFirestore.getInstance()
            val allScores = viewModel.getAllTypeScores()
            val scoreData = allScores.associate { scoreMap ->
                val type = scoreMap["type"] as String
                val correct = scoreMap["correctCount"] as Int
                val total = scoreMap["totalCount"] as Int
                val percentage = scoreMap["percentage"] as Int

                type to mapOf(
                    "correct" to correct,
                    "total" to total,
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

        view.findViewById<Button>(R.id.buttonYes).setOnClickListener {
            // 새로운 취약 유형 분석 화면으로 이동
            findNavController().navigate(R.id.action_resultFragment_to_weakTypeAnalysisFragment)
        }

        view.findViewById<Button>(R.id.buttonNo).setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_homeFragment)
        }
    }
}