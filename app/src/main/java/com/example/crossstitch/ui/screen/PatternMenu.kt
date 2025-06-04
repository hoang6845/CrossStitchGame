package com.example.crossstitch.ui.screen


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.crossstitch.R
import com.example.crossstitch.databinding.FragmentPatternMenuBinding
import com.example.crossstitch.di.Constants
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.PatternAdapter
import com.example.crossstitch.ui.adapter.irv.IPatternRv
import com.example.crossstitch.viewmodel.PatternViewModel

lateinit var patternViewBinding: FragmentPatternMenuBinding
class PatternMenu : Fragment() {
    var adapter:PatternAdapter? = null
    var navController: NavController? = null
    private lateinit var viewModel: PatternViewModel
    private var category:String? = null
    private var collectionType:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString("category")
        collectionType = arguments?.getString("collectionType")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding.toolbar.visibility = View.VISIBLE
        navController = findNavController()
        patternViewBinding = FragmentPatternMenuBinding.inflate(inflater, container, false)

        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)

        adapter = PatternAdapter(object : IPatternRv{
            override fun onClickItem(position: Int) {
                val bundle= Bundle()
                bundle.putInt("patternId", this@PatternMenu.adapter?.listPattern?.get(position)?.id!!)
                bundle.putString("type", "App")
                navController!!.navigate(R.id.gameManager, bundle)
            }

        }, listOf(), listOf(), mutableListOf())

        patternViewBinding.rv.adapter = adapter
        patternViewBinding.rv.layoutManager = GridLayoutManager(
            requireContext(),
            2,
            GridLayoutManager.VERTICAL,
            false
        )

        if (category!=null){
            viewModel.listPatternLiveData.observe(viewLifecycleOwner, {
                    list ->updateAdapter(list, viewModel.listGameProgressLiveData.value?: emptyList())

            })

            viewModel.listGameProgressLiveData.observe(viewLifecycleOwner, {
                    list -> updateAdapter(viewModel.listPatternLiveData.value?: emptyList(), list)
            })
        }else if (collectionType!=null){
            viewModel.listPatternLiveData.observe(viewLifecycleOwner, {
                    list ->updateAdapterByCollection(list, viewModel.listGameProgressLiveData.value?: emptyList())

            })

            viewModel.listGameProgressLiveData.observe(viewLifecycleOwner, {
                    list -> updateAdapterByCollection(viewModel.listPatternLiveData.value?: emptyList(), list)
            })
        }

        return patternViewBinding.root
    }



    private fun getPatternByCategory(category: String, listPattern: List<PatternData>): List<PatternData> {
        return when (category){
            "Cartoon" -> listPattern.filter { it.Category.equals("Cartoon") }
            "Flowers" -> listPattern.filter { it.Category.equals("Flowers") }
            "Animals" -> listPattern.filter { it.Category.equals("Animals") }
            "All" -> listPattern.filter { it.Category != null }
            else -> emptyList()
        }
    }

    private fun getProgressByCollection(collectionType: String, listProgress: List<GameProgress>): List<GameProgress> {
        return when (collectionType){
            "In progress" -> listProgress.filter {(it.completedCells in 0..Constants.Cells-1 && it.mistake>0)|| it.completedCells in 1..Constants.Cells-1}
            "Completed" -> listProgress.filter {it.completedCells == Constants.Cells}
            else -> emptyList()
        }
    }

    private fun updateAdapterByCollection(allPaterns: List<PatternData>, allProgress: List<GameProgress>){
        var listProgress = getProgressByCollection(collectionType!!, allProgress)

        var patternId = listProgress.map { it.patternId }
        val listPattern = allPaterns.filter { it.id in patternId }

        adapter?.apply {
            this.listPattern = listPattern
            this.listProgress = listProgress
            this.listState = MutableList(listPattern.size){true}
            notifyDataSetChanged()
        }
    }

    private fun updateAdapter(allPaterns: List<PatternData>, allProgress: List<GameProgress>){
        var listPattern = getPatternByCategory(category!!, allPaterns)

        var patternIds = listPattern.map { it.id }
        val listProgress = allProgress.filter { it.patternId in patternIds }

        adapter?.apply {
            this.listPattern = listPattern
            this.listProgress = listProgress
            this.listState = MutableList(listPattern.size){true}
            notifyDataSetChanged()
        }
    }

    companion object {
        fun newInstance(category: String) = PatternMenu().apply {
            arguments = Bundle().apply {
                putString("category", category)
            }
        }

        fun newInstanceForCollection(collectionType:String) = PatternMenu().apply {
            arguments = Bundle().apply {
                putString("collectionType", collectionType)
            }
        }
    }


}