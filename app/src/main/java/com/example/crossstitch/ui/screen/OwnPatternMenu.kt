package com.example.crossstitch.ui.screen

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.crossstitch.R
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.FragmentOwnPatternMenuBinding
import com.example.crossstitch.di.Constants
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.PatternAdapter
import com.example.crossstitch.ui.adapter.irv.IPatternRv
import com.example.crossstitch.utils.TakePhotoUtils
import com.example.crossstitch.viewmodel.ImageViewModel
import com.example.crossstitch.viewmodel.PatternViewModel

private lateinit var ownPatternBinding: FragmentOwnPatternMenuBinding
class OwnPatternMenu : Fragment() {
    private var handleAddImage:View.OnClickListener?=null
    var adapter: PatternAdapter? = null
    var navController: NavController? = null
    private lateinit var viewModel: PatternViewModel
    private lateinit var imageViewModel: ImageViewModel
    private lateinit var takePhotoUtils: TakePhotoUtils
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePhotoUtils = TakePhotoUtils(
            this,
            onImagePicked = { bitmap: Bitmap ->
                imageViewModel?.setBitmap(bitmap)
                val converter = ConverterPixel()
                val grid = converter.generatePatternFromBitmap(
                    bitmap,
                    Constants.NUMROWS,
                    Constants.NUMCOLS)
                val palette = converter.KMeansColor(grid, Constants.MAX_COLORS)
                imageViewModel?.setPalette(palette)
                imageViewModel?.setGrid(converter.quantizeColors(grid, Constants.MAX_COLORS, palette))
                Log.d("demm", "onCreate: ${palette.size}")
                navController?.navigate(R.id.createOwnPattern)
            },
            onError = { errorMessage: String ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding.toolbar.visibility = View.VISIBLE
        navController = findNavController()
        ownPatternBinding = FragmentOwnPatternMenuBinding.inflate(inflater, container, false)
        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)
        imageViewModel = ViewModelProvider(requireActivity()).get(ImageViewModel::class.java)
        adapter = PatternAdapter(object : IPatternRv {
            override fun onClickItem(position: Int) {
                val bundle= Bundle()
                bundle.putInt("patternId", this@OwnPatternMenu.adapter?.listPattern?.get(position)?.id!!)
                bundle.putString("type", "Own")
                navController!!.navigate(R.id.gameManager, bundle)
            }

        }, listOf(), listOf(), mutableListOf())

        ownPatternBinding.rv.adapter = adapter
        ownPatternBinding.rv.layoutManager = GridLayoutManager(
            requireContext(),
            2,
            GridLayoutManager.VERTICAL,
            false
        )

        updateAdapter(viewModel.listPatternLiveData.value?: emptyList(), viewModel.listGameProgressLiveData.value?: emptyList())

        viewModel.listPatternLiveData.observe(viewLifecycleOwner, {
                list -> updateAdapter(list, viewModel.listGameProgressLiveData.value?: emptyList())
        })

        viewModel.listGameProgressLiveData.observe(viewLifecycleOwner, {
                list -> updateAdapter(viewModel.listPatternLiveData.value?: emptyList(), list)
        })

        prepareHandle()

        ownPatternBinding.AddImage.setOnClickListener(handleAddImage)
        return ownPatternBinding.root
    }


    fun prepareHandle(){
        handleAddImage = View.OnClickListener {
            takePhotoUtils.showImagePickerDialog()
        }
    }

    private fun updateAdapter(allPattern: List<PatternData>, allProgress: List<GameProgress>){
        Log.d("my library", "updateAdapter: ${allPattern.size}")
        val allMyPattern = allPattern.filter { patternData: PatternData -> patternData.authorName != null}

        val listPatternId: List<Int> = allMyPattern.map { it.id!! }
        var allMyProgress:List<GameProgress> = allProgress.filter { gameProgress: GameProgress -> gameProgress.patternId in listPatternId }

        adapter?.apply {
            this.listPattern = allMyPattern
            this.listProgress = allMyProgress
            this.listState = MutableList(allMyPattern.size) {true}
            notifyDataSetChanged()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        takePhotoUtils.handlePermissionResult(requestCode, grantResults)

    }




}