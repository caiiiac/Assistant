package org.andcreator.assistant.layout

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import org.andcreator.assistant.R

class AppWidgetResizeFrame : FrameLayout {

    constructor(context: Context, widget: View, cell: CellLayout.Cell, dragLayer: DragLayer) : super(context) {
        init(context,widget,cell,dragLayer)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context,attributeSet: AttributeSet) : this(context)

    private lateinit var mContext: Context

    private var mBackgroundPadding: Int = 0
    private var mTouchTargetWidth: Int = 0
    private lateinit var mLeftHandle: ImageView
    private lateinit var mRightHandle: ImageView
    private lateinit var mTopHandle: ImageView
    private lateinit var mBottomHandle: ImageView
    private var mLeftBorderActive: Boolean = false
    private var mRightBorderActive: Boolean = false
    private var mTopBorderActive: Boolean = false
    private var mBottomBorderActive: Boolean = false

    private lateinit var mWidgetPadding: Rect
    private val mTmpPt = IntArray(2)

    private lateinit var mWidgetView: View
    private lateinit var mCellLayout: CellLayout.Cell
    private lateinit var mDragLayer: DragLayer

    private var mBaselineX: Int = 0
    private var mBaselineY: Int = 0
    private var mDeltaX: Int = 0
    private var mDeltaY: Int = 0

    private val DIMMED_HANDLE_ALPHA = 0f

    private var mTopTouchRegionAdjustment = 0
    private var mBottomTouchRegionAdjustment = 0

    private fun init(context: Context,widget: View,cell: CellLayout.Cell,dragLayer: DragLayer) {
        mCellLayout = cell
        mWidgetView = widget
        mDragLayer = dragLayer
        mContext = context

        setBackgroundResource(R.drawable.widget_resize_shadow)
        foreground = resources.getDrawable(R.drawable.widget_resize_frame)
        setPadding(0, 0, 0, 0)

        val handleMargin = resources.getDimensionPixelSize(R.dimen.widget_handle_margin)

        var lp: FrameLayout.LayoutParams

        mLeftHandle = ImageView(context)
        mLeftHandle.setImageResource(R.drawable.ic_widget_resize_handle)
        lp = FrameLayout.LayoutParams(
            82,82,
            Gravity.LEFT or Gravity.CENTER_VERTICAL
        )
        lp.leftMargin = handleMargin
        addView(mLeftHandle, lp)

        mRightHandle = ImageView(context)
        mRightHandle.setImageResource(R.drawable.ic_widget_resize_handle)
        lp = FrameLayout.LayoutParams(
            82,82,
            Gravity.RIGHT or Gravity.CENTER_VERTICAL
        )
        lp.rightMargin = handleMargin
        addView(mRightHandle, lp)

        mTopHandle = ImageView(context)
        mTopHandle.setImageResource(R.drawable.ic_widget_resize_handle)
        lp = FrameLayout.LayoutParams(
            82,82,
            Gravity.CENTER_HORIZONTAL or Gravity.TOP
        )
        lp.topMargin = handleMargin
        addView(mTopHandle, lp)

        mBottomHandle = ImageView(context)
        mBottomHandle.setImageResource(R.drawable.ic_widget_resize_handle)
        lp = FrameLayout.LayoutParams(
            82,82,
            Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        )
        lp.bottomMargin = handleMargin
        addView(mBottomHandle, lp)

        val r = context.resources
        val padding = r.getDimensionPixelSize(R.dimen.default_widget_padding)
        mWidgetPadding = Rect(padding, padding, padding, padding)

        mBackgroundPadding = resources
            .getDimensionPixelSize(R.dimen.resize_frame_background_padding)
        mTouchTargetWidth = 2 * mBackgroundPadding

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // 动态获取子View实例

        var i = 0
        val size = childCount
        while (i < size) {
            val view = getChildAt(i)
            // 放置子View，宽高都是100
            when(i){
                0 -> view.layout(mBackgroundPadding, (bottom-top)/2-mBackgroundPadding+50, mBackgroundPadding+50, (bottom-top)/2 - mBackgroundPadding+100)

                1 -> view.layout(right-left-mBackgroundPadding-50, (bottom-top)/2-mBackgroundPadding+50, right-left-mBackgroundPadding, (bottom-top)/2 - mBackgroundPadding+100)

                2 -> view.layout((right-left)/2-25, mBackgroundPadding, (right-left)/2+25, mBackgroundPadding + 50)

                3 -> view.layout((right-left)/2-25, bottom-top-mBackgroundPadding-50, (right-left)/2+25, bottom-top-mBackgroundPadding)
            }

            i++
        }
    }

