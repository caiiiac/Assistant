package org.andcreator.assistant.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


/**
 * Created by zhangfan on 17-7-26.
 */

class ColorMainPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var paintFocus = Paint()
    val strokeWith = dp2px(2f)
    val circleRadius1 = dp2px(8f)
    val circleRadius2 = dp2px(10f)

    private fun dp2px(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)

    init {
        paintFocus.isAntiAlias = true
        paintFocus.style = Paint.Style.STROKE
        paintFocus.strokeWidth = strokeWith
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paintFocus.color = Color.BLACK
        val cx = progressX * measuredWidth
        val cy = progressY * measuredHeight
        canvas.drawCircle(cx, cy, circleRadius1, paintFocus)
        paintFocus.color = Color.WHITE
        canvas.drawCircle(cx, cy, circleRadius2, paintFocus)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currentX = Math.max(0f, Math.min(measuredWidth.toFloat(), event.x))
        val currentY = Math.max(0f, Math.min(measuredHeight.toFloat(), event.y))
        progressX = currentX / measuredWidth
        progressY = currentY / measuredHeight
        onChoose?.invoke(progressX, 1 - progressY)
        postInvalidate()
        return true
    }

    var onChoose: ((Float, Float) -> Unit)? = null

    var progressX = 0f
    var progressY = 0f

    fun choose(progressX: Float, progressY: Float) {
        this.progressX = progressX
        this.progressY = 1 - progressY
        postInvalidate()
    }

}
