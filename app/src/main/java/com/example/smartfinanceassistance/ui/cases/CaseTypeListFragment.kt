// CaseTypeListFragment.kt
package com.example.smartfinanceassistance.ui.cases

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartfinanceassistance.R

class CaseTypeListFragment : Fragment(R.layout.fragment_case_type_list) {

    private val caseTypes = listOf("투자 사기", "스미싱", "메신저 피싱", "기관 사칭")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val listView = view.findViewById<ListView>(R.id.caseTypeListView)
        listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, caseTypes)

        listView.setOnItemClickListener { _, _, position, _ ->
            val action = CaseTypeListFragmentDirections
                .actionCaseTypeListFragmentToCaseListFragment(caseTypes[position])
            findNavController().navigate(action)
        }
    }
}
