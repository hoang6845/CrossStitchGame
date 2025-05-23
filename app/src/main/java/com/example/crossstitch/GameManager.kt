package com.example.crossstitch

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.FragmentGameManagerBinding
import com.example.crossstitch.model.entity.PatternData
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

    private lateinit var viewModel : PatternViewModel

    @SuppressLint("WrongThread")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        gameBinding = FragmentGameManagerBinding.inflate(inflater, container, false)
        stitchView = CrossStitchView(requireContext())

        prepareHandle()

        gameBinding.MainBoardGame.addView(stitchView)
        gameBinding.btn.setOnClickListener(handleSwitchMode)
        gameBinding.cache.setOnTouchListener(handleGetStateCross)

        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(this, factory).get(PatternViewModel::class.java)

        var ListColor = listOf(
            Color.rgb(255,255,254),
            Color.rgb(214,222,215),
            Color.rgb(146,146,147),
            Color.rgb(88,86,90),
            Color.rgb(136,129,112),
            Color.rgb(205,208,205),
            Color.rgb(175,176,173),
            Color.rgb(169,169,149),
            Color.rgb(222,235,236),
            Color.rgb(15,18,50),
            Color.rgb(53,50,55),
            Color.rgb(0,0,0),
            Color.rgb(62,58,65),
            Color.rgb(61,44,35),
            Color.rgb(249,237,210),
            Color.rgb(253,226,221),
            Color.rgb(248,220,198),
            Color.rgb(96,173,54),
            Color.rgb(28,133,50),

            )
        prepareColor(ListColor)


        gameBinding.save.setOnClickListener{
            val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.panda)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            viewModel.addPattern(PatternData(id = null, collorPalette = ListColor, name = "Panda", gridColor = stitchView.getGrid(), image =  stream.toByteArray()))
        }
        lifecycleScope.launch {
            var myPattern = viewModel.findPatternAsync(4).await()
            myPattern?.gridColor?.let { stitchView.setGrid(it) }
            val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.panda)
            var converterPixel:ConverterPixel = ConverterPixel()
            stitchView.setGrid(converterPixel.generatePatternFromBitmap(bitmap, 150, 120, 30, ListColor))
        }
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
        for (i in gameBinding.gridlayout.childCount - 1 downTo 0) {
            val child = gameBinding.gridlayout.getChildAt(i)
            if (child is CardView) {
                gameBinding.gridlayout.removeViewAt(i)
            }
        }

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
            gameBinding.gridlayout.addView(cardView)


        }
    }


}