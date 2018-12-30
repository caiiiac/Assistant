package org.andcreator.assistant.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

class NightModeDrawable: Drawable() {

    private val paint = Paint()

    init {

        paint.color = Color.BLACK

    }

    override fun draw(canvas: Canvas?) {
        canvas?.drawRect(bounds,paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

}