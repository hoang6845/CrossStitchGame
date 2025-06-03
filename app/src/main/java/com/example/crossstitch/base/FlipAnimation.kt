package com.example.crossstitch.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible

class FlipAnimation(
    private val duration: Long = 400L
) {

    private val interpolator = DecelerateInterpolator()
    private var viewFront:View?= null
    private var viewBehind:View?= null
    private var isFrontVisible = true
    fun flip(containerView: View, viewFront: View, viewBehind: View, isFrontVisible: Boolean, onFlipEnd: (() -> Unit)? = null) {
        setupPivotPoint(containerView)
        this.viewFront = viewFront
        this.viewBehind = viewBehind
        this.isFrontVisible = isFrontVisible
        val firstHalfAnimator = ObjectAnimator.ofFloat(
            containerView,
            "rotationY",
            0f,
            90f
        ).apply {
            duration = this@FlipAnimation.duration / 2
            interpolator = this@FlipAnimation.interpolator
        }

        val secondHalfAnimator = ObjectAnimator.ofFloat(
            containerView,
            "rotationY",
            90f,
            0f
        ).apply {
            duration = this@FlipAnimation.duration / 2
            interpolator = this@FlipAnimation.interpolator
        }

        firstHalfAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                rotate(isFrontVisible)
                secondHalfAnimator.start()
            }
        })

        secondHalfAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onFlipEnd?.invoke()
            }
        })

        firstHalfAnimator.start()
    }

    // Giữ lại method cũ để tương thích ngược (deprecated)
//    @Deprecated("Use flip with containerView parameter instead")
//    fun flip(viewFront: View, viewBehind: View, isFrontVisible: Boolean, onFlipEnd: (() -> Unit)? = null) {
//        // Thiết lập pivot point để view quay quanh trung tâm
//        setupPivotPoint(viewFront)
//        setupPivotPoint(viewBehind)
//
//        val firstHalfAnimator = ObjectAnimator.ofFloat(
//            if (isFrontVisible) viewFront else viewBehind,
//            "rotationY",
//            0f,
//            90f
//        ).apply {
//            duration = this@FlipAnimation.duration / 2
//            interpolator = this@FlipAnimation.interpolator
//        }
//
//        val secondHalfAnimator = ObjectAnimator.ofFloat(
//            if (isFrontVisible) viewBehind else viewFront,
//            "rotationY",
//            -90f,
//            0f
//        ).apply {
//            duration = this@FlipAnimation.duration / 2
//            interpolator = this@FlipAnimation.interpolator
//        }
//
//        firstHalfAnimator.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: Animator) {
//                rotate(isFrontVisible)
//                secondHalfAnimator.start()
//            }
//        })
//
//        secondHalfAnimator.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: Animator) {
//                onFlipEnd?.invoke()
//            }
//        })
//
//        firstHalfAnimator.start()
//    }

    private fun setupPivotPoint(view: View) {
        // Đảm bảo view đã được layout trước khi thiết lập pivot
        view.post {
            // Thiết lập pivot point ở giữa view theo trục X và Y
            view.pivotX = view.width / 2f
            view.pivotY = view.height / 2f
        }
    }

    private fun rotate(isFrontVisible: Boolean){
        viewFront?.isVisible = !isFrontVisible
        viewBehind?.isVisible = isFrontVisible
    }
}