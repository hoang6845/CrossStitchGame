package com.example.crossstitch.ui.screen

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.crossstitch.R
import com.example.crossstitch.converter.Converter
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.FragmentCreateOwnPatternBinding
import com.example.crossstitch.di.Constants
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
    private var currentSeekBarValue:Int?=Constants.MAX_COLORS
    private var paletteSelected: List<Int>? = null
    private var gridColorSelected: Array<IntArray>? = null
    private var navController:NavController?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navController = findNavController()
        mainBinding.toolbar.visibility = View.GONE
        createOwnPatternBinding = FragmentCreateOwnPatternBinding.inflate(inflater, container, false)
        imageViewModel = ViewModelProvider(requireActivity()).get(ImageViewModel::class.java)
        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)
        val converter = ConverterPixel()
        createOwnPatternBinding.img.setImageBitmap(converter.colorMatrixToBitmap(imageViewModel.grid.value))
        paletteSelected = imageViewModel.palette.value
        gridColorSelected = imageViewModel.grid.value
        createOwnPatternBinding.seekBar.max = Constants.MAX_COLORS
        createOwnPatternBinding.seekBar.progress = Constants.MAX_COLORS
        createOwnPatternBinding.numcolors.setText(""+Constants.MAX_COLORS)
        createOwnPatternBinding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                currentSeekBarValue = p1
                createOwnPatternBinding.numcolors.text = ""+p1

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                Log.d("SeekBar", "Start tracking")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                paletteSelected =
                    currentSeekBarValue?.let {
                        converter.KMeansColor(imageViewModel.grid.value,
                            it
                        )
                    }
                gridColorSelected = converter.quantizeColors(imageViewModel.grid.value, currentSeekBarValue!!, paletteSelected!!)
                createOwnPatternBinding.img.setImageBitmap(converter.colorMatrixToBitmap(gridColorSelected!!))
                Log.d("demmau", "onStopTrackingTouch: "+countUniqueColors(gridColorSelected!!))
            }
        })
        preapareHandle()
        createOwnPatternBinding.btnSave.setOnClickListener(handleSave)
        createOwnPatternBinding.btnBack.setOnClickListener(View.OnClickListener {
            navController!!.popBackStack()
        })
        return createOwnPatternBinding.root
    }

    fun preapareHandle(){
        var converter=Converter()
        var converterP = ConverterPixel()
        var idCreated: Long? = null
        handleSave = View.OnClickListener {

            imageViewModel.setBitmap(converterP.colorMatrixToBitmap(imageViewModel.grid.value))
            imageViewModel.setPalette(paletteSelected!!)
            imageViewModel.setGrid(gridColorSelected!!)

            CoroutineScope(Dispatchers.IO).launch {
                idCreated = viewModel.addPattern(PatternData(id = null, name = createOwnPatternBinding.nameImage.text.toString(), collorPalette = paletteSelected!!, gridColor = imageViewModel.grid.value, image = converter.bitmapToByteArray(converterP.colorMatrixToBitmap(imageViewModel.grid.value)), authorName = "Own")).await()
                viewModel.addProgress(GameProgress(
                    id = 0, patternId = idCreated!!.toInt(),
                    myCrossStitchGrid =  Array(Constants.NUMROWS) { IntArray(Constants.NUMCOLS) { Int.MIN_VALUE } },
                    completedCells = 0,
                    mistake = 0
                ))
                withContext(Dispatchers.Main) {
                    val builder = AlertDialog.Builder(requireContext())
                    val message = "Add Cross-Stitch success. Closing in "
                    var runnable:Runnable? = null
                    val handler = Handler(Looper.getMainLooper())
                    val dialog = builder.setMessage("$message 3...")
                        .setCancelable(false)
                        .setPositiveButton("Back"){d,_ ->
                            handler.removeCallbacks(runnable!!)
                            d.dismiss()
                            navController!!.popBackStack()
                        }
                        .create()
                    dialog.show()
                    var count = 3
                    runnable = object : Runnable {
                        override fun run() {
                            if (count > 1) {
                                count--
                                dialog.setMessage("$message $count...")
                                handler.postDelayed(this, 1000)
                            } else {
                                dialog.dismiss()
                                navController!!.popBackStack()
                            }
                        }
                    }
                    handler.postDelayed(runnable, 1000)
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