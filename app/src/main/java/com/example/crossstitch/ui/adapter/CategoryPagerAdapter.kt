package com.example.crossstitch.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.crossstitch.ui.screen.PatternMenu

class CategoryPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val categories:List<String>

) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return categories.size
    }

    override fun createFragment(position: Int): Fragment {
        var category = categories[position]
        return PatternMenu.newInstance(category)
    }
}