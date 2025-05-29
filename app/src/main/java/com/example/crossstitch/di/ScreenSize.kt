package com.example.crossstitch.di

import com.example.crossstitch.R

object ScreenSize {
    var widthDp: Float = 0f
    var heightDp: Float = 0f
    var cellSizeDp:Float = 0f
    fun getGameBoardWidthDp(): Float {
        if (widthDp>=600f){
            return cellSizeDp * Constants.NUMCOLS
        }
        return widthDp
    }
    fun getSettingWidthDp():Float{
        if (widthDp>=600f){
            return widthDp- getGameBoardWidthDp()
        }
        return widthDp
    }

}