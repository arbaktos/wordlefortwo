package com.vasilisasycheva.android.wordlefortwo.ui.keyboard

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec.getSize
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import com.vasilisasycheva.android.wordlefortwo.R
import com.vasilisasycheva.android.wordlefortwo.extensions.dpToIntPx
import com.vasilisasycheva.android.wordlefortwo.ui.DEBUG_TAG

class RowTest@JvmOverloads constructor(
              val ctx: Context,
              attributeSet: AttributeSet? = null,
              defStyle: Int = 0)
    : ViewGroup(ctx, attributeSet, defStyle) {

    private val letterList: List<Key> = getRuKeyboard().first()
    val padding = 10
    var textButtonWidth = 1
    var keyboardClicksInt: KeyboardClicksInt? = null
    var rowHeight = 1

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
        val child = getChildAt(0)
        child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        val height = child.measuredHeight
        Log.d(DEBUG_TAG, "onMeasure Row")
        val width = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.AT_MOST)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        val textKeyNum = letterList.count { it is TextKey }
        val width = getWidth() - letterList.size * padding - padding
        val gap = width - textKeyNum * textButtonWidth
        var left = if (gap < padding || children.first() !is TextKey) padding else gap / 2
        val bottom = rowHeight
        Log.d(DEBUG_TAG, "onLayout Row")

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

    inner class TextKey(val ct: Context, val label: String) :
        androidx.appcompat.widget.AppCompatButton(ct, null, 0), View.OnClickListener, Key {
        var tkPadding: Int? = null

        init {
            setOnClickListener(this)
            text = label
            gravity = Gravity.CENTER
            setKeyState()
            isClickable = true
            elevation = 12f
            textSize = ctx.dpToIntPx(8).toFloat()
            isAllCaps = true
            tkPadding = ((measuredHeight + textSize)/2).toInt()
            setPadding(0, tkPadding!!, 0, tkPadding!!)
        }

        fun setKeyState(guessState: GuessState = GuessState.Default) {
            background = AppCompatResources.getDrawable(ct, guessState.keyColor)
        }

        override fun onClick(v: View?) {
            keyboardClicksInt?.onTextClick(this)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val parentWidth = getSize(widthMeasureSpec)
            textButtonWidth = (parentWidth - (letterList.size * padding + padding)) / letterList.size
            rowHeight = textButtonWidth * 2
            setMeasuredDimension(textButtonWidth, rowHeight)
            Log.d(DEBUG_TAG, "onMeasure TextKey")
            Log.d(DEBUG_TAG, "parentWidth $parentWidth")
            Log.d(DEBUG_TAG, "textButtonWidth: " + textButtonWidth.toString())
            Log.d(DEBUG_TAG, "rowHeight: " +rowHeight.toString())
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            Log.d(DEBUG_TAG, "onLayout TextKey")
            super.onLayout(changed, left, top, right, bottom)
        }
    }

    inner class BackSpaceKey(ct: Context = ctx, val imageRes: Int) :
        androidx.appcompat.widget.AppCompatImageButton(ctx, null, 0),
        View.OnClickListener,
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

    inner class EnterKey(ct: Context) : AppCompatButton(ctx, null, 0),
        OnClickListener,
        Key {
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


            listOf(
                BackSpaceKey(ctx, R.drawable.ic_baseline_backspace_24),
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
