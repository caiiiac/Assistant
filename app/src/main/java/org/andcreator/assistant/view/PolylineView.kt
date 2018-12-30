package org.andcreator.assistant.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import org.andcreator.assistant.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class PolylineView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    /**
     * 背景画笔
     */
    private var backgroundPaint: Paint = Paint()
    private var backgroundWhitePaint: Paint = Paint()
    /**
     * 前景画笔
     */
    private var frequencyPaint: Paint = Paint()

    private var data = arrayOf(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)

    /**
     * 画布
     */
    private var canvas: Canvas? = null

    private val handler= @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg!!.what){
                1 -> {
//                    drawFrequency(data)
                    Log.e("xxxxxxxxxxxxxxxxx",data[1].toString())
                }
            }
        }
    }

    private lateinit var calculateThread: Thread

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        backgroundWhitePaint.color = resources.getColor(R.color.white)
        backgroundWhitePaint.style = Paint.Style.FILL

        if (canvas != null){
            this.canvas = canvas

            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundWhitePaint)

            drawBackground()
            drawFrequency(data)
        }

    }

    private fun drawBackground(){

        val viewWidth = measuredWidth-2f
        val viewHeight = measuredHeight-2f
        val viewWidthLines = viewWidth/20
        val viewHeightLines = viewHeight/10

        backgroundPaint.strokeWidth = 2f
        backgroundPaint.color = resources.getColor(R.color.colorPrimary)

        canvas!!.drawLines(
            floatArrayOf(// 绘制一组线 每四数字(两个点的坐标)确定一条线
                2f, 2f, 2f, viewHeight,
                viewWidthLines, 2f,viewWidthLines, viewHeight,
                viewWidthLines*2, 2f,viewWidthLines*2, viewHeight,
                viewWidthLines*3, 2f,viewWidthLines*3, viewHeight,
                viewWidthLines*4, 2f,viewWidthLines*4, viewHeight,
                viewWidthLines*5, 2f,viewWidthLines*5, viewHeight,
                viewWidthLines*6, 2f,viewWidthLines*6, viewHeight,
                viewWidthLines*7, 2f,viewWidthLines*7, viewHeight,
                viewWidthLines*8, 2f,viewWidthLines*8, viewHeight,
                viewWidthLines*9, 2f,viewWidthLines*9, viewHeight,
                viewWidthLines*10, 2f,viewWidthLines*10, viewHeight,
                viewWidthLines*11, 2f,viewWidthLines*11, viewHeight,
                viewWidthLines*12, 2f,viewWidthLines*12, viewHeight,
                viewWidthLines*13, 2f,viewWidthLines*13, viewHeight,
                viewWidthLines*14, 2f,viewWidthLines*14, viewHeight,
                viewWidthLines*15, 2f,viewWidthLines*15, viewHeight,
                viewWidthLines*16, 2f,viewWidthLines*16, viewHeight,
                viewWidthLines*17,2f,viewWidthLines*17, viewHeight,
                viewWidthLines*18, 2f,viewWidthLines*18, viewHeight,
                viewWidthLines*19, 2f,viewWidthLines*19, viewHeight,
                viewWidthLines*20, 2f,viewWidthLines*20, viewHeight,
                2f,2f,viewWidth,2f,
                2f,viewHeightLines,viewWidth,viewHeightLines,
                2f,viewHeightLines*1,viewWidth,viewHeightLines*1,
                2f,viewHeightLines*2,viewWidth,viewHeightLines*2,
                2f,viewHeightLines*3,viewWidth,viewHeightLines*3,
                2f,viewHeightLines*4,viewWidth,viewHeightLines*4,
                2f,viewHeightLines*5,viewWidth,viewHeightLines*5,
                2f,viewHeightLines*6,viewWidth,viewHeightLines*6,
                2f,viewHeightLines*7,viewWidth,viewHeightLines*7,
                2f,viewHeightLines*8,viewWidth,viewHeightLines*8,
                2f,viewHeightLines*9,viewWidth,viewHeightLines*9,
                2f,viewHeightLines*10,viewWidth,viewHeightLines*10,
                2f,viewHeightLines*11,viewWidth,viewHeightLines*11,
                2f,viewHeightLines*12,viewWidth,viewHeightLines*12,
                2f,viewHeightLines*13,viewWidth,viewHeightLines*13,
                2f,viewHeightLines*14,viewWidth,viewHeightLines*14,
                2f,viewHeightLines*15,viewWidth,viewHeightLines*15,
                2f,viewHeightLines*16,viewWidth,viewHeightLines*16,
                2f,viewHeightLines*17,viewWidth,viewHeightLines*17,
                2f,viewHeightLines*18,viewWidth,viewHeightLines*18,
                2f,viewHeightLines*19,viewWidth,viewHeightLines*19,
                2f,viewHeightLines*20,viewWidth,viewHeightLines*20), backgroundPaint
        )
    }

    private fun drawFrequency(data: Array<Float>){

        val viewWidth = measuredWidth.toFloat()-2
        val viewHeight = measuredHeight.toFloat()-2
        val viewWidthLines = viewWidth/(data.size-1)

        val path = Path()
        frequencyPaint.color = resources.getColor(R.color.transColorAccent)
        path.moveTo(0f, viewHeight)
        path.lineTo(viewWidth, viewHeight)

        for ((index, value) in data.withIndex()){
            path.lineTo(viewWidthLines*(data.size-index-1),viewHeight-value*viewHeight)
        }

        if (canvas!= null){

            canvas!!.drawPath(path,frequencyPaint)
            canvas!!.save()

            invalidate()
        }
    }

    private lateinit var dataCopy: Array<Float>

    fun update(f: Float){
        if (measuredWidth > 0 && measuredHeight > 0) {

            calculateThread = object : Thread() {
                override fun run() {
                    super.run()

                    dataCopy = data.clone()

                    for ((index, value) in dataCopy.withIndex()) {
                        if (index == 0) {
                            data[index] = f
                        }
                        if (index < dataCopy.size - 1) {
                            data[index + 1] = dataCopy[index]
                        }

                    }
                }
            }
            calculateThread.start()

        }else{
            Log.e("加载失败","表格加载失败")
        }
    }
}