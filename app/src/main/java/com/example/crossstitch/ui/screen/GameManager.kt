package com.example.crossstitch.ui.screen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.crossstitch.R
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.FragmentGameManagerBinding
import com.example.crossstitch.di.Constants
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.GridColorAdapter
import com.example.crossstitch.ui.adapter.irv.IRv
import com.example.crossstitch.ui.view.CrossStitchView
import com.example.crossstitch.viewmodel.PatternViewModel
import com.example.crossstitch.viewmodel.VerifyViewModel

lateinit var gameBinding: FragmentGameManagerBinding
class GameManager : Fragment() {
    private lateinit var stitchView: CrossStitchView

    private var colorAdapter:GridColorAdapter?= null
    private var selectedColor:Int? = null

    private var handleAimCellNotCompleted :View.OnClickListener? = null
    private var handleGetStateCross:View.OnTouchListener? = null
    private var handleSwitchMode:View.OnClickListener? = null
    private var handleModeEraser:View.OnClickListener? =null
    private var handleMore:View.OnClickListener? =null
    private var handleAutoFill:View.OnClickListener? = null
    private var handleReset:View.OnClickListener? = null
    private var handleVerify: View.OnClickListener? = null
    private var handleWarningMistake: View.OnTouchListener? = null
    private var handleExitDraw: View.OnClickListener? = null

    private var currentPatternId: Int? = null
    private var currentPattern: PatternData? = null
    private var currentProgress: GameProgress? = null

    private lateinit var verifyViewModel: VerifyViewModel
    private lateinit var viewModel : PatternViewModel
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = findNavController()


        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory)[PatternViewModel::class.java]
        verifyViewModel = ViewModelProvider(requireActivity())[VerifyViewModel::class.java]

        currentPatternId = arguments?.getInt("patternId")
        currentPattern =  viewModel.listPatternLiveData.value.find { patternData: PatternData -> patternData.id == currentPatternId }
        currentProgress = viewModel.listGameProgressLiveData.value.find { gameProgress: GameProgress -> gameProgress.patternId == currentPatternId }
        val index = viewModel.listPatternLiveData.value?.indexOfFirst { it.id == currentPatternId }
        if (index != null) {
            verifyViewModel.setPosition(index)
        }
    }

    @SuppressLint("WrongThread")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        gameBinding = FragmentGameManagerBinding.inflate(inflater, container, false)

        stitchView = currentPattern?.let { currentProgress?.let { it1 ->
            CrossStitchView(requireContext(), it, it1, viewModel
            )
        } }!!

        prepareHandle()
        gameBinding.MainBoardGame.addView(stitchView)
        gameBinding.btnFullscreen.setOnClickListener(handleSwitchMode)
        gameBinding.btnCache.setOnTouchListener(handleGetStateCross)
        gameBinding.btnEraser.setOnClickListener(handleModeEraser)
        gameBinding.btnMore.setOnClickListener(handleMore)
        gameBinding.btnAim.setOnClickListener(handleAimCellNotCompleted)
        gameBinding.btnReset.setOnClickListener(handleReset)
        gameBinding.btnFill.setOnClickListener(handleAutoFill)
        gameBinding.btnVerified.setOnClickListener(handleVerify)
        gameBinding.btnExitDraw.setOnClickListener(handleExitDraw)
        gameBinding.btnWarningMistake.setOnTouchListener(handleWarningMistake)

        currentPattern?.collorPalette?.let { prepareColor(it) }
        gameBinding.rvGridColor.adapter = colorAdapter
        gameBinding.rvGridColor.layoutManager = GridLayoutManager(
            requireContext(),
            6,
            GridLayoutManager.VERTICAL,
            false
        )

        viewModel.currentProgress.observe(viewLifecycleOwner, { value ->
            gameBinding.numCompleted?.text = "Progress: ${value}/${Constants.Cells}"
            if (value == Constants.Cells){
                showResult()
            }
        })

        viewModel.currentMistake.observe(viewLifecycleOwner, {value ->
//            gameBinding.mistake?.setText("Mistake: "+value.toString())
        })

