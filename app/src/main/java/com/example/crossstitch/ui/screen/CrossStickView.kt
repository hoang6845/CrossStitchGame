package com.example.crossstitch.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.crossstitch.R
import com.example.crossstitch.di.ScreenSize
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData

class CrossStitchView @JvmOverloads constructor(
    context: Context,
    private val patternData: PatternData,
    var gameProgress: GameProgress,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    val Float.dp: Float
        get() = (this * resources.displayMetrics.density)
    private var drawMode = false

    private val numRows = resources.getInteger(R.integer.max_rows)
    private val numCols = resources.getInteger(R.integer.max_columns)

    private var numDrawCols = 16
    private val numDrawRows = 20

    private var cellSize = 0f

    private var startCol: Int? = 0
    private var startRow: Int? = 0

    private var drawCellSize: Float? = 0f

    private var touchDownX = 0f
    private var touchDownY = 0f
    private val touchSlop = 10f

    private var grid: Array<IntArray>
    private var myCrossStitchGrid: Array<IntArray>
    private var completedCells: Int? = 0
    private var mistake: Int? = 0

    private var MapSymbols = HashMap<Int, String>()

    fun setMapSymbols(map: HashMap<Int, String>) {
        MapSymbols = map
    }


    var selectedColor: Int? = null

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

        cellSize = (ScreenSize.widthDp / numCols).dp
        drawCellSize = ((cellSize*numRows)/ numDrawRows)
        numDrawCols = (ScreenSize.widthDp/drawCellSize!!).dp.toInt()

        gameBinding.MainBoardGame.layoutParams.height = (cellSize*numRows).toInt()
        gameBinding.MainBoardGame.requestLayout()
    }

    fun getProgress(): GameProgress {
        gameProgress.myCrossStitchGrid = this.myCrossStitchGrid
        gameProgress.completedCells = this.completedCells!!
        gameProgress.mistake = this.mistake!!
        return gameProgress
    }

    fun getBitMap(): Bitmap? {
        return cacheBitmap
    }

    fun setSelectedColor(color: Int) {
        selectedColor = color
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
        Log.d("check", "onDraw: +${(ScreenSize.widthDp/numCols).dp} + ${cellSize*150}")
        if (drawCellSize!=null)Log.d("check", "onDraw: +${(ScreenSize.widthDp/numDrawCols).dp} + ${drawCellSize!!*numDrawCols}")
    }

    private var isEraserMode: Boolean = false

    fun changeEraserMode() {
        isEraserMode = !isEraserMode
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
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val col = (event.x / drawCellSize!!).toInt()
            val row = (event.y / drawCellSize!!).toInt()
            if (startRow!! + row in 0 until numRows && startCol!! + col in 0 until numCols) {
                if (isEraserMode) {
                    myCrossStitchGrid[startRow!! + row][startCol!! + col] = Int.MIN_VALUE
                    grid[startRow!! + row][startCol!! + col] = Int.MIN_VALUE
                    cacheCanvas?.let {
                        updateOnMainGrid(
                            it, startRow!! + row, startCol!! + col,
                            Int.MIN_VALUE
                        )
                    }

                    invalidate()
                } else {
                    grid[startRow!! + row][startCol!! + col] = selectedColor!!
                    updateProgress(startRow!! + row, startCol!! + col, selectedColor!!)
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

    private fun updateProgress(row: Int, col: Int, newColor: Int) {
        if (patternData.gridColor[row][col] == myCrossStitchGrid[row][col] && patternData.gridColor[row][col] != newColor) {
            this.completedCells = this.completedCells!! - 1
            this.mistake = this.mistake!! + 1
        } else if (patternData.gridColor[row][col] != myCrossStitchGrid[row][col]) {
            if (patternData.gridColor[row][col] == newColor) {
                this.completedCells = this.completedCells!! + 1
            } else {
                this.mistake = this.mistake!! + 1
            }
        }
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
                paint.strokeWidth = 1f
                paint.color = Color.GRAY
                paint.style = Paint.Style.STROKE
//                drawRect(canvas, , row, col,, ) //ve vien
                canvas.drawRect(
                    col * drawCellSize!!, row * drawCellSize!!,
                    (col + 1) * drawCellSize!!, (row + 1) * drawCellSize!!,
                    paint
                )
                paint.style = Paint.Style.FILL

            }

        }

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
//            canvas.drawRect(
//                col * cellSize, row * cellSize,
//                (col + 1) * cellSize, (row + 1) * cellSize,
//                paint
//            )
            drawCrossStitch(canvas, row, col, cellSize, paint)
            if (this.patternData.gridColor[checkRow][checkCol] != myCrossStitchGrid[checkRow][checkCol]) {
                val cellLeft = col * cellSize
                val cellTop = row * cellSize
                var paintTemp = Paint().apply {
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
                var paintTemp = Paint().apply {
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
        Log.d("assa", "drawCrossStitch: ")
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
