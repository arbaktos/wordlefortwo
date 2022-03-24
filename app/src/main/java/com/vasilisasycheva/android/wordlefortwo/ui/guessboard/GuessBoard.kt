package com.vasilisasycheva.android.wordlefortwo.ui.guessboard

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.children
import com.vasilisasycheva.android.wordlefortwo.R
import com.vasilisasycheva.android.wordlefortwo.extensions.pixelsToSp
import com.vasilisasycheva.android.wordlefortwo.ui.keyboard.GuessState

class GuessBoard@JvmOverloads constructor(
    val ctx: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ViewGroup(ctx, attributeSet, defStyle) {

    private var squareWidth = 0
    private val padding = 10

    init {
        repeat(6) {
            addView(RowOfSquares(ctx).apply {
                pad = padding
            })
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        squareWidth = (width - padding * 6) / 7
        children.forEach { it as RowOfSquares
            it.sqWidth = squareWidth
        }
        var top = 0
        var bottom = squareWidth
        children.forEach { child ->
            child.layout(l, top, r, bottom)
            top += squareWidth
            bottom += top + padding
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = 0
        children.forEach { child ->
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            usedHeight += child.measuredHeight
        }
        setMeasuredDimension(widthMeasureSpec, usedHeight + padding)
    }

    class RowOfSquares @JvmOverloads constructor(
        val ctx: Context,
        attributeSet: AttributeSet? = null,
        defStyle: Int = 0
    ) : ViewGroup(ctx, attributeSet, defStyle) {

        var sqWidth = 0
        var pad = 0

        init {
            repeat(5) { num ->
                addView(
                    Square(ctx))
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//            val parentWidth = MeasureSpec.getSize(widthMeasureSpec);
//            val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
            val child = children.first()
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val height = child.measuredHeight * 3 - pad
            setMeasuredDimension(widthMeasureSpec, height)
//            setMeasuredDimension(parentWidth, parentHeight/6)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            var left = sqWidth + pad
            children.forEach { it as Square
                it.layout(left, pad, left + sqWidth, sqWidth)
                left += sqWidth + pad
            }
        }
    }

    class Square(private val ctx: Context): androidx.appcompat.widget.AppCompatEditText(ctx, null, 0) {

        init {
            filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
            isAllCaps = true
            background = ctx.getDrawable(R.drawable.et_bg)
            showSoftInputOnFocus = false
            isCursorVisible = false
            isFocusableInTouchMode = false
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val tSize = height * 0.7f
            textSize = ctx.pixelsToSp(tSize)
            val paddingV = (height * 0.1).toInt()
            val paddingH = (width * 0.2).toInt()
            setPadding(paddingH, paddingV, paddingH, paddingV)
            super.onLayout(changed, left, top, right, bottom)
        }

        fun setSquareStatus(status: GuessState = GuessState.Default) {
            this.background = ctx.getDrawable(status.etColor)
            invalidate()
        }
    }

}