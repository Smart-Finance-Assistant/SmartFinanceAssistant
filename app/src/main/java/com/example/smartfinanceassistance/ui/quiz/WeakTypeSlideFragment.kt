package com.example.smartfinanceassistance.ui.quiz

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.smartfinanceassistance.R

class WeakTypeSlideFragment : Fragment(R.layout.fragment_weak_type_slide) {

    private val type: String by lazy {
        arguments?.getString("type") ?: "알 수 없음"
    }

    private lateinit var viewModel: CaseViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById<TextView>(R.id.typeTextView)
        textView.text = "취약 유형: $type"

        viewModel = ViewModelProvider(this)[CaseViewModel::class.java]
        viewModel.loadCases(type)

        viewModel.cases.observe(viewLifecycleOwner) { cases ->
            val caseList = cases.joinToString("\n\n") { "- ${it.content}" }
            textView.text = "취약 유형: $type\n\n사례:\n$caseList"
        }
    }


    companion object {
        fun newInstance(type: String) = WeakTypeSlideFragment().apply {
            arguments = Bundle().apply { putString("type", type) }
        }
    }
}