//        gameBinding.gridlayout.getChildAt(0).performClick()


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
                    val selectedDrawable = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 16f
                        setColor(Color.WHITE)
                        setStroke(6, Color.BLACK) // Độ dày và màu viền
                    }
                    if (gameBinding.btnCache.background == null) {
                        gameBinding.btnCache.background = selectedDrawable
                    } else {
                        gameBinding.btnCache.background = null
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    gameBinding.boxstate.visibility = View.GONE
                    val selectedDrawable = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 16f
                        setColor(Color.WHITE)
                        setStroke(6, Color.BLACK) // Độ dày và màu viền
                    }
                    if (gameBinding.btnCache.background == null) {
                        gameBinding.btnCache.background = selectedDrawable
                    } else {
                        gameBinding.btnCache.background = null
                    }
                    true
                }
                else -> false
            }
        }

        handleExitDraw = View.OnClickListener {
            showSaveConfirmationDialog()
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

        handleMore = View.OnClickListener{
            if (gameBinding.moreBoard.visibility == View.VISIBLE){
                gameBinding.moreBoard.visibility = View.GONE
            }else{
                gameBinding.moreBoard.visibility = View.VISIBLE
            }
        }

        handleAimCellNotCompleted = View.OnClickListener {
            stitchView.AimUnCompletedCeils()
        }

        handleReset = View.OnClickListener {
            showResetDialog(requireContext()){
                stitchView.reset()
            }
        }

        handleAutoFill = View.OnClickListener {
            stitchView.autoFill()
        }

        handleVerify = View.OnClickListener {
            viewModel.updateProgress(stitchView.getProgress())
            if (stitchView.getCompletedCells()!! < Constants.Cells){
                verifyViewModel.setBitmap(stitchView.getBitMap()!!)
            }else{
                val converterP = ConverterPixel()
                verifyViewModel.setBitmap(converterP.colorMatrixToBitmap(stitchView.getCurrentCross()))
            }

            verifyViewModel.setCategory(currentPattern?.Category)
            var bundle = Bundle()
            currentPatternId?.let { it1 -> bundle.putInt("patternId", it1) }
            navController?.navigate(R.id.verify, bundle)
        }

        handleWarningMistake = View.OnTouchListener{view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    gameBinding.numMistake.text = stitchView.getMistake().toString()
                    gameBinding.warning.visibility = View.VISIBLE
                    val selectedDrawable = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 16f
                        setColor(Color.WHITE)
                        setStroke(6, Color.BLACK) // Độ dày và màu viền
                    }
                    if (gameBinding.btnWarningMistake.background == null) {
                        gameBinding.btnWarningMistake.background = selectedDrawable
                    } else {
                        gameBinding.btnWarningMistake.background = null
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    gameBinding.warning.visibility = View.GONE
                    val selectedDrawable = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 16f
                        setColor(Color.WHITE)
                        setStroke(6, Color.BLACK) // Độ dày và màu viền
                    }
                    if (gameBinding.btnWarningMistake.background == null) {
                        gameBinding.btnWarningMistake.background = selectedDrawable
                    } else {
                        gameBinding.btnWarningMistake.background = null
                    }
                    true
                }
                else -> {
                    false
                }
            }

        }
    }

    private fun showResult(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_show_result, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialogView.findViewById<AppCompatButton>(R.id.btn_show_result).setOnClickListener{

            viewModel.updateProgress(stitchView.getProgress())
            if (stitchView.getCompletedCells()!! < Constants.Cells){
                verifyViewModel.setBitmap(stitchView.getBitMap()!!)
            }else{
                val converterP = ConverterPixel()
                verifyViewModel.setBitmap(converterP.colorMatrixToBitmap(stitchView.getCurrentCross()))
            }

            verifyViewModel.setCategory(currentPattern?.Category)
            var bundle = Bundle()
            currentPatternId?.let { it1 -> bundle.putInt("patternId", it1) }
            bundle.putBoolean("isCompleted", true)
            navController?.navigate(R.id.verify, bundle)

            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showResetDialog(
        context: android.content.Context,
        onConfirmed: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reset_pattern, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialogView.findViewById<AppCompatButton>(R.id.btn_keep_it).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<AppCompatButton>(R.id.btn_accept_clear).setOnClickListener {
            onConfirmed()
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

    }

    private fun showSaveConfirmationDialog(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_exit_drawing, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialogView.findViewById<AppCompatButton>(R.id.btn_save_dialog).setOnClickListener{
            viewModel.updateProgress(stitchView.getProgress())
            dialog.dismiss()
            navController!!.navigate(R.id.menuPatternContainer)
        }
        dialogView.findViewById<AppCompatButton>(R.id.btn_cancel_dialog).setOnClickListener{
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun prepareColor(colorPalette: List<Int>){
        var listSymbol = resources.getStringArray(R.array.symbol_list)
        colorAdapter = GridColorAdapter(colorPalette, listSymbol, object :IRv{
            override fun onClickItem(position: Int) {
                val color = colorPalette[position]
                gameBinding.colorSelected.setBackgroundColor(color)
                selectedColor = color
                this@GameManager.colorAdapter?.setSelectedColor(color, position)
                stitchView.setSelectedColor(color)
                if (stitchView.isEraserMode()){
                    gameBinding.btnEraser.performClick()
                }
                this@GameManager.colorAdapter?.notifyItemChanged(position)
            }

        })
        if (colorPalette.isNotEmpty()) {
            val firstColor = colorPalette[0]
            gameBinding.colorSelected.setBackgroundColor(firstColor)
            selectedColor = firstColor
            stitchView.setSelectedColor(firstColor)
            this@GameManager.colorAdapter?.setSelectedColor(firstColor, 0)
            // Bỏ chế độ xóa nếu đang bật
            if (stitchView.isEraserMode()) {
                gameBinding.btnEraser.performClick()
            }
        }
        val map = HashMap<Int, String>()
        for ((count, color) in colorPalette.withIndex()){
            map[color] = listSymbol.get(count)
        }
        stitchView.setMapSymbols(map)

//        for (i in gameBinding.gridlayout.childCount - 1 downTo 0) {
//            val child = gameBinding.gridlayout.getChildAt(i)
//            if (child is CardView) {
//                gameBinding.gridlayout.removeViewAt(i)
//            }
//        }
//        val listSymbols = resources.getStringArray(R.array.symbol_list)
//        val cardSize = (ScreenSize.getSettingWidthDp()/8-12).dp
//        for ((count, color) in colorPalette.withIndex()){
//            val cardView = CardView(requireContext()).apply {
//                radius = 16f
//                cardElevation = 8f
//                setCardBackgroundColor(color)
//                val normalDrawable = GradientDrawable().apply {
//                    shape = GradientDrawable.RECTANGLE
//                    cornerRadius = 16f
//                    setColor(color)
//                }
//                background = normalDrawable
//                layoutParams = ViewGroup.MarginLayoutParams(cardSize.toInt(), cardSize.toInt()).apply {
//                    setMargins(6f.dp.toInt(), 2f.dp.toInt(), 6f.dp.toInt(), 2f.dp.toInt())
//                }
//
//            }
//            cardView.setOnClickListener(View.OnClickListener {
//                val normalDrawable = GradientDrawable().apply {
//                    shape = GradientDrawable.RECTANGLE
//                    cornerRadius = 16f
//                    selectedColor?.let { it1 -> setColor(it1) }
//                }
//                selectedCardView?.background = normalDrawable
//
//                val selectedDrawable = GradientDrawable().apply {
//                    shape = GradientDrawable.RECTANGLE
//                    cornerRadius = 16f
//                    setColor(color)
//                    setStroke(6, Color.BLACK) // Độ dày và màu viền
//                }
//
//                it.background = selectedDrawable
//                selectedCardView = it as CardView?
//                selectedColor = color
//                gameBinding.colorSelected?.setBackgroundColor(selectedColor!!)
//                stitchView.setSelectedColor(selectedColor!!)
//                if (stitchView.isEraserMode()){
//                    gameBinding.btnEraser.performClick()
//                }
//
//            })
//
//            val textView:TextView = TextView(requireContext()).apply {
//                setText(listSymbols.get(count))
//                textSize = 18f
//                gravity = Gravity.CENTER
//            }
//
//            cardView.addView(textView)
//            gameBinding.gridlayout.addView(cardView)
//
//        }
//        // Tính toán số cột dựa theo chiều rộng màn hình
//        val columnCount = ScreenSize.widthDp.dp / (cardSize+12f.dp)
//        Log.d("check", "prepareColor: ${columnCount}")
//        Log.d("check", "prepareColor: ${cardSize}")
//        gameBinding.gridlayout.columnCount = columnCount.toInt()

    }

    val Float.dp: Float
        get() = (this * resources.displayMetrics.density)


}