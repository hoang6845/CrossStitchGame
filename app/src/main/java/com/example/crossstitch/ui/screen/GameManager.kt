package com.example.crossstitch.ui.screen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.crossstitch.R
import com.example.crossstitch.databinding.FragmentGameManagerBinding
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.viewmodel.PatternViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

lateinit var gameBinding: FragmentGameManagerBinding
class GameManager : Fragment() {
    private lateinit var stitchView: CrossStitchView
    private var selectedCardView:CardView? = null
    private var selectedColor:Int? = null
    var handleGetStateCross:View.OnTouchListener? = null
    var handleSwitchMode:View.OnClickListener? = null

    private var currentPattern: PatternData? = null
    private var currentProgress: GameProgress? = null

    private lateinit var viewModel : PatternViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)


        currentPattern = arguments?.getInt("position")
            ?.let { viewModel.listPatternLiveData.value?.get(it) }

        currentProgress = arguments?.getInt("position")
            ?.let { viewModel.listGameProgressLiveData.value?.get(it) }

    }

    @SuppressLint("WrongThread")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        gameBinding = FragmentGameManagerBinding.inflate(inflater, container, false)

        stitchView = currentPattern?.let { currentProgress?.let { it1 ->
            CrossStitchView(requireContext(), it,
                it1
            )
        } }!!

        prepareHandle()
        gameBinding.MainBoardGame.addView(stitchView)
        gameBinding.btn.setOnClickListener(handleSwitchMode)
        gameBinding.cache.setOnTouchListener(handleGetStateCross)

        currentPattern?.collorPalette?.let { prepareColor(it) }

        gameBinding.save.setOnClickListener(View.OnClickListener {
            viewModel.updateProgress(stitchView.getProgress())
        })

//        lifecycleScope.launch {
//            val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.yasuo)
//            var converterPixel:ConverterPixel = ConverterPixel()
//            stitchView.setGrid(converterPixel.generatePatternFromBitmap(bitmap, 150, 120, 30, ListColor))
//        }
        return gameBinding.root
    }

    fun prepareHandle(){
        handleGetStateCross = View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    gameBinding.boxstate.visibility = View.VISIBLE
                    gameBinding.currentState.setImageBitmap(stitchView.getBitMap())
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    gameBinding.boxstate.visibility = View.GONE
                    true
                }
                else -> false
            }
        }

        handleSwitchMode = View.OnClickListener {
            stitchView.switchToWatchingMode()
        }
    }

    fun prepareColor(colorPalette: List<Int>){
        var map = HashMap<Int, String>()
        for (i in gameBinding.gridlayout.childCount - 1 downTo 0) {
            val child = gameBinding.gridlayout.getChildAt(i)
            if (child is CardView) {
                gameBinding.gridlayout.removeViewAt(i)
            }
        }
        var count = 0
        var listSymbols = resources.getStringArray(R.array.symbol_list)
        for (color in colorPalette){
            val cardView = CardView(requireContext()).apply {
                radius = 16f
                cardElevation = 8f
                setCardBackgroundColor(color)
                val normalDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 16f
                    setColor(color)
                }
                background = normalDrawable
                layoutParams = ViewGroup.MarginLayoutParams(92, 92).apply {
                    setMargins(8, 8, 8, 8)
                }

            }
            cardView.setOnClickListener(View.OnClickListener {
                val normalDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 16f
                    selectedColor?.let { it1 -> setColor(it1) }
                }
                selectedCardView?.background = normalDrawable

                val selectedDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 16f
                    setColor(color)
                    setStroke(6, Color.BLACK) // Độ dày và màu viền
                }
                it.background = selectedDrawable
                selectedCardView = it as CardView?
                selectedColor = color

                stitchView.setSelectedColor(selectedColor!!)

            })

            val textView:TextView = TextView(requireContext()).apply {
                setText(listSymbols.get(count))
                textSize = 24f
                gravity = Gravity.CENTER
            }
            map.put(color, listSymbols.get(count))
            count++;
            cardView.addView(textView)
            gameBinding.gridlayout.addView(cardView)
        }
        stitchView.setMapSymbols(map)
    }


}