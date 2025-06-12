package com.example.smartfinanceassistance.ui.quiz

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WeakTypePagerAdapter(
    fragment: Fragment,
    private val types: List<String>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = types.size

    override fun createFragment(position: Int): Fragment {
        val isLast = position == types.size - 1
        return WeakTypeSlideFragment.newInstance(types[position], isLast)
    }
}
