package com.example.smartfinanceassistance.ui.quiz

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.smartfinanceassistance.R

class WeakTypePagerFragment : Fragment(R.layout.fragment_weak_type_pager) {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: WeakTypePagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val types = arguments?.getStringArray("weakTypes")?.toList() ?: emptyList()

        viewPager = view.findViewById(R.id.weakTypeViewPager)
        adapter = WeakTypePagerAdapter(this, types)
        viewPager.adapter = adapter
    }
}
