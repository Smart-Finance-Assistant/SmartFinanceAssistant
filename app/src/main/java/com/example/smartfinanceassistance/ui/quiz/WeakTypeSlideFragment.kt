package com.example.smartfinanceassistance.ui.quiz

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.smartfinanceassistance.R

class WeakTypeSlideFragment : Fragment(R.layout.fragment_weak_type_slide) {

    private val type: String by lazy {
        arguments?.getString("type") ?: "알 수 없음"
    }

    private val isLast: Boolean by lazy {
        arguments?.getBoolean("isLast", false) ?: false
    }

    private lateinit var viewModel: CaseViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 유형별 UI 설정
        setupTypeUI(view)

        // 마지막 슬라이드인 경우 홈 버튼 표시
        setupFinishButton(view)

        // 사례 데이터 로드
        loadCaseData(view)
    }

    private fun setupTypeUI(view: View) {
        val typeTitle = view.findViewById<TextView>(R.id.typeTitle)
        val typeIcon = view.findViewById<TextView>(R.id.typeIcon)
        val iconContainer = view.findViewById<LinearLayout>(R.id.iconContainer)

        typeTitle.text = type

        // 유형별 아이콘 및 색상 설정
        when (type) {
            "보이스피싱" -> {
                typeIcon.text = "📞"
                iconContainer.setBackgroundColor(resources.getColor(android.R.color.holo_red_light, null))
            }
            "스미싱" -> {
                typeIcon.text = "💬"
                iconContainer.setBackgroundColor(resources.getColor(android.R.color.holo_orange_light, null))
            }
            "메신저 피싱" -> {
                typeIcon.text = "💌"
                iconContainer.setBackgroundColor(resources.getColor(android.R.color.holo_green_light, null))
            }
            "투자 사기" -> {
                typeIcon.text = "💰"
                iconContainer.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light, null))
            }
            else -> {
                typeIcon.text = "❓"
                iconContainer.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            }
        }
    }

    private fun setupFinishButton(view: View) {
        val finishButtonCard = view.findViewById<CardView>(R.id.finishButtonCard)
        val finishButton = view.findViewById<Button>(R.id.finishButton)

        if (isLast) {
            finishButtonCard.visibility = View.VISIBLE
            finishButton.setOnClickListener {
                findNavController().navigate(R.id.action_weakTypePagerFragment_to_homeFragment)
            }
        } else {
            finishButtonCard.visibility = View.GONE
        }
    }

    private fun loadCaseData(view: View) {
        viewModel = ViewModelProvider(this)[CaseViewModel::class.java]
        viewModel.loadCases(type)

        val casesContainer = view.findViewById<LinearLayout>(R.id.casesContainer)

        viewModel.cases.observe(viewLifecycleOwner) { cases ->
            casesContainer.removeAllViews()

            if (cases.isNotEmpty()) {
                cases.forEachIndexed { index, case ->
                    val caseView = createCaseView(index + 1, case.content)
                    casesContainer.addView(caseView)
                }
            } else {
                val noCaseView = createNoCaseView()
                casesContainer.addView(noCaseView)
            }
        }
    }

    private fun createCaseView(number: Int, content: String): View {
        val caseCard = CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            radius = 12f
            cardElevation = 4f
            useCompatPadding = true
        }

        val caseLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 16, 20, 16)
            setBackgroundColor(resources.getColor(android.R.color.white, null))
        }

        // 번호 표시
        val numberView = TextView(requireContext()).apply {
            text = "$number"
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.white, null))
            setPadding(12, 8, 12, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16, 0)
            }
            setBackgroundColor(resources.getColor(android.R.color.holo_blue_bright, null))
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
        }

        caseLayout.addView(numberView)
        caseLayout.addView(contentView)
        caseCard.addView(caseLayout)

        return caseCard
    }

    private fun createNoCaseView(): View {
        return TextView(requireContext()).apply {
            text = "해당 유형의 사례가 없습니다."
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }
    }

    companion object {
        fun newInstance(type: String, isLast: Boolean = false) = WeakTypeSlideFragment().apply {
            arguments = Bundle().apply {
                putString("type", type)
                putBoolean("isLast", isLast)
            }
        }
    }
}