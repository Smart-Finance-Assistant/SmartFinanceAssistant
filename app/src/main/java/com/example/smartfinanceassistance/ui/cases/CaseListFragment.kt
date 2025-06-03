package com.example.smartfinanceassistance.ui.cases

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.ui.quiz.CaseViewModel

class CaseListFragment : Fragment(R.layout.fragment_case_list) {

    private val viewModel: CaseViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val type = arguments?.getString("caseType") ?: return
        val listView = view.findViewById<ListView>(R.id.caseListView)

        viewModel.loadCases(type)

        viewModel.cases.observe(viewLifecycleOwner) { caseList ->
            val contents = caseList.map { it.content }
            listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, contents)
        }

        // ✅ “홈으로” 버튼
        val backButton = view.findViewById<Button>(R.id.buttonBackToHome)
        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_caseListFragment_to_homeFragment)
        }
    }

}
