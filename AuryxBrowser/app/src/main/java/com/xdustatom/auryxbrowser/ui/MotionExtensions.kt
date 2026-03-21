package com.xdustatom.auryxbrowser.ui

import android.view.MotionEvent
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

private val easing = FastOutSlowInInterpolator()

fun View.applyPressAnimation(
    idleScale: Float = 1f,
    pressedScale: Float = 0.985f
) {
    setOnTouchListener { v, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                v.animate()
                    .scaleX(pressedScale)
                    .scaleY(pressedScale)
                    .setInterpolator(easing)
                    .setDuration(90L)
                    .start()
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(idleScale)
                    .scaleY(idleScale)
                    .setInterpolator(easing)
                    .setDuration(140L)
                    .start()
            }
        }
        false
    }
}

fun View.animateEntrance(
    delay: Long = 0L,
    fromY: Float = 18f
) {
    alpha = 0f
    translationY = fromY
    animate()
        .alpha(1f)
        .translationY(0f)
        .setInterpolator(easing)
        .setStartDelay(delay)
        .setDuration(220L)
        .start()
}

fun View.crossfadeVisible(
    show: Boolean,
    duration: Long = 160L
) {
    if (show) {
        if (visibility != View.VISIBLE) {
            alpha = 0f
            visibility = View.VISIBLE
        }
        animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(easing)
            .start()
    } else {
        if (visibility != View.VISIBLE) return
        animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(easing)
            .withEndAction { visibility = View.GONE }
            .start()
    }
}
