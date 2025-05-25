package com.example.crossstitch.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.crossstitch.R
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData

class CrossStitchView @JvmOverloads constructor(
    context: Context,
    private val patternData: PatternData,
    var gameProgress: GameProgress,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var drawMode = false

    private val numRows = resources.getInteger(R.integer.max_rows)
    private val numCols = resources.getInteger(R.integer.max_columns)

    private val numDrawCols = 16
    private val numDrawRows = 20

    private var cellSize = 0f

    private var startCol: Int? =0
    private var startRow: Int? =0

    private var drawCellSize:Float? =0f

    private var touchDownX = 0f
    private var touchDownY = 0f
    private val touchSlop = 10f

    private var grid: Array<IntArray>

    private var myCrossStitchGrid: Array<IntArray>

    private var MapSymbols = HashMap<Int, String>()

    fun setMapSymbols(map:HashMap<Int, String>){
        MapSymbols = map
    }


    var selectedColor: Int? = null

    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    private var cacheBitmap: Bitmap? = null
    private var cacheCanvas: Canvas? = null

    init {
        this.grid = patternData.gridColor
        this.myCrossStitchGrid = gameProgress.myCrossStitchGrid
    }

    fun getProgress(): GameProgress {
        gameProgress.myCrossStitchGrid = this.myCrossStitchGrid
        return gameProgress
    }

    fun getBitMap(): Bitmap? {
        return cacheBitmap
    }

    fun setSelectedColor(color:Int){
        selectedColor = color
    }

    fun getGrid(): Array<IntArray> {
        return grid
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (drawMode){
            openDrawPage(touchDownX, touchDownY, canvas)
        }else{
            if (cacheBitmap == null || cacheBitmap?.width != width || cacheBitmap?.height != height) {
                cacheBitmap?.recycle()
                cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                cacheCanvas = Canvas(cacheBitmap!!)
                drawMainGrid(cacheCanvas!!)
            }
            canvas.drawBitmap(cacheBitmap!!, 0f, 0f, null)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!drawMode) {
            when (event.action){
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
            if (startRow!!+row in 0 until numRows && startCol!!+col in 0 until numCols) {
                grid[startRow!!+row][startCol!!+col] = selectedColor!!
                updateProgress(startRow!!+row, startCol!! + col, selectedColor!!)
                myCrossStitchGrid[startRow!!+row][startCol!!+col] = selectedColor!!
                cacheCanvas?.let { updateOnMainGrid(it, startRow!!+row, startCol!!+col,
                    selectedColor!!
                ) }
                invalidate()
            }
        }
        return true
    }

    private fun updateProgress(row:Int, col:Int, newColor:Int){
        if (patternData.gridColor[row][col]==myCrossStitchGrid[row][col]&&patternData.gridColor[row][col]!=newColor){
            gameProgress.completedCells--
            gameProgress.mistake++
        }else if (patternData.gridColor[row][col]!=myCrossStitchGrid[row][col]){
            if (patternData.gridColor[row][col]==newColor){
                gameProgress.completedCells++
            }else {
                gameProgress.mistake++
            }
        }
    }



    private fun drawMainGrid(canvas: Canvas){
        cellSize = (width / numCols).toFloat()

        for (row in 0 until numRows) {
            for (col in 0 until numCols) {

                if (grid[row][col] != Color.WHITE) {
                    paint.color = grid[row][col]
                    drawRect(canvas, cellSize, row, col, row, col)
                }else{
                    paint.color = Color.WHITE
                    drawRect(canvas, cellSize, row, col, row, col)
                }
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

    private fun updateOnMainGrid(canvas: Canvas, rowUpdate: Int, colUpdate :Int, color: Int){
        paint.color = color
        drawRect(canvas, cellSize, rowUpdate, colUpdate, rowUpdate, colUpdate,true)
    }

    //15 o ngang
    //150
    private fun openDrawPage(positionX:Float, positionY: Float, canvas: Canvas){
        val col = (positionX/cellSize).toInt()
        val row = (positionY/cellSize).toInt() //o hien tai
        drawCellSize = (width/numDrawCols).toFloat() //45
        startCol = when (col){
            in 0..numDrawCols/2 -> 0
            in numDrawCols/2+1..numCols -numDrawCols/2 -> col - numDrawCols/2 -1
            else -> numCols - numDrawCols
        }
        startRow = when (row){
            in 0..numDrawCols/2 ->0
            in numDrawRows/2+1 .. numRows-numDrawRows/2 -> row-numDrawRows/2-1
            else -> numRows-numDrawRows
        }
        for (row in 0 until numDrawRows) {
            for (col in 0 until numDrawCols) {
//                Toast.makeText(this@CrossStitchView, , Toast.LENGTH_SHORT).show()
                if (grid[startRow!! +row][startCol!! +col] != Color.WHITE) {
                    paint.color = grid[startRow!! +row][startCol!! +col]
                    drawRect(canvas, drawCellSize!!, row, col,startRow!! +row, startCol!!+col)
                }else{
                    paint.color = Color.WHITE
                    drawRect(canvas, drawCellSize!!, row, col,startRow!! +row, startCol!!+col)
                }
                paint.strokeWidth = 1f
                paint.color = Color.GRAY
                paint.style = Paint.Style.STROKE
//                drawRect(canvas, , row, col,, ) //ve vien
                canvas.drawRect(
                    col * drawCellSize!!,  row * drawCellSize!!,
                    (col + 1) * drawCellSize!!,  (row + 1) * drawCellSize!!,
                    paint
                )
                paint.style = Paint.Style.FILL

            }

        }

    }

    private fun drawRect(canvas: Canvas, cellSize: Float, row:Int, col:Int, checkRow:Int, checkCol:Int, update:Boolean = false){
        if (myCrossStitchGrid[checkRow][checkCol] != Int.MIN_VALUE){
            canvas.drawRect(
                col * cellSize, row * cellSize,
                (col + 1) * cellSize, (row + 1) * cellSize,
                paint
            )
        }else {
            val cellLeft = col * cellSize
            val cellTop = row * cellSize
            MapSymbols[paint.color]?.let { symbol ->
                var paintTemp = Paint().apply {
                    textAlign = Paint.Align.CENTER
                    textSize = cellSize * 0.8f  //
                    isAntiAlias = true
                }
                val x = cellLeft + cellSize / 2f
                val y = cellTop + cellSize / 2f - ((paintTemp.descent() + paintTemp.ascent()) / 2)
                canvas.drawText(symbol, x, y, paintTemp)
            }
        }

        if (update){
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

    fun switchToWatchingMode(){
        drawMode = false
        invalidate()
    }

}
