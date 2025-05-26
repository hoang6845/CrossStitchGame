package com.example.crossstitch.ui.screen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.crossstitch.R
import com.example.crossstitch.converter.Converter
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.FragmentCreateOwnPatternBinding
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.viewmodel.ImageViewModel
import com.example.crossstitch.viewmodel.PatternViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private lateinit var createOwnPatternBinding: FragmentCreateOwnPatternBinding
class CreateOwnPattern : Fragment() {
    private lateinit var imageViewModel: ImageViewModel
    private lateinit var viewModel: PatternViewModel
    private var handleSave:View.OnClickListener? = null
    private var currentSeekBarValue:Int?=24
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding.toolbar.visibility = View.GONE
        createOwnPatternBinding = FragmentCreateOwnPatternBinding.inflate(inflater, container, false)
        imageViewModel = ViewModelProvider(requireActivity()).get(ImageViewModel::class.java)
        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)
        val converter = ConverterPixel()
        createOwnPatternBinding.img.setImageBitmap(converter.colorMatrixToBitmap(imageViewModel.grid.value))

        createOwnPatternBinding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                currentSeekBarValue = p1
                createOwnPatternBinding.numcolors.text = ""+p1

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                Log.d("SeekBar", "Start tracking")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                var palette =
                    currentSeekBarValue?.let {
                        converter.KMeansColor(imageViewModel.grid.value,
                            it
                        )
                    }

                imageViewModel.setPalette(palette!!)
                imageViewModel.setGrid(converter.quantizeColors(imageViewModel.grid.value, currentSeekBarValue!!, palette))
                createOwnPatternBinding.img.setImageBitmap(converter.colorMatrixToBitmap(imageViewModel.grid.value))
//                Log.d("demmau", "onStopTrackingTouch: "+countUniqueColors(imageViewModel.grid.value))
            }
        })
        preapareHandle()
        createOwnPatternBinding.btnSave.setOnClickListener(handleSave)
        createOwnPatternBinding.btnBack.setOnClickListener(View.OnClickListener {
            findNavController().popBackStack()
        })
        return createOwnPatternBinding.root
    }

    fun preapareHandle(){
        var converter=Converter()
        var converterP = ConverterPixel()
        var idCreated: Long? = null
        handleSave = View.OnClickListener {
            imageViewModel.setBitmap(converterP.colorMatrixToBitmap(imageViewModel.grid.value))
            CoroutineScope(Dispatchers.IO).launch {
                idCreated = viewModel.addPattern(PatternData(id = null, name = "TEST", collorPalette = imageViewModel.palette.value, gridColor = imageViewModel.grid.value, image = converter.bitmapToByteArray(imageViewModel.bitmap.value), authorName = "Hoang")).await()
                viewModel.addProgress(GameProgress(
                    id = 0, patternId = idCreated!!.toInt(),
                    myCrossStitchGrid =  Array(resources.getInteger(R.integer.max_rows)) { IntArray(resources.getInteger(R.integer.max_columns)) { Int.MIN_VALUE } },
                    completedCells = 0,
                    mistake = 0
                ))
                withContext(Dispatchers.Main) {
                    findNavController().popBackStack()
                }
            }


        }

    }

    fun countUniqueColors(grid: Array<IntArray>): Int {
        return grid.flatMap { it.toList() }
            .toSet()
            .size
    }


}