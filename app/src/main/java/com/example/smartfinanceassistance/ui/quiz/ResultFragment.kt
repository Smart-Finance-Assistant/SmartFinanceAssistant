package com.example.smartfinanceassistance.ui.quiz

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.smartfinanceassistance.R

class ResultFragment : Fragment(R.layout.fragment_result) {
    private lateinit var viewModel: QuizViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[QuizViewModel::class.java]

        val answers = viewModel.userAnswers
        val correct = answers.count { (quiz, userChoice) -> quiz.answer == userChoice }
        val total = answers.size
        val weakTypes = viewModel.getWeakTypes()

        view.findViewById<TextView>(R.id.resultText).text =
            "정답 수: $correct / $total\n정답률: ${if (total > 0) (correct * 100 / total) else 0}% \n취약 유형: $weakTypes"

        view.findViewById<Button>(R.id.buttonYes).setOnClickListener {
            val action = ResultFragmentDirections
                .actionResultFragmentToWeakTypePagerFragment(weakTypes.toTypedArray())
            findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.buttonNo).setOnClickListener {
            //findNavController().navigate(R.id.action_resultFragment_to_mainFragment)
        }
    }
}