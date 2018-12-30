package org.andcreator.assistant.layout

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import org.andcreator.assistant.listener.ResizeWidgetListener
import java.util.ArrayList


class DragLayer: FrameLayout {

    // Variables relating to resizing widgets
    //与调整小部件大小有关的变量
    private val mResizeFrame = HashMap<String, AppWidgetResizeFrame>()
    private lateinit var mCurrentResizeFrame: AppWidgetResizeFrame
    private lateinit var mCurrentResizeFrameId: String
    private var mXDown: Int = 0
    private var mYDown:Int = 0
    private var moveId: String = ""
    private var isShow = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            when(ev.action){
                MotionEvent.ACTION_DOWN ->{
                    Log.e("onInterceptTouchEvent","onInterceptTouchEvent")
                    if (handleTouchDown(ev)) {
                        return true
                    }else{

                        if (isShow){
                            isShow = false
                            hideResizeFrame()
                        }
                    }

                }
            }

        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

//        Log.e("onTouchEvent","onTouchEvent")
        if (event != null) {

            when(event.action){
                MotionEvent.ACTION_DOWN ->{
                    return handleTouchDown(event)
                }

                MotionEvent.ACTION_MOVE ->{

                    resizeWidget.onMove(mCurrentResizeFrameId,event.x.toInt(), event.y.toInt(),mCurrentResizeFrame.visualizeResizeForDelta(event.x.toInt(), event.y.toInt()))

                }

                MotionEvent.ACTION_UP ->{
//                    mCurrentResizeFrame.visualizeResizeForDelta((x - mXDown).toInt(), (y - mYDown).toInt())
                    mCurrentResizeFrame.onTouchUp()
                    resizeWidget.onUp()
                }
            }

        }
        return true
    }

    private fun handleTouchDown(event: MotionEvent): Boolean{
        val hitRect = Rect()
        val x = event.x.toInt()
        val y = event.y.toInt()

        for ((key,value) in mResizeFrame) {
            if (value.visibility != View.VISIBLE)
                continue
            value.getHitRect(hitRect)
            if (hitRect.contains(x, y)) {
                if (value.beginResizeIfPointInRegion(x - value.left, y - value.top)) {

                    mCurrentResizeFrame = value
                    mCurrentResizeFrameId = key
                    mXDown = x
                    mYDown = y
                    requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
        }
        return false
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        for ((key,value) in mResizeFrame){

            value.snapToWidget(false)

        }
        Log.e("ResizeFrame",mResizeFrame.size.toString())
    }

    fun addResizeFrame(context: Context, widget: View, cell: CellLayout.Cell){

        val resizeFrame = AppWidgetResizeFrame(context,widget,cell, this)
        val lp = LayoutParams(-1, -1)
        addView(resizeFrame, lp)
        mResizeFrame[cell.id] = resizeFrame
        resizeFrame.snapToWidget(false)
        resizeFrame.visibility = View.GONE
        Log.e("addResizeFrame","addResizeFrame")
    }

    fun moveResizeFrame(id: String){
        moveId = id

        val resizeFrame = mResizeFrame[id]
        resizeFrame?.snapToWidget(false)
        Log.e("moveResizeFrame",id)
    }

    fun removeResizeFrame(id: String){
        val resizeFrame = mResizeFrame[id]
        this.removeView(resizeFrame)
        mResizeFrame.remove(id)
        Log.e("removeResizeFrame",id)
    }

    fun hideResizeFrame(){
        for ((key,value) in mResizeFrame){
            val animator = ObjectAnimator.ofFloat(value, "alpha", 1f, 0f)
            animator.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {

                }
                override fun onAnimationEnd(animation: Animator?) {
                    if (value.visibility != View.GONE){
                        value.visibility = View.GONE
                        isShow = false
                    }
                }
                override fun onAnimationCancel(animation: Animator?) {

                }
                override fun onAnimationStart(animation: Animator?) {

                }
            })
            animator.setDuration(400).start()

        }
    }

    fun showResizeFrame(id: String){
        if (mResizeFrame.containsKey(id)){
            val resizeFrame = mResizeFrame[id]

            val animator = ObjectAnimator.ofFloat(resizeFrame, "alpha", 0f, 1f)
            animator.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {

                }
                override fun onAnimationEnd(animation: Animator?) {
                    if (resizeFrame!!.visibility != View.VISIBLE){
                        resizeFrame.visibility = View.VISIBLE
                        isShow = true
                    }
                }
                override fun onAnimationCancel(animation: Animator?) {

                }
                override fun onAnimationStart(animation: Animator?) {

                }
            })
            animator.setDuration(400).start()
        }

    }

    private lateinit var resizeWidget: ResizeWidgetListener

    fun setLocationListener(resizeWidget: ResizeWidgetListener){
        this.resizeWidget = resizeWidget
    }

}