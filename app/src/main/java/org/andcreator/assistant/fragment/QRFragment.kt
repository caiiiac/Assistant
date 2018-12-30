package org.andcreator.assistant.fragment


import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.google.zxing.Result
import kotlinx.android.synthetic.main.fragment_qr.*
import liang.lollipop.lqr.LQR
import liang.lollipop.lqr.camera.CameraCallback
import liang.lollipop.lqr.decode.CaptureCallback
import liang.lollipop.lqr.decode.CaptureHandler

import org.andcreator.assistant.R
import org.andcreator.assistant.dialog.QRResultActivity
import org.andcreator.assistant.drawable.DialDrawable
import org.andcreator.assistant.skin.Skin
import org.andcreator.assistant.skin.SkinFragment
import org.andcreator.assistant.util.OtherUtils

/**
 * A simple [Fragment] subclass.
 *
 */
class QRFragment : SkinFragment(), SensorEventListener,
    CameraCallback,
    CaptureCallback {

    companion object {
        private const val REQUEST_SHOW = 88
    }

    /**
     * 传感器管理类
     */
    private var mSensorManager: SensorManager? = null
    /**
     * 地磁场传感器
     */
    private var accelerometer: Sensor? = null
    /**
     * 地磁场传感器
     */
    private var magnetic: Sensor? = null
    /**
     * 表盘Drawable
     */
    private val dialDrawable  = DialDrawable()
    /**
     * 加速度
     */
    private var accelerometerValues = FloatArray(3)
    /**
     * 地磁场
     */
    private var magneticFieldValues = FloatArray(3)
    /**
     *方向的实际值
     */
    private var orientationValues = FloatArray(3)
    /**
     * 方向的矩阵数据
     */
    private var orientationR = FloatArray(9)
    /**
     * 老版的定位API
     */
    private var oldOrientation: Sensor? = null
    /**
     * 模式
     */
    private var isNewModel = true
    /**
     * 滤波
     */
    private var filter =  FilterUtil()
    /**
     * 揭示动画
     */
    private var radio = 0.toDouble()
    /**
     * 是否显示相机画面
     */
    private var isOn = false

    private var captureHandler: CaptureHandler? = null

    private var isFlashOpen = false

    override fun setContentView(): Int {
        return R.layout.fragment_qr
    }

    override fun lazyLoad() {
        captureHandler = LQR.capture(surface,this,finderView,this)
        initView()
    }

    override fun onSkinUpdate(skin: Skin) {

    }

    private fun initView(){
        dialDrawable.setTextColor(context!!.resources.getColor(R.color.colorAccent))
        dialDrawable.setBgColor(Color.TRANSPARENT)
        dialDrawable.setScaleColor(context!!.resources.getColor(R.color.colorAccent))
        dialDrawable.setChinase(true)

        dial.setImageDrawable(dialDrawable)

        startScan.setOnClickListener {

            if (!isOn){
                captureHandler!!.onStart(activity!!)
                captureHandler!!.onResume()

            }else{
                captureHandler!!.onPause()
                captureHandler!!.onStop()
            }

            createAnimation(cameraLayout,startScan).start()

        }
        flashBtn.setOnClickListener {
            captureHandler!!.changeFlash(isFlashOpen)
            flashBtn.setImageResource(if(isFlashOpen){ R.drawable.ic_flash_off_white_24dp }else{ R.drawable.ic_flash_on_white_24dp })
            isFlashOpen = !isFlashOpen
        }
        finderView.setOnClickListener {
            captureHandler!!.requestFocus()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isOn){
            captureHandler!!.onStart(activity!!)
            captureHandler!!.onResume()
        }
    }

    override fun onStart() {
        super.onStart()
        registerSensorService()
        if (isOn)
            captureHandler!!.onStart(activity!!)
    }

    override fun onResume() {
        super.onResume()
        if (isOn)
            captureHandler!!.onResume()
    }

    override fun onStop() {
        super.onStop()
        if (mSensorManager != null){
            mSensorManager!!.unregisterListener(this)
        }
        if (captureHandler != null) {
            captureHandler!!.onStop()
        }
    }

    override fun onPause() {
        super.onPause()
        if (captureHandler != null){
            captureHandler!!.onPause()
        }
    }

    override fun onDestroy(){
        super.onDestroy()

        if (captureHandler != null) {
            captureHandler!!.onDestroy()
        }
    }

    /**
     * 注册传感器监听
     */
    private fun registerSensorService(){
        if (mSensorManager == null){
            mSensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }
        if (mSensorManager != null){

            if (accelerometer == null){
                accelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            }
            if (magnetic == null){
                magnetic = mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            }

            mSensorManager!!.registerListener(this,accelerometer, SensorManager.SENSOR_DELAY_GAME)
            mSensorManager!!.registerListener(this,magnetic, SensorManager.SENSOR_DELAY_GAME)

//            if (oldOrientation == null)
//                oldOrientation = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION)
//
//            mSensorManager!!.registerListener(this,oldOrientation, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    /**
     * 传感器数据回调
     */
    override fun onSensorChanged(event: SensorEvent?) {
        when(event!!.sensor.type){
            Sensor.TYPE_ACCELEROMETER ->{
                accelerometerValues = event.values
            }
            Sensor.TYPE_MAGNETIC_FIELD ->{
                magneticFieldValues = event.values
            }
//            Sensor.TYPE_ORIENTATION ->{
//                orientationValues = event.values
//            }
        }
        if (isNewModel){

            SensorManager.getRotationMatrix(
            orientationR, null, accelerometerValues,
            magneticFieldValues)

            SensorManager.getOrientation(orientationR, orientationValues)
            orientationValues[0] = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
            orientationValues[1] = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
            orientationValues[2] = Math.toDegrees(orientationValues[2].toDouble()).toFloat()
            orientationValues[2] *= -1f

        }

        if (orientationValues[0] > 180) {
            //有些计量方式是360有些是180
            orientationValues[0] = orientationValues[0] - 360
        }

        updateOrientation(filter.filter( orientationValues[0]))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun updateOrientation(angle: Float){
        dial.rotation = -(angle)
        degree.text = (-angle.toInt()).toString()+"°"
    }

    /**
     * 滤波
     */
    class FilterUtil(private val calculateCount: Int = 20) {

        private val lastPointArray = FloatArray(calculateCount)

        private var arraySize = 0

        fun filter(value: Float): Float {

            val newValue = value + 180F

            push(lastPointArray, newValue)
            if (arraySize < calculateCount) {
                arraySize++
                return newValue
            }
            var result = process(lastPointArray) - 180
            while (Math.abs(result) > 180) {
                if (result < 0) {
                    result += 360
                } else {
                    result -= 360
                }
            }
            return result
        }

        private fun push(array: FloatArray, value: Float) {
            for (index in 1 until array.size) {
                array[index-1] = array[index]
            }
            var newValue = value
            val def = array[array.size-1] - value
            if (def > 180) {
                newValue += 360
            } else if (def < -180) {
                newValue -= 360
            }
            array[array.size-1] = newValue
        }

        private fun process(array: FloatArray): Float{
            if (array.isEmpty()) {
                return 0F
            }
            if (array.size < 2) {
                return array[0]
            }
            return mean(array)
        }

        private fun mean(array: FloatArray): Float {
            var sum = 0F
            var allMoreThan = true
            var allLessThan = true
            for (i in array) {
                if (i > -360) {
                    allLessThan = false
                }
                if (i < 360) {
                    allMoreThan = false
                }
                sum += i
            }
            if (allLessThan) {
                for (i in 0 until array.size) {
                    array[i] += 360F
                }
            } else if (allMoreThan) {
                for (i in 0 until array.size) {
                    array[i] -= 360F
                }
            }
            return sum / array.size
        }
    }

    private fun createAnimation(v: View,local: View): Animator{

        if (radio == 0.toDouble()){
            radio = Math.sqrt(Math.pow(v.width.toDouble(), 2.0) + Math.pow(v.height.toDouble(), 2.0))
        }

        val animator: Animator = if (isOn) {
            ViewAnimationUtils.createCircularReveal(
                v,// 操作的视图
                local.x.toInt()+(local.width/2),// 动画开始的中心点X
                local.y.toInt()+(local.height/2),// 动画开始的中心点Y
                radio.toFloat(),// 动画开始半径
                0f)// 动画结束半径
        } else {

            ViewAnimationUtils.createCircularReveal(
                v,// 操作的视图
                local.x.toInt()+(local.width/2),// 动画开始的中心点X
                local.y.toInt()+(local.height/2),// 动画开始的中心点Y
                0f,// 动画开始半径
                radio.toFloat())// 动画结束半径
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                if (isOn) {
                    v.visibility = View.INVISIBLE
                }else{
                    val animator = ObjectAnimator.ofFloat(cover, "alpha", 1f, 0f)
                    animator.duration = 800
                    animator.interpolator = AccelerateInterpolator()
                    animator.addListener(object : Animator.AnimatorListener{
                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            cover.visibility = View.INVISIBLE
                        }

                    })
                    animator.start()
                }
                isOn = !isOn

            }

            override fun onAnimationStart(animation: Animator?) {
                if (!isOn) {
                    cover.alpha = 1f
                    cover.visibility = View.VISIBLE
                    v.visibility = View.VISIBLE
                }
            }
        })

        animator.interpolator = DecelerateInterpolator()
        animator.duration = 500
        return animator

    }


    override fun onCameraOpen() {

    }

    override fun onCameraError(exception: Exception?) {

    }

    override fun onCameraOpening() {

    }

    override fun selectCamera(cameraIds: Array<String>): String {
        if(cameraIds.isEmpty()){
            return "0"
        }
        return cameraIds[0]
    }

    override fun onSuccess(result: Result, barcode: Bitmap?, scaleFactor: Float) {
        /*
        if(activity!!.intent.action == "com.google.zxing.client.android.SCAN"){
            val resultIntent = Intent()
            resultIntent.putExtra("SCAN_RESULT",result.text)
            resultIntent.putExtra("SCAN_RESULT_FORMAT",result.barcodeFormat.toString())
            if(barcode != null){
                resultIntent.putExtra("SCAN_RESULT_BYTES", OtherUtils.Bitmap2Bytes(barcode))
            }
            resultIntent.putExtra("SCAN_RESULT_ORIENTATION",finderView.getRotetion())
            resultIntent.putExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL","")
            activity!!.setResult(Activity.RESULT_OK,resultIntent)
            activity!!.onBackPressed()
        }else{
            val newIntent = Intent(context, QRResultActivity::class.java)
            newIntent.putExtra(QRResultActivity.ARG_IMAGE_ROTETION,finderView.getRotetion())
            if(barcode != null){
                newIntent.putExtra(QRResultActivity.ARG_IMAGE_DATA, OtherUtils.Bitmap2Bytes(barcode))
            }
            newIntent.putExtra(QRResultActivity.ARG_TEXT_VALUE,result.text)
            startActivityForResult(newIntent, REQUEST_SHOW)
            captureHandler.onStop()
        }*/
        val newIntent = Intent(context, QRResultActivity::class.java)
        newIntent.putExtra(QRResultActivity.ARG_IMAGE_ROTETION,finderView.getRotetion())
        if(barcode != null){
            newIntent.putExtra(QRResultActivity.ARG_IMAGE_DATA, OtherUtils.Bitmap2Bytes(barcode))
        }
        newIntent.putExtra(QRResultActivity.ARG_TEXT_VALUE,result.text)
        startActivityForResult(newIntent, REQUEST_SHOW)
        captureHandler!!.onStop()
    }

}