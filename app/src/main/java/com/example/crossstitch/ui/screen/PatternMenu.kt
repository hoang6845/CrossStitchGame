package com.example.crossstitch.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.crossstitch.R
import com.example.crossstitch.databinding.FragmentPatternMenuBinding
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.PatternAdapter
import com.example.crossstitch.ui.adapter.irv.IPatternRv
import com.example.crossstitch.viewmodel.PatternViewModel

lateinit var patternViewBinding: FragmentPatternMenuBinding
class PatternMenu : Fragment() {
    var adapter:PatternAdapter? = null
    private lateinit var viewModel: PatternViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        patternViewBinding = FragmentPatternMenuBinding.inflate(inflater, container, false)

        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)

        adapter = PatternAdapter(object : IPatternRv{
            override fun onClickItem(position: Int) {
                val bundle= Bundle()
                bundle.putInt("position", position)
                val gameFragment = GameManager()
                gameFragment.arguments = bundle
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, gameFragment)
                    .addToBackStack(null)
                    .commit()
            }

        }, listOf(), listOf(), mutableListOf())

        patternViewBinding.rv.adapter = adapter
        patternViewBinding.rv.layoutManager = GridLayoutManager(
            requireContext(),
            2,
            GridLayoutManager.VERTICAL,
            false
        )

        viewModel.listPatternLiveData.observe(viewLifecycleOwner, {
            list -> adapter!!.listPattern = list
            adapter!!.listState = MutableList(list.size){true}
            adapter!!.notifyDataSetChanged()
        })

        viewModel.listGameProgressLiveData.observe(viewLifecycleOwner, {
            list -> adapter!!.listProgress = list
            adapter!!.notifyDataSetChanged()
        })

        prepareSwipedItem()

        return patternViewBinding.root
    }

    fun prepareSwipedItem(){
        var i = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                var position = viewHolder.adapterPosition
                if ( direction == ItemTouchHelper.RIGHT){
                    adapter?.rotate(position)
                    adapter?.notifyItemChanged(position)

                }
            }

        }
        val itemTouchHelper = ItemTouchHelper(i)
        itemTouchHelper.attachToRecyclerView(patternViewBinding.rv)
    }


}