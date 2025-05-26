package com.example.crossstitch.converter

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import smile.clustering.KMeans

class ConverterPixel {
     fun generatePatternFromBitmap(bitmap: Bitmap, rows: Int, cols: Int): Array<IntArray> {
        val resized = resizeBitmap(bitmap, rows, cols)
        val grid = bitmapToGrid(resized)
//        return quantizeColors(grid, maxColors, palette)
         return grid
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


    fun quantizeColors(grid: Array<IntArray>, targetColorCount: Int, palette: List<Int>): Array<IntArray> {
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

    fun KMeansColor(grid: Array<IntArray>, maxColors: Int): List<Int> {
        // Bước 1: Trích xuất các màu Int từ grid
        val colorList: List<Int> = grid.flatMap { it.toList() }

        // Bước 2: Lọc các màu duy nhất
        val uniqueColors: List<Int> = colorList.distinct()
        Log.d("demm", "KMeansColor: ${uniqueColors.size}")
        // Bước 3: Chuyển sang dạng [R, G, B] để làm KMeans
        val colorData: Array<DoubleArray> = uniqueColors.map { color ->
            doubleArrayOf(
                Color.red(color).toDouble(),
                Color.green(color).toDouble(),
                Color.blue(color).toDouble()
            )
        }.toTypedArray()

        Log.d("TAG", "KMeansColor: unique=${colorData.size} ${maxColors}")

        // Bước 4: Chạy KMeans
        val clusterCount = minOf(maxColors, colorData.size)  // Đề phòng số màu ít hơn maxColors
        val kmeans = KMeans.fit(colorData, clusterCount)
        Log.d("TAG", "KMeansColor after: ${kmeans.size}")

        // Bước 5: Lấy tâm cụm (centroid) và chuyển về Int màu
        return kmeans.centroids.map { center ->
            val (r, g, b) = center.map { it.toInt().coerceIn(0, 255) }
            Color.rgb(r, g, b)  // Trả về Int theo Android
        }
    }

    fun colorMatrixToBitmap(matrix: Array<IntArray>): Bitmap {
        val height = matrix.size
        val width = matrix[0].size
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                bitmap.setPixel(x, y, matrix[y][x])
            }
        }

        return bitmap
    }
}