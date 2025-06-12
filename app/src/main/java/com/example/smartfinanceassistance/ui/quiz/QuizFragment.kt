package com.example.smartfinanceassistance.ui.quiz

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.data.db.QuizEntity
import androidx.navigation.fragment.findNavController

class QuizFragment : androidx.fragment.app.Fragment() {

    private lateinit var viewModel: QuizViewModel
    private lateinit var quizzes: List<QuizEntity>
    private var currentIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[QuizViewModel::class.java]

        // 안전장치: 비정상적으로 많은 데이터가 있으면 초기화
        if (viewModel.userAnswers.size > 20) {
            Log.w("QuizFragment", "비정상적인 데이터 감지 (${viewModel.userAnswers.size}개) - 강제 초기화")
            viewModel.resetQuiz()
        }

        Log.d("QuizFragment", "퀴즈 시작 - 현재 답변 개수: ${viewModel.userAnswers.size}")

        viewModel.loadQuizzes()

        // O/X 버튼 클릭 리스너 연결
        view.findViewById<Button>(R.id.buttonTrue).setOnClickListener {
            Log.d("QuizFragment", "O 버튼 클릭됨")
            handleAnswer(true)
        }

        view.findViewById<Button>(R.id.buttonFalse).setOnClickListener {
            Log.d("QuizFragment", "X 버튼 클릭됨")
            handleAnswer(false)
        }

        viewModel.quizzes.observe(viewLifecycleOwner) {
            Log.d("QuizFragment", "퀴즈 옵저빙됨: ${it.size}개")
            quizzes = it
            showQuestion()
        }
    }

    private fun showQuestion() {
        if (currentIndex < quizzes.size) {
            val currentQuiz = quizzes[currentIndex]
            val question = currentQuiz.question
            val type = currentQuiz.type

            Log.d("QuizFragment", "문제 ${currentIndex + 1}/20: $question (유형: $type)")

            // 진행률 업데이트
            view?.findViewById<TextView>(R.id.progressText)?.text = "${currentIndex + 1} / 20"

            // 문제 텍스트 업데이트
            view?.findViewById<TextView>(R.id.questionText)?.text = question

            // 유형별 아이콘 및 배경색 변경
            updateQuizIcon(type)

        } else {
            Log.d("QuizFragment", "모든 문제 완료 - 총 답변: ${viewModel.userAnswers.size}개")
            navigateToResult()
        }
    }

    private fun updateQuizIcon(type: String) {
        val iconContainer = view?.findViewById<LinearLayout>(R.id.iconContainer)
        val iconText = view?.findViewById<TextView>(R.id.iconText)

        when (type) {
            "보이스피싱" -> {
                iconContainer?.setBackgroundColor(resources.getColor(android.R.color.holo_red_light, null))
                iconText?.text = "📞"
            }
            "스미싱" -> {
                iconContainer?.setBackgroundColor(resources.getColor(android.R.color.holo_orange_light, null))
                iconText?.text = "💬"
            }
            "메신저 피싱" -> {
                iconContainer?.setBackgroundColor(resources.getColor(android.R.color.holo_green_light, null))
                iconText?.text = "💌"
            }
            "투자 사기" -> {
                iconContainer?.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light, null))
                iconText?.text = "💰"
            }
            else -> {
                iconContainer?.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
                iconText?.text = "❓"
            }
        }
    }

    private fun handleAnswer(choice: Boolean) {
        if (currentIndex < quizzes.size) {
            val currentQuiz = quizzes[currentIndex]
            viewModel.recordAnswer(currentQuiz, choice)

            Log.d("QuizFragment", "답변 기록 완료: ${currentQuiz.type} - ${currentQuiz.question} -> $choice")
            Log.d("QuizFragment", "현재 진행률: ${viewModel.userAnswers.size}/20")

            currentIndex++
            showQuestion()
        }
    }

    private fun navigateToResult() {
        Log.d("QuizFragment", "결과 화면으로 이동 - 최종 답변 개수: ${viewModel.userAnswers.size}")
        findNavController().navigate(R.id.action_quizFragment_to_resultFragment)
    }
}