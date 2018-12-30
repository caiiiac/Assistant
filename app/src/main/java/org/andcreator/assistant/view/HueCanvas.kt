package org.andcreator.assistant.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Created by zhangfan on 17-7-26.
 */
class HueCanvas @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var colors: IntArray
    var positions: FloatArray
    var paintBg = Paint()

    init {
        paintBg.isAntiAlias = true;
        colors = (0..360).map { Color.HSVToColor(floatArrayOf(it * 1f, 100f, 100f)) }.toIntArray()
        positions = (0..360).map { it / 360f }.toFloatArray()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paintBg.shader = LinearGradient(0f, 0f, measuredWidth.toFloat(), 0f, colors, positions, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paintBg)
    }
}