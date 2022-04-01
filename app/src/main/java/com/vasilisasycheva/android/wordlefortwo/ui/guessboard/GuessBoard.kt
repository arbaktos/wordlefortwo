package com.vasilisasycheva.android.wordlefortwo.ui.guessboard

import android.content.Context
import android.graphics.Typeface
import android.text.InputFilter
import android.util.AttributeSet
import android.view.Gravity
import android.view.View.MeasureSpec.getSize
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.children
import com.vasilisasycheva.android.wordlefortwo.R
import com.vasilisasycheva.android.wordlefortwo.extensions.flipAnimation
import com.vasilisasycheva.android.wordlefortwo.extensions.pixelsToSp
import com.vasilisasycheva.android.wordlefortwo.ui.keyboard.GuessState

class GuessBoard@JvmOverloads constructor(
    val ctx: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ViewGroup(ctx, attributeSet, defStyle) {

    private var squareWidth = 0
    private var squareHeight = 0
    private val padding = 10
    private var gbWidth = 0
    private var gbHeight = 0

    init {
        repeat(6) {
            addView(RowOfSquares(ctx))
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var top = 0
        var bottom = squareHeight
        children.forEach { child ->
            child.layout(l, top, r, bottom)
            top += squareHeight
            bottom += top + padding
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        gbHeight = getSize(heightMeasureSpec)
        gbWidth = getSize(widthMeasureSpec)

        var usedHeight = 0
        children.forEach { child ->
            child.measure(widthMeasureSpec, heightMeasureSpec)
            usedHeight += child.measuredHeight + padding
        }
        setMeasuredDimension(widthMeasureSpec, usedHeight + padding)
    }

    inner class RowOfSquares @JvmOverloads constructor(
        val ctx: Context,
        attributeSet: AttributeSet? = null,
        defStyle: Int = 0
    ) : ViewGroup(ctx, attributeSet, defStyle) {

        init {
            repeat(5) { num ->
                addView(
                    Square(ctx))
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val child = children.first()
            child.measure(widthMeasureSpec, heightMeasureSpec)
            val height = child.measuredHeight
            setMeasuredDimension(widthMeasureSpec, height)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            var left = (gbWidth - (squareWidth + padding) * 5) / 2
            children.forEach { it as Square
                it.layout(left, padding, left + squareWidth, squareHeight)
                left += squareWidth + padding
            }
        }


    }

    inner class Square(private val ctx: Context): androidx.appcompat.widget.AppCompatEditText(ctx, null, 0) {

        init {
            filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
            isAllCaps = true
            background = ctx.getDrawable(R.drawable.et_bg)
            showSoftInputOnFocus = false
            isCursorVisible = false
            isFocusableInTouchMode = false
            setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val tSize = height * 0.7f
            textSize = ctx.pixelsToSp(tSize)
            val paddingV = (height * 0.07).toInt()
            setPadding(0, paddingV, 0, paddingV)
            gravity = Gravity.CENTER
            super.onLayout(changed, left, top, right, bottom)
        }

        fun setSquareStatus(guessState: GuessState = GuessState.Default) {
//            this.background = ctx.getDrawable(status.etColor)
            this.flipAnimation(guessState.etColor, ctx)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            squareHeight = (gbHeight - padding) / 6 //(squareWidth * 1.2).toInt()
            squareWidth = (squareHeight * 0.8).toInt()
            setMeasuredDimension(squareWidth, squareHeight)
        }
    }

}