    /**
     * 如果区域中的点，则开始调整大小
     * @param x
     * @param y
     * @return
     */
    fun beginResizeIfPointInRegion(x: Int, y: Int): Boolean {
        Log.e("参数","${x} and ${y}")

        val horizontalActive = true
        val verticalActive = true

        mLeftBorderActive = x < mTouchTargetWidth && horizontalActive
        mRightBorderActive = x > width - mTouchTargetWidth && horizontalActive
        mTopBorderActive = y < mTouchTargetWidth + mTopTouchRegionAdjustment && verticalActive
        mBottomBorderActive = y > height - mTouchTargetWidth + mBottomTouchRegionAdjustment && verticalActive

        val anyBordersActive = (mLeftBorderActive || mRightBorderActive
                || mTopBorderActive || mBottomBorderActive)

//        mBaselineWidth = measuredWidth
//        mBaselineHeight = measuredHeight
        mBaselineX = left
        mBaselineY = top

        if (anyBordersActive) {
            mLeftHandle.alpha = if (mLeftBorderActive) 1.0f else DIMMED_HANDLE_ALPHA
            mRightHandle.alpha = if (mRightBorderActive) 1.0f else DIMMED_HANDLE_ALPHA
            mTopHandle.alpha = if (mTopBorderActive) 1.0f else DIMMED_HANDLE_ALPHA
            mBottomHandle.alpha = if (mBottomBorderActive) 1.0f else DIMMED_HANDLE_ALPHA
        }
        return anyBordersActive
    }

    /**
     * Here we bound the deltas such that the frame cannot be stretched beyond the extents
     * of the CellLayout, and such that the frame's borders can't cross.
     * 在这里，我们绑定增量，使得框架不能超出范围 CellLayout，以及框架的边框不能交叉。
     */
    fun updateDeltas(deltaX: Int, deltaY: Int) {
        mDeltaX = deltaX
        mDeltaY = deltaY
    }

    /**
     * Based on the deltas, we resize the frame, and, if needed, we resize the widget.
     * 基于增量，我们调整框架的大小，如果需要，我们调整窗口小部件的大小。
     */
    fun visualizeResizeForDelta(deltaX: Int, deltaY: Int):Int {
        updateDeltas(deltaX, deltaY)
        val lp = layoutParams
//        Log.e("mBaselineWidth","${mBaselineWidth} xx ${mBaselineHeight}")
        if (mLeftBorderActive) {
//            Log.e("触摸的位置","左")
//            lp.width = mBaselineWidth - mDeltaX
//            lp.width = mBaselineWidth + deltaX
            layout(deltaX,mCellLayout.locationY-mBackgroundPadding,mWidgetView.layoutParams.width+mCellLayout.locationX+mBackgroundPadding,mWidgetView.layoutParams.height+mCellLayout.locationY+mBackgroundPadding)
            return 0
        } else if (mRightBorderActive) {
//            Log.e("触摸的位置","右")
//            lp.width = mBaselineWidth + mDeltaX
//            lp.width = deltaX
            layout(mCellLayout.locationX-mBackgroundPadding,mCellLayout.locationY-mBackgroundPadding,  deltaX,mWidgetView.layoutParams.height+mCellLayout.locationY+mBackgroundPadding)
            return 1
        }

        if (mTopBorderActive) {
//            Log.e("触摸的位置","上")
//            lp.height = mBaselineHeight - mDeltaY
//            lp.height = mBaselineHeight - deltaY
            layout(mCellLayout.locationX-mBackgroundPadding,deltaY,mWidgetView.layoutParams.width+mCellLayout.locationX+mBackgroundPadding,mWidgetView.layoutParams.height+mCellLayout.locationY+mBackgroundPadding)
            return 2
        } else if (mBottomBorderActive) {
//            Log.e("触摸的位置","下")
//            lp.height = mBaselineHeight + mDeltaY
//            lp.height = deltaY
            layout(mCellLayout.locationX-mBackgroundPadding,mCellLayout.locationY-mBackgroundPadding,mWidgetView.layoutParams.width+mCellLayout.locationX+mBackgroundPadding,deltaY)
            return 3
        }

        return -1
    }

