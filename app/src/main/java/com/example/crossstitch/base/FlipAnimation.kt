package com.example.crossstitch.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator

class FlipAnimation(
    private val duration: Long = 600L
) {

    private val interpolator = DecelerateInterpolator()

    fun flip(viewFront: View, viewBehind: View, isFrontVisible: Boolean, onFlipEnd: (() -> Unit)? = null) {
        // Thiết lập pivot point để view quay quanh trung tâm
        setupPivotPoint(viewFront)
        setupPivotPoint(viewBehind)

        val firstHalfAnimator = ObjectAnimator.ofFloat(
            if (isFrontVisible) viewFront else viewBehind,
            "rotationY",
            0f,
            90f
        ).apply {
            duration = this@FlipAnimation.duration / 2
            interpolator = this@FlipAnimation.interpolator
        }

        val secondHalfAnimator = ObjectAnimator.ofFloat(
            if (isFrontVisible) viewBehind else viewFront,
            "rotationY",
            -90f,
            0f
        ).apply {
            duration = this@FlipAnimation.duration / 2
            interpolator = this@FlipAnimation.interpolator
        }

        firstHalfAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Đổi visibility khi animation quay tới 90 độ
                if (isFrontVisible) {
                    viewFront.visibility = View.GONE
                    viewBehind.visibility = View.VISIBLE
                } else {
                    viewFront.visibility = View.VISIBLE
                    viewBehind.visibility = View.GONE
                }
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

    private fun setupPivotPoint(view: View) {
        // Đảm bảo view đã được layout trước khi thiết lập pivot
        view.post {
            // Thiết lập pivot point ở giữa view theo trục X và Y
            view.pivotX = view.width / 2f
            view.pivotY = view.height / 2f
        }
    }
}