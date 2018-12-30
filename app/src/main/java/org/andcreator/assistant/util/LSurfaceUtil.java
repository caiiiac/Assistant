package org.andcreator.assistant.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.*;

import java.util.Arrays;

/**
 * Created by Lollipop on 2017/08/15.
 * SurfaceView的集成管理类
 */
public class LSurfaceUtil implements TextureView.SurfaceTextureListener {

    //绘制线程
    private DrawThread drawThread;
    //绘制回调
    private DrawCallBack drawCallBack;
    //自行运行
    private boolean autoRunning = false;
    //绘制的View
    private TextureView textureView;
    //TextureView的操作类
    private SurfaceTexture surfaceTexture;

    //关联摄像头
    private boolean withCamera = false;
    //摄像头管理器
    private CameraManager cameraManager;
    //摄像头的ID
    private String[] cameraIdList;
    //摄像头驱动对象
    private CameraDevice cameraDevice;
    //预览，拍照用的Hanlder
    private Handler captureHandler;
    //拍照的回调函数
    private CameraCaptureSession.CaptureCallback captureCallback;
    //输出用的ImageReader
    private ImageReader outpotImageReader;
    //相机开启的回调函数
    private OnCameraOpenedCallback cameraOpenedCallback;
    //用于恢复摄像头的bean
    private ResumeCameraBean resumeCameraBean;
    //是否已经停止过了
    private boolean isStop = false;
    //画布是否已经完成初始化
    private boolean isSurfaceCreate = false;
    //预览的Surface
    private Surface previewSurface;
    //是否开启过相机
    private boolean isCameraOpened = false;

    //手机方向
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    public static LSurfaceUtil withSurface(SurfaceView view, DrawCallBack drawCallBack) {
        return new LSurfaceUtil(view, drawCallBack);
    }

    public static LSurfaceUtil withSurface(SurfaceView view) {
        return new LSurfaceUtil(view);
    }

    public static LSurfaceUtil withTexture(TextureView view) {
        return new LSurfaceUtil(view);
    }

    public void withCamera2(Context context) throws CameraAccessException {
        //http://blog.csdn.net/vinicolor/article/details/50992692
        //http://www.jianshu.com/p/7f766eb2f4e7
        withCamera = true;

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        cameraIdList = cameraManager.getCameraIdList();
    }

    private void openCamera(ResumeCameraBean resumeCameraBean) throws CameraAccessException {
        if (resumeCameraBean == null)
            return;
        if (!isStop)
            return;
        //如果以前的摄像头驱动不为空，那么就释放并且置空
        closeCamera();
        openCamera();
    }

    public void openCamera(Context context, String cameraID, Handler mainHandler, WindowManager windowManager) throws CameraAccessException {
        if (captureHandler == null) {
            //启动一个Handler线程
            HandlerThread handlerThread = new HandlerThread("Camera2");
            handlerThread.start();
            captureHandler = new Handler(handlerThread.getLooper());
        }

        if (!withCamera || cameraManager == null) {
            throw new RuntimeException("You will need to withCamera2(Context context)");
        }

        isCameraOpened = true;

        //如果以前的摄像头驱动不为空，那么就释放并且置空
        closeCamera();

        //保存上一次打开的请求数据，用以下一次的重新开启
        if (resumeCameraBean == null)
            resumeCameraBean = new ResumeCameraBean();
        resumeCameraBean.context = context;
        resumeCameraBean.cameraID = cameraID;
        resumeCameraBean.mainHandler = mainHandler;
        resumeCameraBean.windowManager = windowManager;
        openCamera();
    }

