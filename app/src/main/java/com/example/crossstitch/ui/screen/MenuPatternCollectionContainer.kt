package com.example.crossstitch.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.crossstitch.databinding.FragmentMenuPatternCollectionContainerBinding
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.CollectionPagerAdapter
import com.example.crossstitch.viewmodel.PatternViewModel
import com.google.android.material.tabs.TabLayoutMediator


class MenuPatternCollectionContainer : Fragment() {
    private lateinit var menuPatternCollectionBinding: FragmentMenuPatternCollectionContainerBinding
    private var adapter: CollectionPagerAdapter? = null
    private lateinit var viewModel: PatternViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val listCollectionType = mutableListOf("Completed", "In progress")
        menuPatternCollectionBinding = FragmentMenuPatternCollectionContainerBinding.inflate(layoutInflater, container, false)
        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)
        adapter = CollectionPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, listCollectionType)
        menuPatternCollectionBinding.viewpager.adapter = adapter
        TabLayoutMediator(menuPatternCollectionBinding.tablayout, menuPatternCollectionBinding.viewpager) { tab, position ->
            tab.text = listCollectionType[position]
        }.attach()

        return menuPatternCollectionBinding.root
    }

}