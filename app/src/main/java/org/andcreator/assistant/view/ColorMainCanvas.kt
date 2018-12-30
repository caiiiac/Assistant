package org.andcreator.assistant.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


/**
 * Created by zhangfan on 17-7-26.
 */

class ColorMainCanvas @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    val paintBg = Paint()

    init {
        paintBg.isAntiAlias = true
        paintBg.color = Color.RED

        //ComposeShader默认不能使用两个相同类型的shader进行合并，若要支持这个特性需要关闭硬件加速。
        //见https://stackoverflow.com/questions/12445583/issue-with-composeshader-on-android-4-1-1
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    var color = Color.RED
        set(value) {
            field = value
            postInvalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val vertical = LinearGradient(0f, 0f, 0f, measuredHeight.toFloat(), Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP)
        val horizontal = LinearGradient(0f, 0f, measuredWidth.toFloat(), 0f, Color.WHITE, color, Shader.TileMode.CLAMP)
        val composeShader = ComposeShader(horizontal, vertical, PorterDuff.Mode.MULTIPLY)
        paintBg.shader = composeShader

        canvas.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paintBg);

    }

}
