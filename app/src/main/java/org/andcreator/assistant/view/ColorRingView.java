package org.andcreator.assistant.view;

import android.content.Context;
import android.graphics.*;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import org.andcreator.assistant.R;

public class ColorRingView extends View {

    private Context mContext;
    private float strokeWidth = 64;
    private int width;
    private int height;

    float progress = 0;
    Paint paintBg = new Paint();//背景笔刷
    Paint paintBgEnds = new Paint();//背景端点笔刷
    Paint paintProgress = new Paint();//进度笔刷
    Paint getPaintProgressEnds = new Paint();//进度端点笔刷
    int[] ringColors = new int[2];

    public ColorRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
//        setWillNotDraw(false);
        init();
    }

    private void init() {

        ringColors[0] = mContext.getResources().getColor(R.color.colorAccent);
        ringColors[1] = mContext.getResources().getColor(R.color.colorAccented);

        //白色背景弧线
        paintBg.setColor(mContext.getResources().getColor(R.color.transAccent));
        paintBg.setStrokeWidth(strokeWidth);
        paintBg.setStyle(Paint.Style.STROKE);
        paintBg.setAntiAlias(true);
        //白色背景两个端点
        paintBgEnds.setColor(ringColors[0]);
        paintBgEnds.setAntiAlias(true);
        //彩色进度弧线
        paintProgress.setColor(ringColors[0]);
        paintProgress.setStrokeWidth(strokeWidth);
        paintProgress.setStyle(Paint.Style.STROKE);
        paintProgress.setAntiAlias(true);
        //彩色进度端点
        getPaintProgressEnds.setColor(ringColors[0]);
        getPaintProgressEnds.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = width / 2;
        int centerY = height / 2;
        //白色背景左圆端点
        float startX = (float) (circleSize * Math.sin(180 * Math.PI / 180));
        float startY = (float) (circleSize * Math.cos(180 * Math.PI / 180));
        canvas.drawArc(rect, 0, 360, false, paintBg);
        //彩色进度左圆端点
        int currentLastColor = getCurrentColor(0 / 180, ringColors);
        paintBgEnds.setColor(currentLastColor);
        canvas.drawCircle(centerX + startX, centerY + startY, strokeWidth / 2f, paintBgEnds);
        //彩色进度右圆端点
        float currentX = (float) (circleSize * Math.sin((180 + 360 * (1 - progress)) * Math.PI / 180));
        float currentY = (float) (circleSize * Math.cos((180 + 360 * (1 - progress)) * Math.PI / 180));
        currentLastColor = getCurrentColor((360 - 359 *(1 - progress)) / 360, ringColors);
        paintBgEnds.setColor(currentLastColor);
        canvas.drawCircle(centerX + currentX, centerY + currentY, strokeWidth / 2f, paintBgEnds);
        //绘制彩色弧形进度
        paintProgress.setShader(new SweepGradient(width / 2f, height / 2f, ringColors, null));
        canvas.rotate(-90, width / 2f, height / 2);
        canvas.drawArc(rect, 0, 360 * progress, false, paintProgress);//进度
    }

    float circleSize = 0;
    RectF rect = new RectF();
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getMeasuredWidth()>getMeasuredHeight()){
            width = getMeasuredWidth();
            height = getMeasuredWidth();
        }else {
            width = getMeasuredHeight();
            height = getMeasuredHeight();
        }

        int size = Math.min(width, height);
        rect.left = strokeWidth / 2f;
        rect.top = strokeWidth / 2f;
        rect.right = size - strokeWidth / 2f;
        rect.bottom = size - strokeWidth / 2f;
        circleSize = (width - strokeWidth) / 2f;
        setMeasuredDimension(width, height);
    }

    public static int getCurrentColor(float percent, int[] colors) {
        float[][] f = new float[colors.length][3];
        for (int i = 0; i < colors.length; i++) {
            f[i][0] = (colors[i] & 0xff0000) >> 16;
            f[i][1] = (colors[i] & 0x00ff00) >> 8;
            f[i][2] = (colors[i] & 0x0000ff);
        }
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < f.length; j++) {
                if (f.length == 1 || percent == j / (f.length - 1f)) {
                    result = f[j];
                } else {
                    if (percent > j / (f.length - 1f) && percent < (j + 1f) / (f.length - 1)) {
                        result[i] = f[j][i] - (f[j][i] - f[j + 1][i]) * (percent - j / (f.length - 1f)) * (f.length - 1f);
                    }
                }
            }
        }
        return Color.rgb((int) result[0], (int) result[1], (int) result[2]);
    }

    public void setProgress(float progress){
        this.progress =progress;
        invalidate();
    }
}
