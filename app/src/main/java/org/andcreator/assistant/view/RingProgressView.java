package org.andcreator.assistant.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import org.andcreator.assistant.R;

public class RingProgressView extends View {

    // 圆环的颜色
    private int ringColor = 0xFF00FF00;
    // 圆环进度的颜色
    private int ringProgressColor = 0xFFFF0000;
    //圆环的宽度
    private int ringWidth = 50;
    // 当前进度
    private float currentProgress = 0;
    // 最大进度
    private int maxProgress = 100;
    // 得到控件的宽度
    private int width;
    // 得到控件的高度
    private int height;
    // 画笔对象
    private Paint paint;
    // 上下文
    private Context context;

    // 默认的构造方法，一般取这3个就够用了
    public RingProgressView(Context context) {
        this(context, null);
    }

    public RingProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RingProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        // 得到自定义资源数组
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingProgressView);
        ringColor = typedArray.getColor(R.styleable.RingProgressView_ringColor, ringColor);
        ringProgressColor = typedArray.getColor(R.styleable.RingProgressView_ringProgressColor, ringProgressColor);
        ringWidth = (int) typedArray.getDimension(R.styleable.RingProgressView_ringWidth, dip2px(10));
        currentProgress = typedArray.getInt(R.styleable.RingProgressView_currentProgress, (int) currentProgress);
        maxProgress = typedArray.getColor(R.styleable.RingProgressView_maxProgress, maxProgress);
        typedArray.recycle();

        paint = new Paint();
        // 抗锯齿
        paint.setAntiAlias(true);
    }


    // 测量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();

        setMeasuredDimension(width,width);
    }

    // 绘制
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float center;

        // 1. 计算圆心坐标及半径
        if (width>height){
            center = width;
        }else {
            center = height;
        }

        float radius = (center - ringWidth) / 2;


        // 2. 画圆环
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(ringWidth);
        paint.setColor(ringColor);
        canvas.drawCircle(center/2, center/2, radius, paint);


        // 3. 画圆弧
        RectF rectF = new RectF(ringWidth/2, ringWidth/2, center-(ringWidth/2), center-(ringWidth/2));
        paint.setColor(ringProgressColor);
        canvas.drawArc(rectF, -90, currentProgress * 360 / maxProgress, false, paint);

    }

    // 布局
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * 把dp转换成px
     *
     * @param dipValue
     * @return
     */
    private int dip2px(int dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public float getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(float currentProgress) {
        this.currentProgress = currentProgress;
        invalidate();
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }
}