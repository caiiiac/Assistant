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
class HuePicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

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
        val x = progress * measuredWidth
        canvas.drawLine(x, 0f, x, measuredHeight.toFloat(), paintFocus)
//        canvas.drawCircle(progress * measuredWidth, 0f, circleRadius1, paintFocus)
        paintFocus.color = Color.WHITE
//        canvas.drawCircle(progress * measuredWidth, 0f, circleRadius2, paintFocus)

        val left = x - strokeWith
        val top = 0f
        val right = x + strokeWith
        val bottom = measuredHeight.toFloat()
        canvas.drawRect(left, top, right, bottom, paintFocus)
    }

    var progress = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currentX = Math.max(0f, Math.min(measuredWidth.toFloat(), event.x))
        progress = currentX / measuredWidth
        onChoose?.invoke(progress)
        postInvalidate()
        return true
    }

    var onChoose: ((Float) -> Unit)? = null

    fun choose(progress: Float) {
        this.progress = progress
        postInvalidate()
    }
}