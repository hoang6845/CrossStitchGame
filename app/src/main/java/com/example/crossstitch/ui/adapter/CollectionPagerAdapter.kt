package com.example.crossstitch.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.crossstitch.ui.screen.PatternMenu

class CollectionPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val collectionType: List<String>
) :FragmentStateAdapter(fragmentManager, lifecycle){
    override fun createFragment(position: Int): Fragment {
        var currentCollection = collectionType[position]
        return PatternMenu.newInstanceForCollection(currentCollection)
    }

    override fun getItemCount(): Int {
        return collectionType.size
    }
}