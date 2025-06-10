package com.example.smartfinanceassistance.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.externalApp.CallActivity
import com.example.smartfinanceassistance.ui.quiz.QuizViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.smartfinanceassistance.image_analysis.GallerySelectActivity

class HomeFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 닉네임 설정
        setupGreeting(view)

        // 취약 유형 동적 표시
        setupWeakTypeDisplay(view)

        // 퀴즈 카드 클릭 - 새 퀴즈 시작 전 초기화
        view.findViewById<CardView>(R.id.cardQuiz).setOnClickListener {
            Log.d("HomeFragment", "퀴즈 카드 클릭 - 새 퀴즈 시작")

            // QuizViewModel 초기화 (이전 답변 데이터 삭제)
            try {
                val quizViewModel = ViewModelProvider(requireActivity())[QuizViewModel::class.java]
                val previousAnswerCount = quizViewModel.userAnswers.size
                quizViewModel.resetQuiz()
                Log.d("HomeFragment", "퀴즈 데이터 초기화 완료 - 이전 답변 ${previousAnswerCount}개 삭제")
            } catch (e: Exception) {
                Log.e("HomeFragment", "QuizViewModel 초기화 실패: $e")
            }

            // 퀴즈 화면으로 이동
            findNavController().navigate(R.id.action_homeFragment_to_quizFragment)
        }

        view.findViewById<CardView>(R.id.cardCases).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_casesFragment)
        }

        view.findViewById<CardView>(R.id.cardAnalysis).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_weakTypeAnalysisFragment)
        }

        // 외부 앱 연결: 전화
        view.findViewById<CardView>(R.id.cardCall).setOnClickListener {
            val intent = Intent(requireContext(), CallActivity::class.java)
            startActivity(intent)
        }

        // 외부 앱 연결: 지도
        view.findViewById<CardView>(R.id.cardMap).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_mapFragment)
        }

        view.findViewById<CardView>(R.id.cardGallery).setOnClickListener {
            val intent = Intent(requireContext(), GallerySelectActivity::class.java)
            startActivity(intent)
        }

        // 🆕 게시판 카드 클릭
        view.findViewById<CardView>(R.id.cardBoard).setOnClickListener {
            Log.d("HomeFragment", "게시판 카드 클릭")
            findNavController().navigate(R.id.action_homeFragment_to_boardListFragment)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // 화면이 다시 보일 때마다 취약 유형 업데이트
        view?.let { setupWeakTypeDisplay(it) }
    }

    private fun setupGreeting(view: View) {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = prefs.getString("nickname", "사용자") ?: "사용자"
        view.findViewById<TextView>(R.id.textGreeting).text = "${nickname}님,\n오늘도 금융사기 조심하세요!"
    }

    private fun setupWeakTypeDisplay(view: View) {
        val weakTypeText = view.findViewById<TextView>(R.id.textWeakType) ?: return
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = prefs.getString("nickname", null)

        Log.d("HomeFragment", "취약 유형 업데이트 시작 - 닉네임: $nickname")

        if (nickname == null) {
            weakTypeText.text = " 취약 유형 분석(퀴즈를 먼저 풀어보세요)"
            Log.d("HomeFragment", "닉네임 없음")
            return
        }

        // 1. 먼저 QuizViewModel에서 현재 세션 데이터 확인
        try {
            val quizViewModel = ViewModelProvider(requireActivity())[QuizViewModel::class.java]
            Log.d("HomeFragment", "QuizViewModel 사용자 답변 개수: ${quizViewModel.userAnswers.size}")

            if (quizViewModel.userAnswers.isNotEmpty()) {
                // 방금 퀴즈를 완료한 경우
                val mostWeakType = quizViewModel.getMostWeakType()
                if (mostWeakType != null) {
                    val weakType = mostWeakType["type"] as String
                    weakTypeText.text = " 취약 유형 분석($weakType)"
                    Log.d("HomeFragment", "ViewModel에서 취약유형: $weakType")
                    return
                } else {
                    // 취약 유형이 없는 경우 (모든 유형에서 양호)
                    weakTypeText.text = " 취약 유형 분석(양호)"
                    Log.d("HomeFragment", "ViewModel에서 취약유형 없음")
                    return
                }
            }
        } catch (e: Exception) {
            Log.w("HomeFragment", "QuizViewModel 접근 실패: $e")
        }

        // 2. Firebase에서 저장된 취약 유형 가져오기
        Log.d("HomeFragment", "Firebase에서 데이터 조회 시작")
        db.collection("quiz_scores").document(nickname).get()
            .addOnSuccessListener { document ->
                Log.d("HomeFragment", "Firebase 조회 성공 - 문서 존재: ${document.exists()}")

                if (document.exists()) {
                    val weakTypes = document.get("weakTypes") as? List<*>
                    Log.d("HomeFragment", "Firebase weakTypes: $weakTypes")

                    if (!weakTypes.isNullOrEmpty()) {
                        val primaryWeakType = weakTypes[0].toString()
                        weakTypeText.text = " 취약 유형 분석($primaryWeakType)"
                        Log.d("HomeFragment", "Firebase에서 취약유형: $primaryWeakType")
                    } else {
                        weakTypeText.text = " 취약 유형 분석(양호)"
                        Log.d("HomeFragment", "Firebase - 취약 유형 없음")
                    }
                } else {
                    weakTypeText.text = " 취약 유형 분석(퀴즈를 먼저 풀어보세요)"
                    Log.d("HomeFragment", "Firebase 문서 없음")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Firebase 조회 실패", exception)
                weakTypeText.text = " 취약 유형 분석(표)"
            }
    }
}