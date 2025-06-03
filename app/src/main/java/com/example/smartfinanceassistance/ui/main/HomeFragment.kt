package com.example.smartfinanceassistance.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.externalApp.CallActivity

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = prefs.getString("nickname", "사용자")
        view.findViewById<TextView>(R.id.textGreeting).text = "${nickname}님,\n오늘도 금융사기 조심하세요!"

        // 카드 클릭 리스너 등록
        view.findViewById<CardView>(R.id.cardQuiz).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_quizFragment)
        }

        // 외부 앱 연결: 전화
        view.findViewById<CardView>(R.id.cardCall).setOnClickListener {
            val intent = Intent(requireContext(), CallActivity::class.java)
            startActivity(intent)
        }

//        view.findViewById<CardView>(R.id.cardCases).setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_casesFragment)
//        }
//
//        view.findViewById<CardView>(R.id.cardAnalysis).setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_analysisFragment)
//        }
//
//        view.findViewById<CardView>(R.id.cardMap).setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_mapFragment)
//        }
//
//
//        view.findViewById<CardView>(R.id.cardGallery).setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_galleryFragment)
//        }

        return view
    }
}