package com.example.crossstitch.ui.screen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
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
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.FragmentGameManagerBinding
import com.example.crossstitch.di.Constants
import com.example.crossstitch.di.ScreenSize
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.viewmodel.PatternViewModel
import com.example.crossstitch.viewmodel.VerifyViewModel

lateinit var gameBinding: FragmentGameManagerBinding
class GameManager : Fragment() {
    private lateinit var stitchView: CrossStitchView
    private var selectedCardView:CardView? = null
    private var selectedColor:Int? = null

    private var handleAimCellNotCompleted :View.OnClickListener? = null
    private var handleGetStateCross:View.OnTouchListener? = null
    private var handleSwitchMode:View.OnClickListener? = null
    private var handleModeEraser:View.OnClickListener? =null
    private var handleBackHome:View.OnClickListener? =null
    private var handleVerified:View.OnClickListener? = null
    private var handleAutoFill:View.OnClickListener? = null
    private var handleReset:View.OnClickListener? = null
    private var handleVerify: View.OnClickListener? = null


    private var currentPattern: PatternData? = null
    private var currentProgress: GameProgress? = null

    private lateinit var verifyViewModel: VerifyViewModel
    private lateinit var viewModel : PatternViewModel
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = findNavController()


        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)
        verifyViewModel = ViewModelProvider(requireActivity()).get(VerifyViewModel::class.java)

        var patternId = arguments?.getInt("patternId")
        currentPattern =  viewModel.listPatternLiveData.value.find { patternData: PatternData -> patternData.id == patternId }
        currentProgress = viewModel.listGameProgressLiveData.value.find { gameProgress: GameProgress -> gameProgress.patternId == patternId }
        val index = viewModel.listPatternLiveData.value?.indexOfFirst { it.id == patternId }
        if (index != null) {
            verifyViewModel.setPosition(index)
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
            CrossStitchView(requireContext(), it, it1, viewModel
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
        gameBinding.btnAim.background = null
        gameBinding.btnAim.setOnClickListener(handleAimCellNotCompleted)
        gameBinding.btnReset?.background = null
        gameBinding.btnReset?.setOnClickListener(handleReset)
        gameBinding.btnFill?.background = null
        gameBinding.btnFill?.setOnClickListener(handleAutoFill)
        gameBinding.btnVerified?.background = null
        gameBinding.btnVerified?.setOnClickListener(handleVerify)

        currentPattern?.collorPalette?.let { prepareColor(it) }

        viewModel.currentProgress.observe(viewLifecycleOwner, { value ->
            gameBinding.progressBar?.progress = value
            gameBinding.completedCells?.text = "Completed: ${value/180}%"
            gameBinding.numCompleted?.text = "Progress: ${value}/18000"
        })

        viewModel.currentMistake.observe(viewLifecycleOwner, {value ->
            gameBinding.mistake?.setText("Mistake: "+value.toString())
        })

        gameBinding.gridlayout.getChildAt(0).performClick()


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

        handleAimCellNotCompleted = View.OnClickListener {
            stitchView.AimUnCompletedCeils()
        }

        handleVerified = View.OnClickListener {

        }

        handleReset = View.OnClickListener {
            showConfirmationDialog(requireContext(), "Reset progress?", "Do you want to reset your progress?"){
                stitchView.reset()
            }
        }

        handleAutoFill = View.OnClickListener {
            showConfirmationDialog(requireContext(), "Auto fill?", "Do you want to auto fill your progress?"){
                stitchView.autoFill()
            }
        }

        handleVerify = View.OnClickListener {
            viewModel.updateProgress(stitchView.getProgress())
            if (stitchView.getCompletedCells()!! < Constants.Cells){
                verifyViewModel.setBitmap(stitchView.getBitMap()!!)
            }else{
                var converterP = ConverterPixel()
                verifyViewModel.setBitmap(converterP.colorMatrixToBitmap(stitchView.getCurrentCross()))
            }

            verifyViewModel.setCategory(currentPattern?.Category)
            navController?.navigate(R.id.verify)
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

    private fun showSaveConfirmationDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle("Save progress?")
            .setMessage("Do you want to save your progress before exiting?")
            .setPositiveButton("Save"){ _,_ ->
                viewModel.updateProgress(stitchView.getProgress())
                navController!!.navigate(R.id.menuPatternContainer)
            }.setNegativeButton("Don't save"){_,_ ->
                navController!!.navigate(R.id.menuPatternContainer)
            }.setNeutralButton("Cancle"){dialog,_ ->
                dialog.dismiss()
            }.show()
    }

    private fun prepareColor(colorPalette: List<Int>){
        var map = HashMap<Int, String>()
        for (i in gameBinding.gridlayout.childCount - 1 downTo 0) {
            val child = gameBinding.gridlayout.getChildAt(i)
            if (child is CardView) {
                gameBinding.gridlayout.removeViewAt(i)
            }
        }
        var count = 0
        var listSymbols = resources.getStringArray(R.array.symbol_list)
        var cardSize = (ScreenSize.getSettingWidthDp()/8-12).dp
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
                layoutParams = ViewGroup.MarginLayoutParams(cardSize.toInt(), cardSize.toInt()).apply {
                    setMargins(6f.dp.toInt(), 2f.dp.toInt(), 6f.dp.toInt(), 2f.dp.toInt())
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
                gameBinding.colorSelected?.setBackgroundColor(selectedColor!!)
                stitchView.setSelectedColor(selectedColor!!)
                if (stitchView.isEraserMode()){
                    gameBinding.btnEraser.performClick()
                }

            })

            val textView:TextView = TextView(requireContext()).apply {
                setText(listSymbols.get(count))
                textSize = 18f
                gravity = Gravity.CENTER
            }
            map.put(color, listSymbols.get(count))
            count++;
            cardView.addView(textView)
            gameBinding.gridlayout.addView(cardView)

        }
        // Tính toán số cột dựa theo chiều rộng màn hình
        val columnCount = ScreenSize.widthDp.dp / (cardSize+12f.dp)
        Log.d("check", "prepareColor: ${columnCount}")
        Log.d("check", "prepareColor: ${cardSize}")
        gameBinding.gridlayout.columnCount = columnCount.toInt()
        stitchView.setMapSymbols(map)
    }

    val Float.dp: Float
        get() = (this * resources.displayMetrics.density)


}