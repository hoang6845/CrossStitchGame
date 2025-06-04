package com.example.crossstitch.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.crossstitch.databinding.FragmentMenuPatternContainerBinding
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.CategoryPagerAdapter
import com.example.crossstitch.viewmodel.PatternViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class menuPatternContainer : Fragment() {
    private lateinit var menuBinding:FragmentMenuPatternContainerBinding
    private var adapter:CategoryPagerAdapter? = null
    private lateinit var viewModel: PatternViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        menuBinding = FragmentMenuPatternContainerBinding.inflate(inflater, container, false)
        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)


        CoroutineScope(Dispatchers.IO).launch {
            val categories = (viewModel.findAllCategory())
            withContext(Dispatchers.Main){
                val currentList = mutableListOf("All") + categories
                viewModel.setCategories(currentList)
                adapter = viewModel.categories.value?.let {
                    CategoryPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle,
                        it
                    )
                }
                menuBinding.viewPager.adapter = adapter
                TabLayoutMediator(menuBinding.tabCategory, menuBinding.viewPager) {tab, position ->
                    tab.text = viewModel.categories.value?.get(position)
                }.attach()
            }
        }
        return menuBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}