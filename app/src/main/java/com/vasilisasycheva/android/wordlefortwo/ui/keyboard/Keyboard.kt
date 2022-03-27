package com.vasilisasycheva.android.wordlefortwo.ui.keyboard

import android.content.Context
import android.icu.util.Measure
import android.media.MediaDrm
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec.getSize
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import com.vasilisasycheva.android.wordlefortwo.R
import com.vasilisasycheva.android.wordlefortwo.extensions.dpToIntPx
import com.vasilisasycheva.android.wordlefortwo.extensions.pixelsToSp
import com.vasilisasycheva.android.wordlefortwo.ui.DEBUG_TAG


class Keyboard @JvmOverloads constructor(
    val ctx: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0)
    : ViewGroup(ctx, attributeSet, defStyle) {

    private val rowList: List<List<Key>> = getRuKeyboard()
    val padding = 10
    var textButtonWidth = 1
    var keyboardClicksInt: KeyboardClicksInt? = null
    var rowHeight = 1

    init {
        rowList.forEach {
            addView(
                Row(ctx, it)
            )
        }
    }

    fun isEnabled(isEnabled: Boolean) {
        this.children.forEach { it as Row
            it.children.forEach {
                it.isEnabled = isEnabled
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        textButtonWidth = (width - (rowList[0].size * padding + padding)) / rowList[0].size
        rowHeight = textButtonWidth * 2
        var top = 0
        children.forEach {
            it.layout(l, top, r, top + rowHeight)
            top += rowHeight + padding
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = 0
        children.forEach { child ->
            child.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            usedHeight += child.measuredHeight
        }
        setMeasuredDimension(widthMeasureSpec, usedHeight + padding)
    }

    inner class Row(private val ctx: Context, private val letterList: List<Key>): ViewGroup(ctx, null, 0) {
        init {
            letterList.forEach {
                when (it) {
                    is TextKey -> {
                        addView(
                            TextKey(ctx, it.label).apply {

//                                typeface = font
                        })
                    }
                    is BackSpaceKey -> {
                        addView(
                            BackSpaceKey(ctx, it.imageRes)
                        )
                    }
                    is EnterKey -> {
                        addView(
                            EnterKey(ctx).apply {
//                                typeface = font
                        })

                    }
                }
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val child =  getChildAt(0)
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val height = child.measuredHeight * 2
            setMeasuredDimension(widthMeasureSpec, height)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            val textKeyNum = letterList.count { it is TextKey }
            val width = getWidth() - letterList.size * padding - padding
            val gap = width - textKeyNum * textButtonWidth
            var left = if (gap < padding || children.first() !is TextKey) padding else gap/2
            val bottom = rowHeight

            children.forEach {
                when (it) {
                    is TextKey -> {
                        it.layout(left, padding, left + textButtonWidth, bottom)
                        left += it.width + padding
                    }
                    is BackSpaceKey -> {
                        it.layout(left, padding, left + gap/2, bottom)
                        left += gap/2 + padding
                    }
                    is EnterKey -> {
                        it.layout(left, padding, left + gap/2, bottom)
                    }
                }
            }
        }
    }

    inner class TextKey(val ct: Context, val label: String)
        : androidx.appcompat.widget.AppCompatButton(ct, null, 0)
        , OnClickListener
        , Key {
            init {
                setOnClickListener(this)
                text = label
                gravity = Gravity.CENTER
                setKeyState()
                isClickable = true
                elevation = 12f
                textSize = ctx.dpToIntPx(8).toFloat()
                isAllCaps = true
                setPadding(0, padding * 2, 0, 0)
            }

        fun setKeyState(guessState: GuessState = GuessState.Default) {
            background = AppCompatResources.getDrawable(ct, guessState.keyColor)
        }

        override fun onClick(v: View?) {
            keyboardClicksInt?.onTextClick(this)
        }
    }

    inner class BackSpaceKey(ct: Context = ctx, val imageRes: Int)
        : androidx.appcompat.widget.AppCompatImageButton(ctx, null, 0),
        OnClickListener,
        Key {
            init {
                setOnClickListener(this)
                setImageResource(imageRes)
                scaleType = ScaleType.CENTER_INSIDE
                background = ct.getDrawable(R.drawable.btn_bg_pressed)
            }

        override fun onClick(v: View?) {
            keyboardClicksInt?.onBackspaceClick()
        }
    }

    inner class EnterKey(ct: Context): AppCompatButton(ctx, null, 0),
        OnClickListener,
        Key
    {
        init {
            setOnClickListener(this)
            text = "ВВОД"
            background = ct.getDrawable(R.drawable.btn_bg_pressed)
            gravity = Gravity.CENTER
            setPadding(0, 25, 0, 0)
        }

        override fun onClick(v: View?) {
            keyboardClicksInt?.onEnterClick()
        }
    }

    fun setupKeyboardClicks(implementation: KeyboardClicksInt) {
        keyboardClicksInt = implementation
    }



    private fun getRuKeyboard(): List<List<Key>> {
        return listOf(
            listOf(
                TextKey(ctx, "й"),
                TextKey(ctx, "ц"),
                TextKey(ctx, "у"),
                TextKey(ctx, "к"),
                TextKey(ctx, "е"),
                TextKey(ctx, "н"),
                TextKey(ctx, "г"),
                TextKey(ctx, "ш"),
                TextKey(ctx, "щ"),
                TextKey(ctx, "з"),
                TextKey(ctx, "х"),
                TextKey(ctx, "ъ"),
            ),

            listOf(
                TextKey(ctx, "ф"),
                TextKey(ctx, "ы"),
                TextKey(ctx, "в"),
                TextKey(ctx, "а"),
                TextKey(ctx, "п"),
                TextKey(ctx, "р"),
                TextKey(ctx, "о"),
                TextKey(ctx, "л"),
                TextKey(ctx, "д"),
                TextKey(ctx, "ж"),
                TextKey(ctx, "э"),
            ),


            listOf(BackSpaceKey(ctx, R.drawable.ic_baseline_backspace_24),
            TextKey(ctx, "я"),
            TextKey(ctx, "ч"),
            TextKey(ctx, "с"),
            TextKey(ctx, "м"),
            TextKey(ctx, "и"),
            TextKey(ctx, "т"),
            TextKey(ctx, "ь"),
            TextKey(ctx, "б"),
            TextKey(ctx, "ю"),
            EnterKey(ctx)
        )
        )
    }

}

interface Key

enum class GuessState(val keyColor: Int, val etColor: Int) {
    Default(R.drawable.btn_bg_pressed, R.drawable.et_bg),
    Positionmatch(R.drawable.position_match_bg, R.drawable.et_pos_match_bg),
    Charmatch(R.drawable.char_match_bg, R.drawable.et_char_match_bg),
    Miss(R.drawable.miss_bg, R.drawable.et_miss_bg)
}