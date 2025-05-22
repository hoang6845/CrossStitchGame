package com.example.crossstitch

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.example.crossstitch.databinding.FragmentGameManagerBinding

lateinit var gameBinding: FragmentGameManagerBinding
class GameManager : Fragment() {
    private lateinit var stitchView: CrossStitchView
    private var numColumn = 6
    private var numRow =5
    var handleGetStateCross:View.OnTouchListener? = null
    var handleSwitchMode:View.OnClickListener? = null
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

        prepareColor(listOf(
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

        ))
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
                layoutParams = ViewGroup.MarginLayoutParams(92, 92).apply {
                    setMargins(8, 8, 8, 8)
                }
            }
            gameBinding.gridlayout.addView(cardView)
        }
    }


}