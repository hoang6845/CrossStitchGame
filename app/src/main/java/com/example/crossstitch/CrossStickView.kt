package com.example.crossstitch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.ActionMode
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

//width = 720
//150*6=900
class CrossStitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var drawMode = false

    private val numRows = 150
    private val numCols = 120

    private val numDrawCols = 16
    private val numDrawRows = 20

    private var cellSize = 0f

    private var startCol: Int? =0
    private var startRow: Int? =0

    private var drawCellSize:Float? =0f

    private var touchDownX = 0f
    private var touchDownY = 0f
    private val touchSlop = 10f

    private var grid = Array(numRows) { IntArray(numCols) { Color.WHITE } }

    var selectedColor: Int? = null

    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    private var cacheBitmap: Bitmap? = null
    private var cacheCanvas: Canvas? = null

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

    fun setGrid(grid:Array<IntArray>){
        this.grid = grid
        cacheCanvas?.let { drawMainGrid(it) }
        invalidate()
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
                        Toast.makeText(context, "Click vào view", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@CrossStitchView.context, "row: ${startRow!!+row}, col: ${startCol!!+col}", Toast.LENGTH_SHORT).show()
                grid[startRow!!+row][startCol!!+col] = selectedColor!!
                cacheCanvas?.let { updateOnMainGrid(it, startRow!!+row, startCol!!+col,
                    selectedColor!!
                ) }
                invalidate()
            }
        }
        return true
    }



    private fun drawMainGrid(canvas: Canvas){
        cellSize = (width / numCols).toFloat()

        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                paint.color = Color.WHITE
                canvas.drawRect(
                    col * cellSize, row * cellSize,
                    (col + 1) * cellSize, (row + 1) * cellSize,
                    paint
                )
                if (grid[row][col] != Color.WHITE) {
                    paint.color = grid[row][col]
                    canvas.drawRect(
                        col * cellSize, row * cellSize,
                        (col + 1) * cellSize, (row + 1) * cellSize,
                        paint
                    )
                }
                paint.strokeWidth = 1f
                paint.color = Color.GRAY
                paint.style = Paint.Style.STROKE
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
        canvas.drawRect(
            colUpdate * cellSize, rowUpdate * cellSize,
            (colUpdate + 1) * cellSize, (rowUpdate + 1) * cellSize,
            paint
        )
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
                paint.color = Color.WHITE
                canvas.drawRect(
                    col * drawCellSize!!, row * drawCellSize!!,
                    (col + 1) * drawCellSize!!, (row + 1) * drawCellSize!!,
                    paint
                )
                if (grid[startRow!! +row][startCol!! +col] != Color.WHITE) {
                    paint.color = grid[startRow!! +row][startCol!! +col]
                    canvas.drawRect(
                        col * drawCellSize!!, row * drawCellSize!!,
                        (col + 1) * drawCellSize!!, (row + 1) * drawCellSize!!,
                        paint
                    )
                }
                paint.strokeWidth = 1f
                paint.color = Color.GRAY
                paint.style = Paint.Style.STROKE
                canvas.drawRect(
                    col * drawCellSize!!, row * drawCellSize!!,
                    (col + 1) * drawCellSize!!, (row + 1) * drawCellSize!!,
                    paint
                )
                paint.style = Paint.Style.FILL

            }

        }

    }

    fun switchToWatchingMode(){
        drawMode = false
        invalidate()
    }

}
