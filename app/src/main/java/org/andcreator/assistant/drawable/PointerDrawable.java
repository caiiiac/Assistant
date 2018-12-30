package org.andcreator.assistant.drawable;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by LiuJ on 2017/07/28.
 * 指针的绘制对象
 */
public class PointerDrawable extends Drawable {
    private Paint bgPaint;
    private Paint pointerPaint;

    private Rect bounds;

    private Point bitmapSize;

    private int radius = 0;

    private BitmapShader bitmapShader;

    private Path pointerPath;

    private boolean isShowBitmap = false;

    private void initPaint(){
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setDither(true);
        bgPaint.setColor(Color.TRANSPARENT);

        pointerPaint = new Paint();
        pointerPaint.setAntiAlias(true);
        pointerPaint.setDither(true);
        pointerPaint.setColor(Color.GRAY);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.bounds = bounds;
        radius = (int) Math.min(bounds.width()/2.,bounds.height()/2);
        matrixBitmap();
        pointerPaint.setStrokeWidth(radius*0.0125f);
        if(pointerPath==null)
            pointerPath = new Path();
        pointerPath.reset();
        pointerPath.moveTo(bounds.centerX(),bounds.centerY()-radius);
        pointerPath.lineTo(bounds.centerX()+radius*0.1f,bounds.centerY());
//        pointerPath.lineTo(bounds.centerX(),bounds.centerY()+radius);
        pointerPath.lineTo(bounds.centerX()-radius*0.1f,bounds.centerY());
        pointerPath.close();
    }

    public PointerDrawable() {
        initPaint();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        //绘制背景
        canvas.save();
        canvas.translate(bounds.centerX()-radius,bounds.centerY()-radius);
        canvas.drawCircle(radius,radius,radius,bgPaint);
        canvas.restore();
        //绘制前景
        pointerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(pointerPath, pointerPaint);
        pointerPaint.setStyle(Paint.Style.STROKE);
        canvas.save();
        canvas.rotate(180,bounds.centerX(),bounds.centerY());
        canvas.drawPath(pointerPath, pointerPaint);
        canvas.restore();
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        bgPaint.setAlpha(alpha);
        pointerPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setBgColor(int color){
        bgPaint.setColor(color);
        invalidateSelf();
    }

    public void setColor(int color){
        pointerPaint.setColor(color);
        invalidateSelf();
    }

    public void setBitmap(Bitmap bitmap){
        if(bitmap==null){
            bitmapShader = null;
            bgPaint.setShader(null);
            return;
        }
        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        if(isShowBitmap)
            bgPaint.setShader(bitmapShader);
        if(bitmapSize==null)
            bitmapSize = new Point();
        bitmapSize.set(bitmap.getWidth(),bitmap.getHeight());
        matrixBitmap();
        invalidateSelf();
    }

    public void setShowBitmap(boolean showBitmap) {
        if(isShowBitmap == showBitmap)
            return;
        isShowBitmap = showBitmap;
        bgPaint.setShader(isShowBitmap?bitmapShader:null);
        invalidateSelf();
    }

    /**
     * 图片尺寸矫正
     */
    private void matrixBitmap() {
        if(bounds==null || bitmapShader == null) return;
        //选择缩放比较多的缩放，这样图片就不会有图片拉伸失衡
        Matrix matrix = new Matrix();
        int d = radius*2;
        float scaleX = d / (float)bitmapSize.x;
        float scaleY = d / (float)bitmapSize.y;
//        float scale = scaleX > scaleY ? scaleX : scaleY;
        float scale = Math.max(scaleX , scaleY);
        matrix.postScale(scale,scale);
        bitmapShader.setLocalMatrix(matrix);
    }
}
