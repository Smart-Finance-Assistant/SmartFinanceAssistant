package com.example.smartfinanceassistance.ui.analysis

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartfinanceassistance.R
import com.google.firebase.firestore.FirebaseFirestore

class WeakTypeAnalysisFragment : Fragment(R.layout.fragment_weak_type_analysis) {

    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val listView = view.findViewById<ListView>(R.id.weakTypeListView)
        val backButton = view.findViewById<Button>(R.id.buttonBackToHome)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = prefs.getString("nickname", null) ?: return

        db.collection("quiz_scores").document(nickname).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val weakTypes = document.get("weakTypes") as? List<*>
                    if (!weakTypes.isNullOrEmpty()) {
                        val stringList = weakTypes.filterIsInstance<String>()
                        listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, stringList)

                        listView.setOnItemClickListener { _, _, position, _ ->
                            val selectedType = stringList[position]
                            val action = WeakTypeAnalysisFragmentDirections
                                .actionWeakTypeAnalysisFragmentToCaseListFragment(selectedType)
                            findNavController().navigate(action)
                        }
                    }
                }
            }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_weakTypeAnalysisFragment_to_homeFragment)
        }
    }
}