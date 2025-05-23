package com.example.crossstitch.converter

import android.graphics.Bitmap
import android.graphics.Color

class ConverterPixel {

     fun generatePatternFromBitmap(bitmap: Bitmap, rows: Int, cols: Int, maxColors: Int, palette: List<Int>): Array<IntArray> {
        val resized = resizeBitmap(bitmap, rows, cols)
        val grid = bitmapToGrid(resized)
        return quantizeColors(grid, maxColors, palette)
    }

    private fun resizeBitmap(bitmap: Bitmap, rows: Int, cols: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, cols, rows, false)
    }

    private fun bitmapToGrid(resized: Bitmap): Array<IntArray> {
        val rows = resized.height
        val cols = resized.width
        val grid = Array(rows) { IntArray(cols) }

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                grid[row][col] = resized.getPixel(col, row)
            }
        }

        return grid
    }


    private fun quantizeColors(grid: Array<IntArray>, targetColorCount: Int, palette: List<Int>): Array<IntArray> {
        val flat = grid.flatMap { it.toList() }

        val colorFrequencies = flat.groupingBy { it }.eachCount()

        if (colorFrequencies.size <= targetColorCount) return grid


        fun findNearest(color: Int): Int {
            return palette.minByOrNull {
                val r = Color.red(it) - Color.red(color)
                val g = Color.green(it) - Color.green(color)
                val b = Color.blue(it) - Color.blue(color)
                r * r + g * g + b * b
            } ?: color
        }

        return grid.map { row ->
            row.map { color -> findNearest(color) }.toIntArray()
        }.toTypedArray()
    }
}