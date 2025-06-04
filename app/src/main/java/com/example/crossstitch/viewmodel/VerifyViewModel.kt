package com.example.crossstitch.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VerifyViewModel: ViewModel() {
    private val _bitmap = MutableLiveData<Bitmap?>()
    val bitmap: LiveData<Bitmap?> get() = _bitmap

    fun setBitmap(newBitmap: Bitmap) {
        _bitmap.value = newBitmap
    }

    fun clearBitMap(){
        _bitmap.value = null
    }
//    private var _grid = MutableLiveData<Array<IntArray>>()
//    val grid: LiveData<Array<IntArray>> get() = _grid
//    fun setGrid(grid: Array<IntArray>) {
//        _grid.value = grid.map { it.clone() }.toTypedArray()
//    }

}