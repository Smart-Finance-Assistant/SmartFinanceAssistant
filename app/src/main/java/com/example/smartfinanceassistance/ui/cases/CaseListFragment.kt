package com.example.smartfinanceassistance.ui.cases

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.data.db.AppDatabase
import kotlinx.coroutines.launch

class CaseListFragment : Fragment(R.layout.fragment_case_list) {

    private val args: CaseListFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val caseType = args.caseType

        // 제목 설정
        setupTitle(view, caseType)

        // 사례 데이터 로드
        loadCases(view, caseType)

        // 홈으로 버튼
        view.findViewById<Button>(R.id.buttonBackToHome).setOnClickListener {
            findNavController().navigate(R.id.action_caseListFragment_to_homeFragment)
        }
    }

    private fun setupTitle(view: View, caseType: String) {
        val titleText = view.findViewById<TextView>(R.id.titleText)
        val iconText = view.findViewById<TextView>(R.id.typeIconText)

        when (caseType) {
            "보이스피싱" -> {
                titleText.text = "$caseType 사례"
                iconText.text = "📞"
            }
            "스미싱" -> {
                titleText.text = "$caseType 사례"
                iconText.text = "💬"
            }
            "메신저 피싱" -> {
                titleText.text = "$caseType 사례"
                iconText.text = "💌"
            }
            "투자 사기" -> {
                titleText.text = "$caseType 사례"
                iconText.text = "💰"
            }
            else -> {
                titleText.text = "사례 목록"
                iconText.text = "📋"
            }
        }
    }

    private fun loadCases(view: View, caseType: String) {
        val container = view.findViewById<LinearLayout>(R.id.caseContainer)

        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(requireContext())
                val cases = database.caseDao().getCasesByType(caseType)

                if (cases.isNotEmpty()) {
                    cases.forEachIndexed { index, caseEntity ->
                        val caseView = createCaseView(index + 1, caseEntity.content)
                        container.addView(caseView)
                    }
                } else {
                    val noCaseView = createNoCaseView()
                    container.addView(noCaseView)
                }
            } catch (e: Exception) {
                val errorView = createErrorView()
                container.addView(errorView)
            }
        }
    }

    private fun createCaseView(number: Int, content: String): View {
        val caseLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 20, 20, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            // 카드 배경 설정
            setBackgroundResource(R.drawable.case_item_background)
        }

        // 번호 표시
        val numberView = TextView(requireContext()).apply {
            text = "$number"
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.white, null))
            setPadding(16, 12, 16, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16, 0)
            }
            // 번호 배경 (원형)
            setBackgroundResource(R.drawable.case_number_background)
            gravity = android.view.Gravity.CENTER
        }

        // 내용 표시
        val contentView = TextView(requireContext()).apply {
            text = content
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black, null))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(0, 8, 0, 8)
            setLineSpacing(4f, 1.2f) // 줄 간격
        }

        caseLayout.addView(numberView)
        caseLayout.addView(contentView)

        return caseLayout
    }

    private fun createNoCaseView(): View {
        return TextView(requireContext()).apply {
            text = "등록된 사례가 없습니다."
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }
    }

    private fun createErrorView(): View {
        return TextView(requireContext()).apply {
            text = "사례를 불러오는 중 오류가 발생했습니다."
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }
    }
}