package org.andcreator.assistant.drawable;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Lollipop on 2017/07/28.
 * 表盘的绘制器
 */
public class DialDrawable extends Drawable {

    private Paint bgPaint;
    private Paint scalePaint;
    private Paint textPaint;
    private boolean isShowBitmap = false;

    private Rect bounds;

    private Point bitmapSize;

    private int radius = 0;

    private BitmapShader bitmapShader;

    private float textSize = 0;

    private boolean isChinase = false;

    private String orientationArray[] = {"N","E","S","W"};
    private String orientationZHArray[] = {"北","东","南","西"};

    private void initPaint(){
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setDither(true);
        bgPaint.setColor(Color.WHITE);

        scalePaint = new Paint();
        scalePaint.setAntiAlias(true);
        scalePaint.setDither(true);
        scalePaint.setColor(Color.GRAY);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setDither(true);
        textPaint.setColor(Color.GRAY);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.bounds = bounds;
        radius = (int) Math.min(bounds.width()/2.,bounds.height()/2);
        matrixBitmap();
        textSize = radius*0.125f;
        scalePaint.setStrokeWidth(textSize*0.1f);
        textPaint.setTextSize(textSize);
    }

    public DialDrawable() {
        initPaint();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        //绘制背景
        canvas.save();
        canvas.translate(bounds.centerX()-radius,bounds.centerY()-radius);
        canvas.drawCircle(radius,radius,radius,bgPaint);
        canvas.restore();

        //绘制刻度及文字
        Point startLoc = new Point(bounds.centerX(),bounds.centerY()-radius);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = -fm.descent + (fm.descent - fm.ascent) / 2;
        canvas.save();
        for(int i = 0;i<360;i++){
            if(i%90==0){
                canvas.drawLine(startLoc.x,startLoc.y,startLoc.x,startLoc.y+textSize,scalePaint);
                if(isChinase){
                    canvas.drawText(orientationZHArray[(i+1)/90],startLoc.x,startLoc.y+textSize*2+textY,textPaint);
                }else{
                    canvas.drawText(orientationArray[(i+1)/90],startLoc.x,startLoc.y+textSize*2+textY,textPaint);
                }
            }else if(i%45==0){
                canvas.drawLine(startLoc.x,startLoc.y,startLoc.x,startLoc.y+textSize*0.6f,scalePaint);
            }else if(i%10==0){
                canvas.drawLine(startLoc.x,startLoc.y,startLoc.x,startLoc.y+textSize*0.3f,scalePaint);
            }else{
                canvas.drawLine(startLoc.x,startLoc.y,startLoc.x,startLoc.y+textSize*0.15f,scalePaint);
            }
            canvas.rotate(1,bounds.centerX(),bounds.centerY());
        }
        canvas.restore();
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        bgPaint.setAlpha(alpha);
        scalePaint.setAlpha(alpha);
        textPaint.setAlpha(alpha);
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

    public void setScaleColor(int color){
        scalePaint.setColor(color);
        invalidateSelf();
    }

    public void setTextColor(int color){
        textPaint.setColor(color);
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


    public void setChinase(boolean chinase) {
        isChinase = chinase;
        invalidateSelf();
    }

    public void setTypeface(Typeface tf) {
        if(textPaint!=null)
            textPaint.setTypeface(tf);
        invalidateSelf();
    }

}
