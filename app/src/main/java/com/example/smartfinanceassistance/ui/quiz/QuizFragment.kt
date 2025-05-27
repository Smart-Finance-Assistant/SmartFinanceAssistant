package com.example.smartfinanceassistance.ui.quiz

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
            val question = quizzes[currentIndex].question
            Log.d("QuizFragment", "표시할 문제: $question")
            view?.findViewById<TextView>(R.id.questionText)?.text = question
        } else {
            Log.d("QuizFragment", "모든 문제 끝. 결과 화면 이동")
            navigateToResult()
        }
    }

    private fun handleAnswer(choice: Boolean) {
        viewModel.recordAnswer(quizzes[currentIndex], choice)
        currentIndex++
        showQuestion()
    }

    private fun navigateToResult() {
        findNavController().navigate(R.id.action_quizFragment_to_resultFragment)
    }
}
