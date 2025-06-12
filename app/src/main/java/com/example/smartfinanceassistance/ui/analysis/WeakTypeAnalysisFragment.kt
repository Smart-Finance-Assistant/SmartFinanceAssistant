package com.example.smartfinanceassistance.ui.analysis

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.ui.quiz.QuizViewModel
import com.google.firebase.firestore.FirebaseFirestore

class WeakTypeAnalysisFragment : Fragment(R.layout.fragment_weak_type_analysis) {

    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<LinearLayout>(R.id.weakTypeContainer)
        val backButton = view.findViewById<Button>(R.id.buttonBackToHome)
        val weakTypeTitle = view.findViewById<TextView>(R.id.weakTypeTitle)

        // 닉네임 가져오기
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = prefs.getString("nickname", "사용자") ?: "사용자"

        // QuizViewModel에서 실제 데이터 가져오기 시도
        try {
            val viewModel = ViewModelProvider(requireActivity())[QuizViewModel::class.java]

            Log.d("WeakTypeAnalysis", "ViewModel 사용자 답변 개수: ${viewModel.userAnswers.size}")

            if (viewModel.userAnswers.isNotEmpty()) {
                // 퀴즈를 방금 완료한 경우: ViewModel에서 실제 데이터 사용
                Log.d("WeakTypeAnalysis", "ViewModel에서 실제 데이터 사용")
                setupUIFromViewModel(viewModel, weakTypeTitle, container)
            } else {
                // Firebase에서 저장된 데이터 사용
                Log.d("WeakTypeAnalysis", "Firebase에서 데이터 로드")
                setupUIFromFirebase(nickname, weakTypeTitle, container)
            }
        } catch (e: Exception) {
            Log.e("WeakTypeAnalysis", "ViewModel 접근 실패: $e")
            // ViewModel이 없는 경우: Firebase에서만 데이터 사용
            setupUIFromFirebase(nickname, weakTypeTitle, container)
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_weakTypeAnalysisFragment_to_homeFragment)
        }
    }

    private fun setupUIFromViewModel(
        viewModel: QuizViewModel,
        weakTypeTitle: TextView,
        container: LinearLayout
    ) {
        // 실제 퀴즈 결과에서 데이터 가져오기
        val allTypeScores = viewModel.getAllTypeScores()
        val mostWeakType = viewModel.getMostWeakType()

        Log.d("WeakTypeAnalysis", "전체 점수 데이터: $allTypeScores")
        Log.d("WeakTypeAnalysis", "가장 취약한 유형: $mostWeakType")

        // 가장 취약한 유형 표시
        if (mostWeakType != null) {
            val type = mostWeakType["type"] as String
            weakTypeTitle.text = "${type} 취약!"
        } else {
            weakTypeTitle.text = "취약한 유형이 없습니다! 🎉"
        }

        // 모든 유형별 점수 표시
        if (allTypeScores.isNotEmpty()) {
            allTypeScores.forEach { scoreMap ->
                val type = scoreMap["type"] as String
                val correct = scoreMap["correctCount"] as Int
                val total = scoreMap["totalCount"] as Int
                val isWeak = scoreMap["isWeak"] as Boolean

                Log.d("WeakTypeAnalysis", "표시할 데이터 - 유형: $type, 점수: $correct/$total, 취약: $isWeak")

                val scoreView = createScoreView(type, correct, total, isWeak)
                container.addView(scoreView)
            }
        } else {
            // 데이터가 없는 경우 메시지 표시
            val noDataView = TextView(requireContext()).apply {
                text = "퀴즈 데이터가 없습니다. 퀴즈를 먼저 풀어보세요!"
                textSize = 16f
                setPadding(32, 32, 32, 32)
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
            }
            container.addView(noDataView)
        }
    }

    private fun setupUIFromFirebase(
        nickname: String,
        weakTypeTitle: TextView,
        container: LinearLayout
    ) {
        Log.d("WeakTypeAnalysis", "Firebase에서 $nickname 사용자 데이터 조회")

        db.collection("quiz_scores").document(nickname).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("WeakTypeAnalysis", "Firebase 문서 존재")
                    val typeScores = document.get("typeScores") as? Map<*, *>
                    val weakTypes = document.get("weakTypes") as? List<*>

                    Log.d("WeakTypeAnalysis", "Firebase typeScores: $typeScores")
                    Log.d("WeakTypeAnalysis", "Firebase weakTypes: $weakTypes")

                    // 가장 취약한 유형 표시
                    if (!weakTypes.isNullOrEmpty()) {
                        val firstWeakType = weakTypes[0].toString()
                        weakTypeTitle.text = "${firstWeakType} 취약!"
                    } else {
                        weakTypeTitle.text = "취약한 유형이 없습니다! 🎉"
                    }

                    // 유형별 점수 표시
                    if (typeScores != null) {
                        typeScores.forEach { (type, scoreData) ->
                            val scoreMap = scoreData as? Map<*, *>
                            val correct = (scoreMap?.get("correct") as? Long)?.toInt() ?: 0
                            val total = (scoreMap?.get("total") as? Long)?.toInt() ?: 5
                            val percentage = (scoreMap?.get("percentage") as? Long)?.toInt() ?: 0
                            val isWeak = percentage <= 40

                            Log.d("WeakTypeAnalysis", "Firebase 데이터 - 유형: $type, 점수: $correct/$total")

                            val scoreView = createScoreView(type.toString(), correct, total, isWeak)
                            container.addView(scoreView)
                        }
                    } else {
                        showNoDataMessage(container)
                    }
                } else {
                    Log.d("WeakTypeAnalysis", "Firebase 문서 없음")
                    weakTypeTitle.text = "퀴즈를 먼저 풀어보세요!"
                    showNoDataMessage(container)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WeakTypeAnalysis", "Firebase 조회 실패", exception)
                weakTypeTitle.text = "데이터를 불러올 수 없습니다"
                showNoDataMessage(container)
            }
    }

    private fun showNoDataMessage(container: LinearLayout) {
        val noDataView = TextView(requireContext()).apply {
            text = "저장된 퀴즈 결과가 없습니다.\n홈에서 퀴즈를 먼저 풀어보세요!"
            textSize = 16f
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
        container.addView(noDataView)
    }

    // 🎨 CardView로 그림자 효과 적용
    private fun createScoreView(type: String, correct: Int, total: Int, isWeak: Boolean): View {
        // CardView 생성 (그림자 효과)
        val cardView = CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12) // 아래쪽 간격
            }
            radius = 12f
            cardElevation = 6f
            useCompatPadding = true // 그림자를 위한 패딩
        }

        // 내부 레이아웃
        val scoreLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 20, 24, 20)
            setBackgroundColor(resources.getColor(android.R.color.white, null))
        }

        // 유형 이름
        val typeNameView = TextView(requireContext()).apply {
            text = type
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black, null))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // 점수 표시
        val scoreView = TextView(requireContext()).apply {
            text = "${correct}/${total}"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            setTextColor(
                if (isWeak) resources.getColor(android.R.color.holo_red_light, null)
                else resources.getColor(android.R.color.black, null)
            )

            // 취약한 유형은 굵게 표시
            if (isWeak) {
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }

        scoreLayout.addView(typeNameView)
        scoreLayout.addView(scoreView)
        cardView.addView(scoreLayout)

        return cardView
    }
}