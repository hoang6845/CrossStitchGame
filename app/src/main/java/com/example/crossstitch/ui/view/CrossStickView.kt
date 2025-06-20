package com.example.crossstitch.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.crossstitch.R
import com.example.crossstitch.di.Constants
import com.example.crossstitch.di.ScreenSize
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.ui.screen.gameBinding
import com.example.crossstitch.viewmodel.PatternViewModel

class CrossStitchView @JvmOverloads constructor(
    context: Context,
    private val patternData: PatternData,
    var gameProgress: GameProgress,
    val viewModel: PatternViewModel,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    val Float.dp: Float
        get() = (this * resources.displayMetrics.density)
    private var drawMode = false

    private val numRows = Constants.NUMROWS
    private val numCols = Constants.NUMCOLS

    private var numDrawCols = Constants.NUMDRAWCOLS
    private val numDrawRows = Constants.NUMDRAWROWS

    private var cellSize = 0f

    private var startCol: Int? = 0
    private var startRow: Int? = 0

    private var drawCellSize: Float? = 0f

    private var touchDownX = 0f
    private var touchDownY = 0f
    private val touchSlop = 10f

    private var lastFingerPositions: Pair<PointF, PointF>? = null

    private var grid: Array<IntArray>
    private var myCrossStitchGrid: Array<IntArray>
    private var completedCells: Int? = 0
    private var mistake: Int? = 0
    private var numCompletedPallet: MutableList<Int>? = null
    private var numColorNeedCompleted: MutableList<Int>? = null
    private var colorIndexMap: Map<Int, Int>? = null


    fun getMistake(): Int {
        return mistake!!
    }

    private fun calNumCompletedPallet(){

        this.patternData.gridColor.forEach { intArray ->
            intArray.forEach {
                if (it != Int.MIN_VALUE){
                    colorIndexMap!![it]?.let { index ->
                        numColorNeedCompleted!!.set(index, numColorNeedCompleted!![index] +1)
                    }
                }
            }
        }


        this.gameProgress.myCrossStitchGrid.forEach { intArray ->
            intArray.forEach {
                if (it != Int.MIN_VALUE) {
                    colorIndexMap!![it]?.let { index ->
                        if ( numCompletedPallet!![index]<numColorNeedCompleted!![index]) {
                            numCompletedPallet!![index] = numCompletedPallet!![index] +1
                        }
                    }
                }
            }
        }


    }

    private fun updateNumCompletedPallet(row: Int, col: Int, newColor: Int) {
        if (isEraserMode){
            val index = colorIndexMap?.get(selectedColor)
            if (patternData.gridColor[row][col] == myCrossStitchGrid[row][col]){
                numCompletedPallet?.set(index!!, numCompletedPallet!![index!!]-1)

            }
            setSelectedColor(selectedColor!!)
        }else {
            val index = colorIndexMap?.get(patternData.gridColor[row][col])
            if (patternData.gridColor[row][col] == myCrossStitchGrid[row][col] && patternData.gridColor[row][col] != newColor) {
                if (index != null) {
                    numCompletedPallet?.set(index, numCompletedPallet!![index]-1)
                }
            }else if (myCrossStitchGrid[row][col]== Int.MIN_VALUE){
                if (patternData.gridColor[row][col] == newColor) {
                    numCompletedPallet?.set(index!!, numCompletedPallet!![index!!]+1)
                }
            } else if (patternData.gridColor[row][col] != myCrossStitchGrid[row][col]) {
                if (patternData.gridColor[row][col] == newColor) {
                    if (index != null) {
                        numCompletedPallet?.set(index, numCompletedPallet!![index]+1)
                    }
                }
            }
            setSelectedColor(newColor)
        }
    }

    private var MapSymbols = HashMap<Int, String>()

    fun setMapSymbols(map: HashMap<Int, String>) {
        MapSymbols = map
    }


    var selectedColor: Int? = null
    private var aimColor: Int? = null

    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    private var cacheBitmap: Bitmap? = null
    private var cacheCanvas: Canvas? = null

    init {
        this.grid = patternData.gridColor.map { it.clone() }.toTypedArray()
        this.myCrossStitchGrid = gameProgress.myCrossStitchGrid.map { it.clone() }.toTypedArray()
        this.completedCells = gameProgress.completedCells
        this.mistake = gameProgress.mistake
        viewModel.setProgress(this.completedCells!!)
        viewModel.setMistake(this.mistake!!)

        cellSize = (ScreenSize.getGameBoardWidthDp() / numCols).dp
        drawCellSize = ((cellSize * numRows) / numDrawRows)
        numDrawCols = (ScreenSize.widthDp / drawCellSize!!).dp.toInt()

            gameBinding.MainBoardGame.layoutParams.height = (cellSize * numRows).toInt()
            gameBinding.MainBoardGame.layoutParams.width = (cellSize*numCols).toInt()
            gameBinding.MainBoardGame.requestLayout()
        colorIndexMap = this.patternData.collorPalette.withIndex().associate { it.value to it.index }

        this.numCompletedPallet = MutableList(this.patternData.collorPalette.size) { 0 }
        this.numColorNeedCompleted = MutableList(this.patternData.collorPalette.size) { 0 }
        calNumCompletedPallet()

    }

    fun getProgress(): GameProgress {
        gameProgress.myCrossStitchGrid = this.myCrossStitchGrid
        gameProgress.completedCells = this.completedCells!!
        gameProgress.mistake = this.mistake!!
        return gameProgress
    }

    fun autoFill(){
        this.myCrossStitchGrid = this.patternData.gridColor.map { it.clone() }.toTypedArray()
        this.grid = this.myCrossStitchGrid.map { it.clone() }.toTypedArray()
        cacheBitmap?.recycle()
        cacheBitmap = null
        cacheCanvas = null
        this.completedCells = Constants.Cells
        this.mistake = 0
        viewModel.setProgress(Constants.Cells)
        numCompletedPallet = numColorNeedCompleted?.toMutableList()
        setSelectedColor(selectedColor!!)
        invalidate()
    }

    fun reset(){
        this.myCrossStitchGrid = Array(numRows) { IntArray(numCols) { Int.MIN_VALUE } }
        this.grid = this.patternData.gridColor.map { it.clone() }.toTypedArray()
        cacheBitmap?.recycle()
        cacheBitmap = null
        cacheCanvas = null
        this.completedCells = 0
        this.mistake = 0
        viewModel.setProgress(0)
        viewModel.setMistake(0)
        numCompletedPallet = MutableList(this.patternData.collorPalette.size) { 0 }
        setSelectedColor(selectedColor!!)
        invalidate()
    }



    fun getBitMap(): Bitmap? {
        return cacheBitmap
    }

    fun getCurrentCross(): Array<IntArray> {
        return this.myCrossStitchGrid
    }

    fun getCompletedCells(): Int? {
        return this.completedCells
    }

    fun setSelectedColor(color: Int) {
        selectedColor = color
        var index = colorIndexMap!![color]
        updatetextNumCompletedColor(index!!)
    }

    fun updatetextNumCompletedColor(index:Int){
        gameBinding.numCompletedColor?.text = "${numCompletedPallet?.get(index)}/${numColorNeedCompleted?.get(index)}"
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cacheBitmap?.recycle()
        cacheBitmap = null
        cacheCanvas = null
        invalidate()
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (drawMode) {
            openDrawPage(touchDownX, touchDownY, canvas)
        } else {
            if (cacheBitmap == null || cacheBitmap?.width != width || cacheBitmap?.height != height) {
                cacheBitmap?.recycle()
                cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                cacheCanvas = Canvas(cacheBitmap!!)
                drawMainGrid(cacheCanvas!!)
            }
            canvas.drawBitmap(cacheBitmap!!, 0f, 0f, null)
        }
//        Log.d("check", "onDraw: +${ScreenSize.heightDp.dp} + ${ScreenSize.widthDp.dp} + ${cellSize * 150}")

    }

    private var isEraserMode: Boolean = false

    fun changeEraserMode() {
        isEraserMode = !isEraserMode
    }

    fun isEraserMode(): Boolean {
        return isEraserMode
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (!drawMode) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownX = event.x
                    touchDownY = event.y
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    val dx = Math.abs(event.x - touchDownX)
                    val dy = Math.abs(event.y - touchDownY)
                    if (dx < touchSlop && dy < touchSlop) {
                        drawMode = true
                        invalidate()
                        return true
                    }
                }
            }
            return false
        }
        if (event.pointerCount == 2) {
            Log.d("check 2 ngon", "onTouchEvent: 2 ngon")
            val point1 = PointF(event.getX(0), event.getY(0))
            val point2 = PointF(event.getX(1), event.getY(1))

            Log.d("check 2 ngon", "Action: ${event.actionMasked}") // Thêm log này

            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    Log.d("check 2 ngon", "ACTION_MOVE detected") // Thêm log này
                    lastFingerPositions?.let { (last1, last2) ->
                        Log.d("check 2 ngon", "lastFingerPositions is not null") // Thêm log này
                        val currentX = (point1.x + point2.x) / 2
                        val currentY = (point1.y + point2.y) / 2
                        var lastCenterX = (last1.x + last2.x) / 2
                        var lastCenterY = (last1.y + last2.y) / 2
                        val dx = currentX - lastCenterX
                        val dy = currentY - lastCenterY
                        Log.d("check 2 ngon", "onTouchEvent: ${dx} ${dy}")
                        lastFingerPositions = Pair(point1, point2)
                        touchDownX -= dx / 3
                        touchDownY -= dy / 3
                        invalidate()

                    } ?: Log.d("check 2 ngon", "lastFingerPositions is null") // Thêm log này
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                    Log.d("check 2 ngon", "ACTION_UP/CANCEL/POINTER_UP") // Thêm log này
                    lastFingerPositions = null
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    Log.d("check 2 ngon", "ACTION_POINTER_DOWN") // Thêm log này
                    lastFingerPositions = Pair(point1, point2)
                }
            }
            return true
        }
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val col = (event.x / drawCellSize!!).toInt()
            val row = (event.y / drawCellSize!!).toInt()
            if (startRow!! + row in 0 until numRows && startCol!! + col in 0 until numCols) {
                if (isEraserMode) {
                    grid[startRow!! + row][startCol!! + col] = Int.MIN_VALUE
                    updateProgress(startRow!! + row, startCol!! + col, Int.MIN_VALUE!!)
                    updateNumCompletedPallet(startRow!! + row, startCol!! + col, Int.MIN_VALUE!!)
                    myCrossStitchGrid[startRow!! + row][startCol!! + col] = Int.MIN_VALUE
                    cacheCanvas?.let {
                        updateOnMainGrid(
                            it, startRow!! + row, startCol!! + col,
                            Int.MIN_VALUE
                        )
                    }

                    invalidate()
                } else {
                    grid[startRow!! + row][startCol!! + col] = selectedColor!!
                    aimColor = null
                    updateProgress(startRow!! + row, startCol!! + col, selectedColor!!)
                    updateNumCompletedPallet(startRow!! + row, startCol!! + col, selectedColor!!)
                    myCrossStitchGrid[startRow!! + row][startCol!! + col] = selectedColor!!
                    cacheCanvas?.let {
                        updateOnMainGrid(
                            it, startRow!! + row, startCol!! + col,
                            selectedColor!!
                        )
                    }
                    invalidate()
                }

            }
        }
        return true
    }

    // lay mau selected color de find
    fun AimUnCompletedCeils() {
        drawMode = true
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                if (myCrossStitchGrid[row][col] == this.patternData.gridColor[row][col]) continue
                if (this.patternData.gridColor[row][col] == selectedColor){
                    aimColor = selectedColor
                    touchDownX = col * cellSize
                    touchDownY = row * cellSize
                    invalidate()
                }
            }
        }
    }

    private fun updateProgress(row: Int, col: Int, newColor: Int) {
        if (isEraserMode) {
            if (patternData.gridColor[row][col] == myCrossStitchGrid[row][col]){
                this.completedCells = this.completedCells!! - 1
            }else if (myCrossStitchGrid[row][col] != Int.MIN_VALUE && patternData.gridColor[row][col] != myCrossStitchGrid[row][col]){
                this.mistake = this.mistake!! -1
            }
        }else{
            if (patternData.gridColor[row][col] == myCrossStitchGrid[row][col] && patternData.gridColor[row][col] != newColor) {
                this.completedCells = this.completedCells!! - 1
                this.mistake = this.mistake!! + 1
            }else if (myCrossStitchGrid[row][col]== Int.MIN_VALUE){
                if (patternData.gridColor[row][col] == newColor) {
                    this.completedCells = this.completedCells!! + 1
                } else {
                    this.mistake = this.mistake!! + 1
                }
            } else if (patternData.gridColor[row][col] != myCrossStitchGrid[row][col]) {
                if (patternData.gridColor[row][col] == newColor) {
                    this.completedCells = this.completedCells!! + 1
                    this.mistake = this.mistake!! -1
                }
            }
        }

        viewModel.setProgress(this.completedCells!!)
        viewModel.setMistake(this.mistake!!)
    }


    private fun drawMainGrid(canvas: Canvas) {
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {


                paint.color = grid[row][col]
                drawRect(canvas, cellSize, row, col, row, col)

//                Log.d("DEBUG", "drawMainGridqweq: ${paint.color}")
                paint.strokeWidth = 1f
                paint.color = Color.GRAY
                paint.style = Paint.Style.STROKE
//                drawRect(canvas, cellSize, row, col, row, col) // ve vien
                canvas.drawRect(
                    col * cellSize, row * cellSize,
                    (col + 1) * cellSize, (row + 1) * cellSize,
                    paint
                )
                paint.style = Paint.Style.FILL

            }

        }
    }

    private fun updateOnMainGrid(canvas: Canvas, rowUpdate: Int, colUpdate: Int, color1: Int) {
        paint.color = color1
//        Log.d("DEBUG", "updateOnMainGrid: ${paint.color}")
        val cellLeft = colUpdate * cellSize
        val cellTop = rowUpdate * cellSize

        var clearPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            cellLeft, cellTop,
            cellLeft + cellSize, cellTop + cellSize,
            clearPaint
        )
        drawRect(canvas, cellSize, rowUpdate, colUpdate, rowUpdate, colUpdate, true)
    }

    //15 o ngang
    //150
    private fun openDrawPage(positionX: Float, positionY: Float, canvas: Canvas) {
        val col = (positionX / cellSize).toInt()
        val row = (positionY / cellSize).toInt() //o hien tai

        startCol = when (col) {
            in 0..numDrawCols / 2 -> 0
            in numDrawCols / 2 + 1..numCols - numDrawCols / 2 -> col - numDrawCols / 2 - 1
            else -> numCols - numDrawCols
        }
        startRow = when (row) {
            in 0..numDrawCols / 2 -> 0
            in numDrawRows / 2 + 1..numRows - numDrawRows / 2 -> row - numDrawRows / 2 - 1
            else -> numRows - numDrawRows
        }
        for (row in 0 until numDrawRows) {
            for (col in 0 until numDrawCols) {

                paint.color = grid[startRow!! + row][startCol!! + col]
                drawRect(canvas, drawCellSize!!, row, col, startRow!! + row, startCol!! + col)
                if (aimColor!= null&& aimColor==patternData.gridColor[startRow!! + row][startCol!!+col] && myCrossStitchGrid[startRow!! + row][startCol!!+col] != aimColor){
                    paint.color = aimColor!!
                    paint.strokeWidth = 6f
                }else {
                    paint.strokeWidth = 1f
                    paint.color = Color.GRAY
                }
                paint.style = Paint.Style.STROKE
//                drawRect(canvas, , row, col,, ) //ve vien
                canvas.drawRect(
                    col * drawCellSize!!, row * drawCellSize!!,
                    (col + 1) * drawCellSize!!, (row + 1) * drawCellSize!!,
                    paint
                )
                paint.strokeWidth = 1f
                paint.style = Paint.Style.FILL

            }

        }

    }

    private fun getInverseColor(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.rgb(255 - r, 255 - g, 255 - b)
    }

    private fun drawRect(
        canvas: Canvas,
        cellSize: Float,
        row: Int,
        col: Int,
        checkRow: Int,
        checkCol: Int,
        update: Boolean = false
    ) {
        if (myCrossStitchGrid[checkRow][checkCol] != Int.MIN_VALUE) {
//            Log.d("DEBUG", "updateOnMainGrid1: ${myCrossStitchGrid[checkRow][checkCol]}")
            paint.color = myCrossStitchGrid[checkRow][checkCol]
            drawCrossStitch(canvas, row, col, cellSize, paint)
            if (this.patternData.gridColor[checkRow][checkCol] != myCrossStitchGrid[checkRow][checkCol]) {
                val cellLeft = col * cellSize
                val cellTop = row * cellSize
                val paintTemp = Paint().apply {
                    textAlign = Paint.Align.CENTER
                    textSize = cellSize * 0.6f
                    isAntiAlias = true
                }
                val x = cellLeft + cellSize / 2f
                val y = cellTop + cellSize / 2f - ((paintTemp.descent() + paintTemp.ascent()) / 2)
                canvas.drawText(resources.getString(R.string.warning), x, y, paintTemp)
            }
        } else {
            val cellLeft = col * cellSize
            val cellTop = row * cellSize
            MapSymbols[this.patternData.gridColor[checkRow][checkCol]]?.let { symbol ->
                val paintTemp = Paint().apply {
                    textAlign = Paint.Align.CENTER
                    textSize = cellSize * 0.8f
                    isAntiAlias = true
                }
                val x = cellLeft + cellSize / 2f
                val y = cellTop + cellSize / 2f - ((paintTemp.descent() + paintTemp.ascent()) / 2)
                canvas.drawText(symbol, x, y, paintTemp)
            }
        }

        if (update) {
            paint.strokeWidth = 1f
            paint.color = Color.GRAY
            paint.style = Paint.Style.STROKE
//            drawRect(canvas, cellSize, row, col, row, col)// ve vien
            canvas.drawRect(
                col * cellSize, row * cellSize,
                (col + 1) * cellSize, (row + 1) * cellSize,
                paint
            )
            paint.style = Paint.Style.FILL
        }
    }

    fun drawCrossStitch(canvas: Canvas, row: Int, col: Int, drawCellSize: Float, paint: Paint) {
        // Tọa độ trung tâm của mũi thêu
        val x = (col + 0.5f) * drawCellSize
        val y = (row + 0.5f) * drawCellSize

        // Kích thước oval
        val ovalLength = drawCellSize * 1.2f
        val ovalWidth = drawCellSize * 0.5f  // Độ dày của chỉ

        // Tạo màu đậm hơn cho bóng đổ
        val shadowPaint = Paint(paint)
        shadowPaint.color = darkenColor(paint.color, 0.4f)

        // Tạo màu sáng hơn cho điểm nhấn
        val highlightPaint = Paint(paint)
        highlightPaint.color = lightenColor(paint.color, 0.3f)

        // Vẽ đường chéo thứ nhất \ (từ trên trái xuống dưới phải)
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(45f) // 45 độ

        // Vẽ bóng đổ
        canvas.save()
        canvas.translate(1f, 1f)
        canvas.drawOval(
            1 - ovalLength / 2, 1 - ovalWidth / 2,
            1 + ovalLength / 2, 1 + ovalWidth / 2,
            shadowPaint
        )
        canvas.restore()

        // Vẽ chỉ chính
        canvas.drawOval(
            -ovalLength / 2, -ovalWidth / 2,
            ovalLength / 2, ovalWidth / 2,
            paint
        )

        // Vẽ điểm nhấn sáng
        canvas.drawOval(
            -ovalLength / 6 - ovalLength / 3, -ovalWidth / 4 - ovalWidth / 4,
            -ovalLength / 6 + ovalLength / 3, -ovalWidth / 4 + ovalWidth / 4,
            highlightPaint
        )
        canvas.restore()

        // Vẽ đường chéo thứ hai / (từ trên phải xuống dưới trái)
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(-45f) // -45 độ

        // Vẽ bóng đổ
        canvas.save()
        canvas.translate(1f, 1f)
        canvas.drawOval(
            1 - ovalLength / 2, 1 - ovalWidth / 2,
            1 + ovalLength / 2, 1 + ovalWidth / 2,
            shadowPaint
        )
        canvas.restore()

        // Vẽ chỉ chính
        canvas.drawOval(
            -ovalLength / 2, -ovalWidth / 2,
            ovalLength / 2, ovalWidth / 2,
            paint
        )

        // Vẽ điểm nhấn sáng
        canvas.drawOval(
            -ovalLength / 6 - ovalLength / 3, -ovalWidth / 4 - ovalWidth / 4,
            -ovalLength / 6 + ovalLength / 3, -ovalWidth / 4 + ovalWidth / 4,
            highlightPaint
        )
        canvas.restore()
    }

    // Hàm làm tối màu
    fun darkenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * (1f - factor)).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * (1f - factor)).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * (1f - factor)).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }

    // Hàm làm sáng màu
    fun lightenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) + (255 - Color.red(color)) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) + (255 - Color.green(color)) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) + (255 - Color.blue(color)) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }

    fun switchToWatchingMode() {
        drawMode = false
        invalidate()
    }

}
