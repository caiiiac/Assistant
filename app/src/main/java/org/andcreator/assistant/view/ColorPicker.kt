package org.andcreator.assistant.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.color_picker.view.*
import org.andcreator.assistant.R


/**
 * Created by zhangfan on 17-7-26.
 */

class ColorPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    var currentColor = floatArrayOf(0f, 1f, 1f)
        private set

    fun setColor(color: Int) {
        Color.colorToHSV(color, currentColor)
        main_canvas.color = Color.HSVToColor(floatArrayOf(currentColor[0], 1f, 1f))
        main_picker.choose(currentColor[1], currentColor[2])
        hue_picker.choose(currentColor[0] / 360)
        calcColor()
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.color_picker, this, false)
        addView(view)
        val layoutParams = view.layoutParams
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT
        view.hue_picker.onChoose = { progress ->
            view.main_canvas.color = Color.HSVToColor(floatArrayOf(progress * 360, 1f, 1f))
            currentColor[0] = progress * 360
            calcColor()
        }
        view.main_picker.onChoose = { px, py ->
            currentColor[1] = px
            currentColor[2] = py
            calcColor()
        }
        calcColor()

    }

    fun calcColor() {
        val color = Color.HSVToColor(currentColor)
        onChoose?.invoke(color)
    }

    var onChoose: ((Int) -> Unit)? = null

}
