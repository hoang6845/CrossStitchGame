package com.example.crossstitch.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel:ViewModel() {
    private val _grid = MutableLiveData<Array<IntArray>>()
    val grid: LiveData<Array<IntArray>> get() = _grid

    private val _palette = MutableLiveData<List<Int>>()
    val palette:LiveData<List<Int>> get() = _palette

    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap:LiveData<Bitmap> get() = _bitmap

    fun setBitmap(bitmap: Bitmap){
        _bitmap.value = bitmap
    }

    fun setPalette(list: List<Int>){
        _palette.value = list
    }

    fun setGrid(grid: Array<IntArray>) {
        val newGrid = grid.map { it.clone() }.toTypedArray()
        _grid.value = newGrid
    }
}