package com.example.crossstitch.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.crossstitch.R
import com.example.crossstitch.databinding.FragmentMenuPatternCollectionContainerBinding
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.CollectionPagerAdapter
import com.example.crossstitch.viewmodel.PatternViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MenuPatternCollectionContainer : Fragment() {
    private lateinit var menuPatternCollectionBinding: FragmentMenuPatternCollectionContainerBinding
    private var adapter: CollectionPagerAdapter? = null
    private lateinit var viewModel: PatternViewModel
    private lateinit var listCollectionType: MutableList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        listCollectionType = mutableListOf("Completed", "In progress")
        menuPatternCollectionBinding = FragmentMenuPatternCollectionContainerBinding.inflate(layoutInflater, container, false)
        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)
        adapter = CollectionPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, listCollectionType)
        menuPatternCollectionBinding.viewpager.adapter = adapter
        TabLayoutMediator(menuPatternCollectionBinding.tablayout, menuPatternCollectionBinding.viewpager) { tab, position ->
            val customView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_tab, null)
            val tv = customView.findViewById<TextView>(R.id.tv)
            tv.text = listCollectionType[position]
            tab.customView = customView
        }.attach()
        menuPatternCollectionBinding.tablayout.tabRippleColor = null
        setUpTabListener(listCollectionType)
        val currentPosition = menuPatternCollectionBinding.viewpager.currentItem
        updateTabAppearance(currentPosition, true, listCollectionType)
        return menuPatternCollectionBinding.root
    }

    private fun setUpTabListener(listCollectionType: List<String>){
        menuPatternCollectionBinding.tablayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { updateTabAppearance(it, true, listCollectionType) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.position?.let { updateTabAppearance(it, false, listCollectionType) }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun updateTabAppearance(position: Int, isSelected: Boolean, listCollectionType:List<String>) {
        val tab = menuPatternCollectionBinding.tablayout.getTabAt(position)
        val layoutRes = if (isSelected) R.layout.custom_tab_indicator else R.layout.custom_tab
        val customView = LayoutInflater.from(requireContext()).inflate(layoutRes, null)
        val tv = customView.findViewById<TextView>(R.id.tv)
        tv.text = listCollectionType[position]
        tab?.customView = customView
    }



}