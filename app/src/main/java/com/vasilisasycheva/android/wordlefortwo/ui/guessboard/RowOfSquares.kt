package com.vasilisasycheva.android.wordlefortwo.ui.guessboard
//
//import android.content.Context
//import android.graphics.Paint
//import android.text.InputFilter
//import android.util.AttributeSet
//import android.util.Log
//import android.view.ViewGroup
//import android.widget.EditText
//import androidx.annotation.Px
//import androidx.core.view.children
//import com.vasilisasycheva.android.wordlefortwo.R
//import com.vasilisasycheva.android.wordlefortwo.extensions.dpToIntPx
//import com.vasilisasycheva.android.wordlefortwo.extensions.pixelsToSp
//import com.vasilisasycheva.android.wordlefortwo.ui.keyboard.GuessState
//import com.vasilisasycheva.android.wordlefortwo.ui.keyboard.TAG
//
//
//class RowOfSquares @JvmOverloads constructor(
//    val ctx: Context,
//    attributeSet: AttributeSet? = null,
//    defStyle: Int = 0
//) : ViewGroup(ctx, attributeSet, defStyle) {
//
//    private var squareWidth = 0
//    private val padding = 10
////    @Px private val sqWidth = ctx.dpToIntPx(200)
////    @Px private val sqHeight = ctx.dpToIntPx(96)
//
//    init {
//        repeat(5) { num ->
//            addView(
//                Square(ctx).apply {
//                    id = num
////                    textColors = ctx.getColor(R.color.black)
//                })
//        }
//    }
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val child = children.first()
//        child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
//        val height = child.measuredHeight * 3 - padding
//        setMeasuredDimension(widthMeasureSpec, height)
//    }
//
//    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        squareWidth = (width - padding * 6) / 7
//        var left = squareWidth + padding
//        children.forEach { it as Square
//            it.layout(left, padding, left + squareWidth, squareWidth)
//            left += squareWidth + padding
//        }
//    }
//}
//
//class Square(private val ctx: Context): androidx.appcompat.widget.AppCompatEditText(ctx, null, 0) {
//
//    init {
//        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
//        isAllCaps = true
//        background = ctx.getDrawable(R.drawable.et_bg)
//        showSoftInputOnFocus = false
//        isCursorVisible = false
//        isFocusableInTouchMode = false
//    }
//
//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        val tSize = height * 0.8f
//        textSize = ctx.pixelsToSp(tSize)
//        val paddingV = (height * 0.1).toInt()
//        val paddingH = (width * 0.2).toInt()
//        setPadding(paddingH, paddingV, paddingH, paddingV)
//        super.onLayout(changed, left, top, right, bottom)
//    }
//
//    fun setSquareStatus(status: GuessState) {
//        this.background = ctx.getDrawable(status.etColor)
//        invalidate()
//    }
//}
//
