package com.vasilisasycheva.android.wordlefortwo.extensions

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.content.res.AppCompatResources
import com.vasilisasycheva.android.wordlefortwo.R


fun View.flipAnimation(keyColor: Int, ct: Context) {
    try {

        val flipAnimatorSet =
            AnimatorInflater.loadAnimator(
                context,
                R.animator.flip_up
            ) as AnimatorSet
        flipAnimatorSet.setTarget(this)
        val backBg = AppCompatResources.getDrawable(ct, keyColor)
        val backgrounds = listOf<Drawable>(this.background, backBg!!)

        val transitionDrawable = TransitionDrawable(backgrounds.toTypedArray())
        this.background = transitionDrawable
        transitionDrawable.startTransition(1000);
        flipAnimatorSet.start()

    } catch (e: Exception) {
        Log.e("e", e.toString())
    }
}

fun View.shake() {
    val shakeAnim = AnimationUtils.loadAnimation(this.context, R.anim.shake)
    this.startAnimation(shakeAnim)
}