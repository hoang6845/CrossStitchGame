package com.example.crossstitch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.crossstitch.databinding.FragmentGameManagerBinding

lateinit var gameBinding: FragmentGameManagerBinding
class GameManager : Fragment() {
    private lateinit var stitchView: CrossStitchView

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


}