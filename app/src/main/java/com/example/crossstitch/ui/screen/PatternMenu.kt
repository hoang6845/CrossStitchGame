package com.example.crossstitch.ui.screen

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import com.example.crossstitch.R
import com.example.crossstitch.databinding.FragmentPatternMenuBinding
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.PatternAdapter
import com.example.crossstitch.ui.adapter.irv.IPatternRv
import com.example.crossstitch.untils.saveBitmapToGallery
import com.example.crossstitch.viewmodel.PatternViewModel

lateinit var patternViewBinding: FragmentPatternMenuBinding
class PatternMenu : Fragment() {
    var adapter:PatternAdapter? = null
    var navController: NavController? = null
    private var bitmapToSave: Bitmap? = null
    private lateinit var viewModel: PatternViewModel
    private var category:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString("category")
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
                this@PatternMenu.adapter?.listProgress?.get(position)?.copy(myCrossStitchGrid = Array(resources.getInteger(R.integer.max_rows)){ IntArray(resources.getInteger(R.integer.max_columns)){Int.MIN_VALUE}})
                    ?.let { viewModel.updateProgress(it) }
            }

            override fun onAutoFillClicked(position: Int) {
                var resultGrid = this@PatternMenu.adapter?.listPattern?.get(position)?.gridColor
                if (resultGrid != null) {
                    this@PatternMenu.adapter?.listProgress?.get(position)?.copy(myCrossStitchGrid = resultGrid)
                        ?.let { viewModel.updateProgress(it) }
                }
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

        viewModel.listPatternLiveData.observe(viewLifecycleOwner, {
            list ->updateAdapter(list, viewModel.listGameProgressLiveData.value?: emptyList())

        })

        viewModel.listGameProgressLiveData.observe(viewLifecycleOwner, {
            list -> updateAdapter(viewModel.listPatternLiveData.value?: emptyList(), list)
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

    private fun getPatternByCategory(category: String, listPattern: List<PatternData>): List<PatternData> {
        return when (category){
            "Cartoon" -> listPattern.filter { it.Category.equals("Cartoon") }
            "Flowers" -> listPattern.filter { it.Category.equals("Flowers") }
            "Animals" -> listPattern.filter { it.Category.equals("Animals") }
            else -> listPattern
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

    private fun showConfirmationDialog(
        context: android.content.Context,
        title: String,
        message: String,
        onConfirmed: () -> Unit
    ) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                onConfirmed()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

}