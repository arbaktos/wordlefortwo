package com.vasilisasycheva.android.wordlefortwo.extensions

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment

fun Context.dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        this.resources.displayMetrics
    )
}

fun Context.dpToIntPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        this.resources.displayMetrics
    ).toInt()
}

fun Fragment.dpToIntPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        this.requireContext().resources.displayMetrics
    ).toInt()
}

fun View.dpToIntPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        this.context.resources.displayMetrics
    ).toInt()
}

fun Context.pixelsToSp(px: Float): Float {
    val scaledDensity = this.resources.displayMetrics.scaledDensity
    return px / scaledDensity
}