    fun onTouchUp(){

        if (width<20 || height<20){
            snapToWidget(true)
        }

        ObjectAnimator.ofFloat(mLeftHandle, "alpha", 1.0f).start()
        ObjectAnimator.ofFloat(mRightHandle, "alpha", 1.0f).start()
        ObjectAnimator.ofFloat(mTopHandle, "alpha", 1.0f).start()
        ObjectAnimator.ofFloat(mBottomHandle, "alpha", 1.0f).start()
    }

    /**
     * 将自己移动到小部件的位置
     * @param animate
     */
     fun snapToWidget(animate: Boolean) {

        val lp = layoutParams

        val newWidth = (mCellLayout.cellWidth
                - mWidgetPadding.left - mWidgetPadding.right)
        val newHeight = (mCellLayout.cellHeight
                - mWidgetPadding.top - mWidgetPadding.bottom)

//        Log.e("宽高",mCellLayout.cellWidth.toString() + "   " + mCellLayout.cellHeight.toString())
        mTmpPt[0] = mWidgetView.left
        mTmpPt[1] = mWidgetView.top

        val newX = mTmpPt[0] - mBackgroundPadding + mWidgetPadding.left
        val newY = mTmpPt[1] - mBackgroundPadding + mWidgetPadding.top

        // We need to make sure the frame's touchable regions lie fully within the bounds of the
        // DragLayer. We allow the actual handles to be clipped, but we shift the touch regions
        // down accordingly to provide a proper touch target.
        //我们需要确保框架的可触摸区域完全位于框架的边界内
        // DragLayer 我们允许剪切实际手柄，但我们移动触摸区域
        //相应地向下提供适当的触摸目标。
        if (newY < 0) {
            // In this case we shift the touch region down to start at the top of the DragLayer
            //在这种情况下，我们将触摸区域向下移动到DragLayer顶部的开始
            mTopTouchRegionAdjustment = -newY
        } else {
            mTopTouchRegionAdjustment = 0
        }
        if (newY + newHeight > mDragLayer.height) {
            // In this case we shift the touch region up to end at the bottom of the DragLayer
            //在这种情况下，我们将触摸区域向上移动到DragLayer底部的末尾
            mBottomTouchRegionAdjustment = -(newY + newHeight - mDragLayer.height)
        } else {
            mBottomTouchRegionAdjustment = 0
        }

        if (!animate) run {

            lp.width = newWidth+mBackgroundPadding
            lp.height =newHeight+mBackgroundPadding

            mLeftHandle.alpha = 1.0f
            mRightHandle.alpha = 1.0f
            mTopHandle.alpha = 1.0f
            mBottomHandle.alpha = 1.0f
        }

//        Log.e("小部件的位置",mCellLayout.locationX.toString()+"  "+mCellLayout.locationY.toString())

        layout(mCellLayout.locationX-mBackgroundPadding,mCellLayout.locationY-mBackgroundPadding,mWidgetView.layoutParams.width+mCellLayout.locationX+mBackgroundPadding,mWidgetView.layoutParams.height+mCellLayout.locationY+mBackgroundPadding)

    }

}