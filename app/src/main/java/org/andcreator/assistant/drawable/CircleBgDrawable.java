package org.andcreator.assistant.drawable;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/**
 * Created by Lollipop on 2016/12/8.
 * 圆形的背景drawavle
 */

public class CircleBgDrawable extends Drawable {
    private Paint paint;
    private Rect bounds;
    private RectF bound;
    private boolean biggestCorners = true;
    private int corners = 0;

    public CircleBgDrawable() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xFF00C3EA);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeJoin(Paint.Join.MITER);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(bound,corners,corners,paint);
    }

    @Override
    public void setAlpha(int i) {
        paint.setAlpha(i);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.bounds = bounds;
        this.bound = new RectF(bounds.left,bounds.top,bounds.right,bounds.bottom);
        if(biggestCorners){
            corners = Math.min(bounds.centerX(),bounds.centerY());
        }
    }

    public boolean isBiggestCorners() {
        return biggestCorners;
    }

    public void setBiggestCorners(boolean biggestCorners) {
        this.biggestCorners = biggestCorners;
    }

    public int getCorners() {
        return corners;
    }

    public void setCorners(int corners) {
        this.corners = corners;
        invalidateSelf();
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }
}
