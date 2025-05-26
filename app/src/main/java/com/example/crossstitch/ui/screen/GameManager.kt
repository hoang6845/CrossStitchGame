package com.example.crossstitch.ui.screen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.crossstitch.R
import com.example.crossstitch.databinding.FragmentGameManagerBinding
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.viewmodel.PatternViewModel

lateinit var gameBinding: FragmentGameManagerBinding
class GameManager : Fragment() {
    private lateinit var stitchView: CrossStitchView
    private var selectedCardView:CardView? = null
    private var selectedColor:Int? = null
    private var handleGetStateCross:View.OnTouchListener? = null
    private var handleSwitchMode:View.OnClickListener? = null
    private var handleModeEraser:View.OnClickListener? =null
    private var handleBackHome:View.OnClickListener? =null

    private var currentPattern: PatternData? = null
    private var currentProgress: GameProgress? = null

    private lateinit var viewModel : PatternViewModel
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = findNavController()

        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)



        if (arguments?.getString("type").equals("App")){
            currentPattern = arguments?.getInt("position")
                ?.let { viewModel.listPatternLiveData.value?.get(it) }
            currentProgress = arguments?.getInt("position")
                ?.let { viewModel.listGameProgressLiveData.value?.get(it) }
        }else if(arguments?.getString("type").equals("Own")){
            currentPattern = arguments?.getInt("position")
                ?.let { viewModel.listOwnPatternLiveData.value?.get(it) }
            currentProgress = arguments?.getInt("position")
                ?.let { viewModel.listGameProgressLiveData.value?.get(it+viewModel.listPatternLiveData.value.size) }
        }

    }

    @SuppressLint("WrongThread")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding.toolbar.visibility = View.GONE
        gameBinding = FragmentGameManagerBinding.inflate(inflater, container, false)

        stitchView = currentPattern?.let { currentProgress?.let { it1 ->
            CrossStitchView(requireContext(), it,
                it1
            )
        } }!!

        prepareHandle()
        gameBinding.MainBoardGame.addView(stitchView)
        gameBinding.btnFullscreen.background = null
        gameBinding.btnFullscreen.setOnClickListener(handleSwitchMode)
        gameBinding.btnCache.background = null
        gameBinding.btnCache.setOnTouchListener(handleGetStateCross)
        gameBinding.btnEraser.background = null
        gameBinding.btnEraser.setOnClickListener(handleModeEraser)
        gameBinding.btnHome.background = null
        gameBinding.btnHome.setOnClickListener(handleBackHome)

        currentPattern?.collorPalette?.let { prepareColor(it) }

        gameBinding.btnSave.background = null
        gameBinding.btnSave.setOnClickListener(View.OnClickListener {
            viewModel.updateProgress(stitchView.getProgress())
        })

//        lifecycleScope.launch {
//            val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.yasuo)
//            var converterPixel:ConverterPixel = ConverterPixel()
//            stitchView.setGrid(converterPixel.generatePatternFromBitmap(bitmap, 150, 120, 30, ListColor))
//        }
        return gameBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
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

        handleModeEraser = View.OnClickListener {
            stitchView.changeEraserMode()
            val selectedDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.WHITE)
                setStroke(6, Color.BLACK) // Độ dày và màu viền
            }
            if (gameBinding.btnEraser.background == null) {
                gameBinding.btnEraser.background = selectedDrawable
            } else {
                gameBinding.btnEraser.background = null
            }
        }

        handleBackHome = View.OnClickListener{
            showSaveConfirmationDialog()
        }
    }

    private fun showSaveConfirmationDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle("Save progress?")
            .setMessage("Do you want to save your progress before exiting?")
            .setPositiveButton("Save"){ _,_ ->
                viewModel.updateProgress(stitchView.getProgress())
                navController!!.popBackStack()
            }.setNegativeButton("Don't save"){_,_ ->
                navController!!.popBackStack()
            }.setNeutralButton("Cancle"){dialog,_ ->
                dialog.dismiss()
            }.show()
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

                var selectedDrawable = GradientDrawable().apply {
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