    private void openCamera() throws CameraAccessException {
        if (!isSurfaceCreate)
            return;
        if (!withCamera)
            return;

        calibrationCameraDirection(resumeCameraBean.windowManager);

        previewSurface = new Surface(surfaceTexture);

        if (outpotImageReader != null) {
            if (ActivityCompat.checkSelfPermission(resumeCameraBean.context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraManager.openCamera(resumeCameraBean.cameraID, new CameraStateCallback(captureHandler,
                captureCallback, previewSurface, cameraOpenedCallback,
                previewSurface, outpotImageReader.getSurface()), resumeCameraBean.mainHandler);
        }else{
            cameraManager.openCamera(resumeCameraBean.cameraID,new CameraStateCallback(captureHandler,
                    captureCallback,previewSurface,cameraOpenedCallback,previewSurface),resumeCameraBean.mainHandler);
        }

    }

    private Size getMaxSize(int totalRotation, Size[] sizes, int width, int height){
        boolean swapRotation = totalRotation == 90 || totalRotation == 270;
        if(swapRotation){
            int a = width;
            width = height;
            height = a;
        }
        float weight = getWeight(width,height);
        Log.e("getMaxSize","SrcWeight:"+weight+",width:"+width+",height"+height);
        Size output = null;
        if(sizes==null)
            return null;
        float lastWeight = 0;
        int lastPixels = 0;
        Log.e("getMaxSize","SizeLength:"+sizes.length);
        for(Size size : sizes){
            Log.e("getMaxSize","Size:"+size.toString());
            float newWeight = getWeight(size.getWidth(),size.getHeight());
            int newPixels = size.getHeight()*size.getWidth();
            if(Math.abs(newWeight-weight)<Math.abs(lastWeight-weight)){//如果比例越接近，那么就优先选择
                output = size;
                lastWeight = newWeight;
            }else if(Math.abs(newWeight-weight)==Math.abs(lastWeight-weight)){//如果比例相等，那么就对比分辨率
                if(lastPixels<newPixels){
                    output = size;
                    lastPixels = newPixels;
                }
            }
        }
        return output;
    }

    private float getWeight(int width,int height){
        return 1.0f*width/height;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surfaceTexture = surface;
        isSurfaceCreate = true;
        if(autoRunning&&drawThread!=null){
            drawThread.start();
        }
        try {
            openCamera();
        } catch (CameraAccessException e) {
            if(cameraOpenedCallback!=null)
                cameraOpenedCallback.onCameraError(e);
            else
                throw new RuntimeException(getCodeError(e.getReason()),e);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        surfaceTexture = surface;
        isSurfaceCreate = true;
        if(drawCallBack!=null)
            drawCallBack.onBoundsChange(width,height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if(surfaceTexture!=surface&&surfaceTexture!=null){
            surfaceTexture.release();
        }
        isSurfaceCreate = false;
        if(autoRunning&&drawThread!=null){
            drawThread.stop();
        }
        surfaceTexture = null;
        surface.release();
        closeCamera();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        surfaceTexture = surface;
    }

    //摄像头状态变化的回调函数
    private class CameraStateCallback extends CameraDevice.StateCallback{

        private Handler captureHandler;
        private CameraCaptureSession.CaptureCallback captureCallback;
        private Surface preview;
        private Surface[] outPut;
        private OnCameraOpenedCallback cameraOpenedCallback;

        public CameraStateCallback(Handler captureHandler,
                                   CameraCaptureSession.CaptureCallback captureCallback,
                                   Surface preview,
                                   OnCameraOpenedCallback cameraOpenedCallback,
                                   Surface... outPut) {
            this.captureHandler = captureHandler;
            this.captureCallback = captureCallback;
            this.preview = preview;
            this.outPut = outPut;
            this.cameraOpenedCallback = cameraOpenedCallback;
        }

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            if(cameraDevice!=null){
                cameraDevice.close();
                cameraDevice = null;
            }
            cameraDevice = camera;
            try {
                CaptureStateCallback captureStateCallback =
                        takePreview(cameraDevice,captureHandler,captureCallback,preview,outPut);
                if(cameraOpenedCallback!=null)
                    cameraOpenedCallback.onCameraOpened(captureStateCallback);
            } catch (CameraAccessException e) {
                if(cameraOpenedCallback!=null)
                    cameraOpenedCallback.onCameraError(e);
                else
                    throw new RuntimeException(getCodeError(e.getReason()),e);
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if(cameraDevice!=null){
                cameraDevice.close();
                cameraDevice = null;
            }
            if(camera!=null){
                camera.close();
            }
            if(cameraOpenedCallback!=null)
                cameraOpenedCallback.onCameraDisconnected();
        }

        /**
         *
         * @param camera
         * @param error @IntDef(value = {
         *      CameraDevice.StateCallback.ERROR_CAMERA_IN_USE
         *      CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE
         *      CameraDevice.StateCallback.ERROR_CAMERA_DISABLED
         *      CameraDevice.StateCallback.ERROR_CAMERA_DEVICE
         *      CameraDevice.StateCallback.ERROR_CAMERA_SERVICE
         * })
         */
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            if(cameraOpenedCallback!=null)
                cameraOpenedCallback.onCameraError(new CameraAccessException(error,"CameraDevice onError,ErrorCode:"+String.valueOf(error)));
            else
                throw new RuntimeException("CameraDevice onError,"+getCodeError(error));
        }
    }

    public interface OnCameraOpenedCallback{
        void onCameraOpened(CaptureStateCallback captureStateCallback);
        void onCameraError(CameraAccessException e);
        void onCameraDisconnected();
    }

    //预览拍照的状态回调
    private class CaptureStateCallback extends CameraCaptureSession.StateCallback{

        private CaptureRequest.Builder previewRequestBuilder;

        private CameraCaptureSession captureSession;

        private Handler captureHandler;

        private CameraCaptureSession.CaptureCallback listener;

        CaptureStateCallback(CaptureRequest.Builder previewRequestBuilder, Handler captureHandler, CameraCaptureSession.CaptureCallback listener) {
            this.previewRequestBuilder = previewRequestBuilder;
            this.captureHandler = captureHandler;
            this.listener = listener;
        }

        public void setListener(CameraCaptureSession.CaptureCallback listener) {
            this.listener = listener;
        }

        public void requestAutoAF(){
            request(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        }

        public void requestOpenFlash(){
            request(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_TORCH);
        }

        public void requestOffFlash(){
            request(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_OFF);
        }

        public void requestRedEye(){
            request(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE);
        }

        public void request(CaptureRequest.Key key, Integer integer){
            previewRequestBuilder.set(key,integer);
            setRepeatingRequest();
        }

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session){
            //一切就绪时
            if(captureSession!=session)
                captureSession = session;

            //执行一遍预先设置的参数
            setRepeatingRequest();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            //出现异常时
            if(captureSession!=null)
                captureSession.close();
            if(captureSession!=session)
                session.close();
        }

        private void setRepeatingRequest(){
            if(captureSession==null)
                return;
            //执行一遍预先设置的参数
            CaptureRequest request = previewRequestBuilder.build();
            try {
                captureSession.setRepeatingRequest(request,listener,captureHandler);
            } catch (CameraAccessException e) {
                throw new RuntimeException(getCodeError(e.getReason()),e);
            }
        }

    }

    //开启预览
    private CaptureStateCallback takePreview(CameraDevice device, Handler captureHandler,
                                             CameraCaptureSession.CaptureCallback captureCallback,
                                             Surface preview, Surface... outPut)throws CameraAccessException {
        if(device==null)
            throw new RuntimeException("takePreview Error,CameraDevice is null");
        // 创建预览需要的CaptureRequest.Builder
        CaptureRequest.Builder previewRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        // 将SurfaceView的surface作为CaptureRequest.Builder的目标
        previewRequestBuilder.addTarget(preview);
        // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
        CaptureStateCallback captureStateCallback = new CaptureStateCallback(previewRequestBuilder,captureHandler,captureCallback);
        device.createCaptureSession(Arrays.asList(outPut),captureStateCallback,captureHandler);
        return captureStateCallback;
    }

    public void onStop(){
        isStop = true;
        if(autoRunning){
            if(drawThread!=null){
                drawThread.stop();
            }
            drawThread = null;
        }
        if(withCamera){
            closeCamera();
        }
    }

    public void onStart() {
        if(!isStop)
            return;

        if(withCamera && isCameraOpened && resumeCameraBean!=null){
            try {
                openCamera(resumeCameraBean);
            } catch (CameraAccessException e) {
                throw new RuntimeException("onStart Error,restart Camera on Error."+getCodeError(e.getReason())+",msg:"+e.getMessage());
            }
        }
        isStop = false;
    }

    private void closeCamera(){
        //如果摄像头驱动不为空，那么就释放并且置空
        if(cameraDevice!=null){
            cameraDevice.close();
            cameraDevice = null;
        }
        if(previewSurface!=null){
            previewSurface.release();
            previewSurface = null;
        }
    }

    public void closeCameras(){
        isCameraOpened = false;
        closeCamera();
    }

    public void onDestroy(){
        closeCameras();
        cameraManager = null;
        isCameraOpened = false;

        if(previewSurface!=null){
            previewSurface.release();
            previewSurface = null;
        }

        if(textureView!=null){
            textureView.setSurfaceTextureListener(null);
            textureView = null;
        }

        if(surfaceTexture!=null){
            surfaceTexture.release();
            surfaceTexture = null;
        }

        if(drawThread!=null){
            drawThread.stop();
            drawThread = null;
        }

        drawCallBack = null;

        if(outpotImageReader!=null){
            outpotImageReader.close();
            outpotImageReader = null;
        }

        captureHandler = null;

    }

    private class ResumeCameraBean{
        public Context context;
        public String cameraID;
        public Handler mainHandler;
        public WindowManager windowManager;
    }

    private LSurfaceUtil(SurfaceView surfaceView) {
        this(surfaceView,null);
    }

    public LSurfaceUtil(SurfaceView surfaceView, DrawCallBack drawCallBack) {
        if(surfaceView==null)
            throw new RuntimeException("SurfaceView is NULL");

        this.drawCallBack = drawCallBack;
        this.autoRunning = drawCallBack != null;

    }

    public LSurfaceUtil(TextureView textureView) {
        if(textureView==null)
            throw new RuntimeException("TextureView is NULL");
        this.textureView = textureView;
        this.autoRunning = false;
        this.textureView.setSurfaceTextureListener(this);
        this.surfaceTexture = this.textureView.getSurfaceTexture();
    }


    private class DrawThread{
        private DrawRunnable runnable;
        private Thread thread;

        public DrawThread(DrawCallBack callBack, SurfaceHolder holder) {
            runnable = new DrawRunnable(callBack,holder);
            thread = new Thread(runnable);
        }

        public void start(){
            runnable.setRunning(true);
            thread.start();
        }

        public void stop(){
            try {
                runnable.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private class DrawRunnable implements Runnable {

        //是否真正运行
        private boolean isRunning = false;
        //绘制的回调函数
        private DrawCallBack callBack;
        //Surface的帮助类
        private SurfaceHolder holder;



        public DrawRunnable(DrawCallBack callBack, SurfaceHolder holder) {
            if(callBack==null)
                throw new RuntimeException("DrawCallBack is NULL");
            this.callBack = callBack;
            this.holder = holder;
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }

        @Override
        public void run() {
            Canvas canvas = null;
            while(isRunning){
                synchronized (holder){
                    try {
                        canvas = holder.lockCanvas(callBack.getBounds());
                        callBack.onDraw(canvas);
                        Thread.sleep(callBack.getDelay());
                    } catch (InterruptedException e) {
                        isRunning = false;
                        callBack.onError(e);
                    }finally {
                        if(canvas!=null)
                            holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    public static interface DrawCallBack{
        //绘制
        public void onDraw(Canvas canvas);
        //绘制的间隔时间
        public long getDelay();
        //设置画布刷新区域
        public Rect getBounds();
        //当边界变化了
        public void onBoundsChange(int width, int height);

        public void onError(Exception e);
    }

    public static abstract class SimpleDrawCallBack implements DrawCallBack{

        private Rect bounds;

        @Override
        public Rect getBounds() {
            if(bounds==null)
                bounds = new Rect(0,0,0,0);
            return bounds;
        }

        @Override
        public void onBoundsChange( int width, int height) {
            if(bounds==null)
                bounds = new Rect();
            bounds.set(0,0,width,height);
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
    }

    public void setCaptureCallback(CameraCaptureSession.CaptureCallback captureCallback) {
        this.captureCallback = captureCallback;
    }

    public void setOutpotImageReader(ImageReader outpotImageReader) {
        this.outpotImageReader = outpotImageReader;
    }

    public void setCameraOpenedCallback(OnCameraOpenedCallback cameraOpenedCallback) {
        this.cameraOpenedCallback = cameraOpenedCallback;
    }

    public String[] getCameraIdList() {
        return cameraIdList;
    }

    public CameraCharacteristics getCameraCharacteristics(String cameraId){
        try {
            if(cameraManager==null)
                return null;
            return cameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     *
     * @param windowManager
     */
    private void calibrationCameraDirection(WindowManager windowManager){
        calibrationCameraDirection(resumeCameraBean.cameraID,windowManager);
    }

    private void calibrationCameraDirection(String cameraId, WindowManager windowManager){
        int deviceOrientation = windowManager.getDefaultDisplay().getRotation();
        CameraCharacteristics cameraCharacteristics = getCameraCharacteristics(cameraId);

        int totalRotation = sensorToDeviceRotation(cameraCharacteristics,deviceOrientation);

        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
        Size maxSize = getMaxSize(totalRotation,sizes,textureView.getWidth(),textureView.getHeight());
        if(maxSize!=null){
            surfaceTexture.setDefaultBufferSize(maxSize.getWidth(),maxSize.getHeight());
            Log.e("getMaxSize","maxSize:"+maxSize.toString());

        }
    }

    private int sensorToDeviceRotation(CameraCharacteristics characteristics, int deviceOrientation) {
        if(characteristics==null)
            return 0;
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }


    public static String getCodeError(int code){
        switch (code){
            case CameraDevice.StateCallback.ERROR_CAMERA_IN_USE:
                return "相机已被使用，请先关闭其他相机应用";
            case CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE:
                return "相机无法打开，全部相机已被占用";
            case CameraDevice.StateCallback.ERROR_CAMERA_DISABLED:
                return "相机无法打开，因为设备政策限制";
            case CameraDevice.StateCallback.ERROR_CAMERA_DEVICE:
                return "相机打开失败，相机驱动遇到致命错误";
            case CameraDevice.StateCallback.ERROR_CAMERA_SERVICE:
                return "相机打开失败，相机服务遇到致命错误";
        }
        return "未记录的异常类型，Code:"+code;
    }

    public boolean isCameraOpened() {
        return isCameraOpened;
    }
}
