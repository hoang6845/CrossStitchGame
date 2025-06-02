package com.example.crossstitch.ui.adapter.irv

import android.graphics.Bitmap

interface IPatternRv {
    fun onClickItem(position:Int)
    fun onDownloadClicked(bitmap: Bitmap)
    fun onResetClicked(position: Int)
    fun onAutoFillClicked(position: Int)
}