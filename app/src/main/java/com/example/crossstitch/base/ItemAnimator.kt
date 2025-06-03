package com.example.crossstitch.base

import android.animation.Animator
import android.view.View

interface ItemAnimator {
    fun animator(view:View):Animator
}