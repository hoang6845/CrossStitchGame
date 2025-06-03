package com.example.crossstitch.ui.screen

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.graphics.Canvas
import android.util.Log
import com.example.crossstitch.R
import com.example.crossstitch.base.FlipAnimation
import com.example.crossstitch.databinding.FragmentPatternMenuBinding
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.PatternAdapter
import com.example.crossstitch.ui.adapter.irv.IPatternRv
import com.example.crossstitch.utils.saveBitmapToGallery
import com.example.crossstitch.viewmodel.PatternViewModel

lateinit var patternViewBinding: FragmentPatternMenuBinding
class PatternMenu : Fragment() {
    var adapter:PatternAdapter? = null
    var navController: NavController? = null
    private var bitmapToSave: Bitmap? = null
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
            override fun onResetClicked(position: Int) {
                this@PatternMenu.adapter?.listProgress?.get(position)?.copy(myCrossStitchGrid = Array(resources.getInteger(R.integer.max_rows)){ IntArray(resources.getInteger(R.integer.max_columns)){Int.MIN_VALUE}}, completedCells = 0)
                    ?.let { viewModel.updateProgress(it) }
                this@PatternMenu.adapter?.notifyItemChanged(position)

            }

            override fun onAutoFillClicked(position: Int) {
                var resultGrid = this@PatternMenu.adapter?.listPattern?.get(position)?.gridColor
                if (resultGrid != null) {
                    this@PatternMenu.adapter?.listProgress?.get(position)?.copy(myCrossStitchGrid = resultGrid, completedCells = 18000)
                        ?.let { viewModel.updateProgress(it) }
                }
                this@PatternMenu.adapter?.notifyItemChanged(position)
            }

            override fun onDownloadClicked(bitmap: Bitmap) {
                bitmapToSave = bitmap
                checkStoragePermissionAndSave()
            }

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
            "In progress" -> listProgress.filter {(it.completedCells in 0..18000-1 && it.mistake>0)|| it.completedCells in 1..18000-1}
            "Completed" -> listProgress.filter {it.completedCells == 18000}
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

    private val REQUEST_CODE_WRITE_STORAGE = 100

    private fun checkStoragePermissionAndSave() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_WRITE_STORAGE
                )
            } else {
                bitmapToSave?.let { saveBitmapToGallery(requireContext(), it) }
            }
        } else {
            // Android 10 trở lên không cần xin quyền WRITE_EXTERNAL_STORAGE
            bitmapToSave?.let { saveBitmapToGallery(requireContext(), it) }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bitmapToSave?.let { saveBitmapToGallery(requireContext(), it) }
            } else {
                Toast.makeText(requireContext(), "Permission denied. Cannot save image.", Toast.LENGTH_SHORT).show()
            }
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