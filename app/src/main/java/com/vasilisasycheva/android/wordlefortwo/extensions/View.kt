package com.vasilisasycheva.android.wordlefortwo.extensions

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.vasilisasycheva.android.wordlefortwo.R

fun View.flipAnimation(keyColor: Int, ct: Context) {
    try {
        val flipDownAnimationSet =
            AnimatorInflater.loadAnimator(
                context,
                R.animator.flip_down
            ) as AnimatorSet
        flipDownAnimationSet.setTarget(this)
        val flipUpAnimatorSet =
            AnimatorInflater.loadAnimator(
                context,
                R.animator.flip_up
            ) as AnimatorSet
        flipUpAnimatorSet.setTarget(this)

        flipDownAnimationSet.start()
        this.apply {
            background = AppCompatResources.getDrawable(ct, keyColor)
        }
        flipUpAnimatorSet.start()

    } catch (e: Exception) {
        Log.e("e", e.toString())
    }
}