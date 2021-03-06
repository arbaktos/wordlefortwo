package com.vasilisasycheva.android.wordlefortwo.ui.keyboard

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec.getSize
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import com.vasilisasycheva.android.wordlefortwo.R
import com.vasilisasycheva.android.wordlefortwo.extensions.dpToIntPx
import com.vasilisasycheva.android.wordlefortwo.extensions.flipAnimation


class Keyboard @JvmOverloads constructor(
    val ctx: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ViewGroup(ctx, attributeSet, defStyle) {

    private val rowList: List<List<Key>> = getRuKeyboard()
    val padding = 10
    var textButtonWidth = 0
    var keyboardClicksInt: KeyboardClicksInt? = null
    var rowHeight = 0
    var keyboardWidth = 0
    var llSize = 0

    init {
        rowList.forEach {
            addView(
                Row(ctx, it)
            )
        }
    }

    fun isEnabled(isEnabled: Boolean) {
        /*Keyboard in enabled only when the row is empty so EnterKey should be disabled by default*/
        this.children.forEach { row ->
            row as Row
            row.children.forEach {
                if (it !is EnterKey) it.isEnabled = isEnabled
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var top = 0
        children.forEach {
            it.layout(l, top, r, top + rowHeight)
            top += rowHeight + padding
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        this.keyboardWidth = getSize(widthMeasureSpec)
        var usedHeight = 0
        children.forEach { child ->
            child.measure(
                widthMeasureSpec,
                heightMeasureSpec
            )
            usedHeight += child.measuredHeight + padding
        }
        setMeasuredDimension(widthMeasureSpec, usedHeight)
    }

    inner class Row(private val ctx: Context, private val letterList: List<Key>) :
        ViewGroup(ctx, null, 0) {
        init {

            letterList.forEach {
                when (it) {
                    is TextKey -> {
                        addView(
                            TextKey(ctx, it.label).apply {
                                /* llsize is used to calculate text buttons width
                                * to calculate correct size we need to take maximum number of buttons in a row
                                * so we calculate the narrowest text button as standard*/
                                if (llSize < letterList.size) llSize = letterList.size
                            })
                    }
                    is BackSpaceKey -> {
                        addView(
                            BackSpaceKey(ctx, it.imageRes)
                        )
                    }
                    is EnterKey -> {
                        addView(
                            EnterKey(ctx, it.label)
                        )
                    }
                }
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val child = getChildAt(0)
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val heightRow = child.measuredHeight
            setMeasuredDimension(widthMeasureSpec, heightRow)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            val textKeyNum = letterList.count { it is TextKey }
            val widthMinusPaddings = width - letterList.size * padding - padding // empty space in a row
            val gap = widthMinusPaddings - textKeyNum * textButtonWidth
            var left = if (gap < padding || children.first() !is TextKey) padding else gap / 2
            val bottom = rowHeight

            children.forEach {
                when (it) {
                    is TextKey -> {
                        it.layout(left, padding, left + textButtonWidth, bottom)
                        left += it.width + padding
                    }
                    is BackSpaceKey -> {
                        it.layout(left, padding, left + gap / 2, bottom)
                        left += gap / 2 + padding
                    }
                    is EnterKey -> {
                        it.layout(left, padding, left + gap / 2, bottom)
                    }
                }
            }
        }
    }

    inner class TextKey(val ct: Context, val label: String) :
        androidx.appcompat.widget.AppCompatButton(ct, null, 0),
        OnClickListener,
        Key {

        init {
            setOnClickListener(this)
            text = label
            gravity = Gravity.CENTER_HORIZONTAL
            setKeyState(GuessState.Default)
            isClickable = true
            elevation = 12f
            textSize = ctx.dpToIntPx(8).toFloat()
            isAllCaps = true
            setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        }

        fun setKeyState(guessState: GuessState = GuessState.Default) {
            /* So animation would be shown only when th game in on not os startup*/
            if (guessState != GuessState.Default) this.flipAnimation(guessState.keyColor, ct)
            else background = AppCompatResources.getDrawable(ct, guessState.keyColor)
        }

        override fun onClick(v: View?) {
            keyboardClicksInt?.onTextClick(this)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            textButtonWidth =
                (this@Keyboard.keyboardWidth - (llSize * padding + padding)) / llSize
            rowHeight = textButtonWidth * 2
            setMeasuredDimension(textButtonWidth, rowHeight)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val paddingVert =
                (rowHeight - textSize.toInt() - paddingTop - paddingBottom - padding * 2) / 2
            setPadding(0, paddingVert, 0, paddingVert)
            super.onLayout(changed, left, top, right, bottom)
        }
    }

    inner class BackSpaceKey(ct: Context = ctx, val imageRes: Int) :
        androidx.appcompat.widget.AppCompatImageButton(ctx, null, 0),
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

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(widthMeasureSpec, rowHeight)
        }
    }

    inner class EnterKey(ct: Context, val label: String) : AppCompatButton(ctx, null, 0),
        OnClickListener,
        Key {
        init {
            setOnClickListener(this)
            text = label
            background = ct.getDrawable(R.drawable.btn_bg_pressed)
            gravity = Gravity.CENTER
            typeface = Typeface.MONOSPACE
        }

        override fun onClick(v: View?) {
            keyboardClicksInt?.onEnterClick()
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val paddingVert =
                (rowHeight - textSize.toInt() - paddingTop - paddingBottom - padding * 2) / 2
            setPadding(0, paddingVert, 0, paddingVert)
            super.onLayout(changed, left, top, right, bottom)
        }
    }

    fun setupKeyboardClicks(implementation: KeyboardClicksInt) {
        keyboardClicksInt = implementation
    }


    private fun getRuKeyboard(): List<List<Key>> {
        return listOf(
            listOf(
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
            ),

            listOf(
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
            ),


            listOf(
                BackSpaceKey(ctx, R.drawable.ic_baseline_backspace_24),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                TextKey(ctx, "??"),
                EnterKey(ctx, "????????")
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