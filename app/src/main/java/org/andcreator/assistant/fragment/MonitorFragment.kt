package org.andcreator.assistant.fragment


import android.annotation.SuppressLint
import android.content.*
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_monitor.*
import org.andcreator.assistant.*
import org.andcreator.assistant.constant.CPUConstant

import org.andcreator.assistant.skin.Skin
import org.andcreator.assistant.skin.SkinFragment
import org.andcreator.assistant.util.*
import org.andcreator.assistant.view.PolylineView
import java.math.BigDecimal


/**
 * A simple [Fragment] subclass.
 *
 */
class MonitorFragment : SkinFragment() {

    /**
     * 服务
     */
    private var mServiceIf: IUsageUpdateService? = null

    /**
     * CPU核心数
     */
    private var coreCount = 0

    /**
     * 是否可以获取CPU温度
     */
    private var isCPUTemperature = true

    /**
     * 表示服务可用
     */
    private var mIsForeground = false

    /**
     * 传感器管理类
     */
    private var mSensorManager: SensorManager? = null

    private var mSensor: Sensor? = null

    /**
     * 传感器获取的温度
     */
    private var temperatureValue = 0f

    /**
     * 电池温度
     */
    private var batteryTemperature = 0f

    /**
     * 界面上的控件
     */
    private lateinit var layouts: Array<RelativeLayout>
    private lateinit var  polylines: Array<PolylineView>
    private lateinit var  messages: Array<TextView>
    private lateinit var  usages: Array<TextView>

    private var isBindService = false

    /**
     * 异步处理数据
     * handler刷新界面
     */
    private lateinit var calculateThread: Thread
    private var mHandler= @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg!!.what){
                1 -> {
                    val bundle = msg.data
                    val usagesText = bundle.getString("usages")
                    val position = bundle.getInt("position")
                    val polyline = bundle.getFloat("polylines")
                    val messagesText = bundle.getString("messages")

                    val coreView = layouts[position]
                    if (coreView.visibility == View.GONE){
                        coreView.visibility = View.VISIBLE
                    }
                    usages[position].text = usagesText
                    polylines[position].update(polyline)
                    messages[position].text =messagesText
                }
                2 ->{
                    val usage = msg.obj as Float
                    cpuAverage.setProgress(usage)
                    cpuAverageNum.text = BigDecimal((usage*100).toDouble()).setScale(1,BigDecimal.ROUND_HALF_UP).toFloat().toString()
                }
            }
        }
    }

    // IUsageUpdateCallback回调updateUsage
    private val mCallback = object : IUsageUpdateCallback.Stub() {
        /**
         * 注意从服务接收方法
         */
        @Throws(RemoteException::class)
        override fun updateUsage(
            cpuUsages: IntArray,
            freqs: IntArray, minFreqs: IntArray, maxFreqs: IntArray
        ) {

            mHandler.post {

                if (mIsForeground) {

                    coreCount = CpuInfoCollector.calcCpuCoreCount()

                    // 更新CPU使用率
                    showCpuUsages(cpuUsages, freqs, minFreqs, maxFreqs)
                }
            }
        }
    }

    //服务连接
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {

            // 获取服务
            mServiceIf = IUsageUpdateService.Stub.asInterface(service)
            isBindService = true
            try {
                if (mServiceIf != null){
                    mServiceIf!!.registerCallback(mCallback)
                }
            } catch (e: RemoteException) {
                Log.e("RemoteException",e.message)
            }

        }

        override fun onServiceDisconnected(name: ComponentName) {
            mServiceIf = null
            Toast.makeText(context,"mServiceIfNUll",Toast.LENGTH_SHORT).show()
        }
    }

    override fun setContentView(): Int {
        return R.layout.fragment_monitor
    }

    override fun lazyLoad() {
        TypefaceUtil.replaceFont(contentView, "fonts/ProductSans.ttf")
        initView()
    }

    override fun onSkinUpdate(skin: Skin) {

    }

    private fun initView() {

        layouts = arrayOf(cpu1,cpu2,cpu3,cpu4,cpu5,cpu6,cpu7,cpu8)
        polylines = arrayOf(polyline1,polyline2,polyline3,polyline4,polyline5,polyline6,polyline7,polyline8)
        messages = arrayOf(cpu1Message,cpu2Message,cpu3Message,cpu4Message,cpu5Message,cpu6Message,cpu7Message,cpu8Message)
        usages = arrayOf(cpu1Usages,cpu2Usages,cpu3Usages,cpu4Usages,cpu5Usages,cpu6Usages,cpu7Usages,cpu8Usages)

        activity!!.registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // 绑定到服务已开始
        val intent = Intent(IUsageUpdateService::class.java.name)
        intent.setPackage(AssistantApplication.context.packageName)
        AssistantApplication.context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStart() {
        super.onStart()
        if (ThermalInfoUtil.getCpuTemperature() == 0f){
            isCPUTemperature = false
            startSensorsTemperature()
        }
    }

    override fun onDestroy() {
        cleanupServiceConnection()
        if (mServiceIf != null) {
            mServiceIf!!.stopResident()
        }
        super.onDestroy()
    }

    private fun cleanupServiceConnection() {

        // 发送注销服务回调
        if (mServiceIf != null) {
            try {
                mServiceIf!!.unregisterCallback(mCallback)
            } catch (e: RemoteException) {
                Log.e("RemoteException",e.message)
            }

        }

        if (isBindService){
            // bind解除
            AssistantApplication.context.unbindService(mServiceConnection)
            activity!!.unregisterReceiver(mBatInfoReceiver)
        }
    }

    override fun onPause() {
        super.onPause()
        mIsForeground = false
        unregister()
    }

    override fun onResume() {
        super.onResume()
        mIsForeground = true
    }

    /**
     * 获取CPU温度
     */
    private fun showCpuTemperature(){
        val cpuTemperature = ThermalInfoUtil.getCpuTemperature()
        cpuTempNum.text = cpuTemperature.toString()+"°"
        cpuTemp.setProgress(cpuTemperature/100f)
    }

    /**
     * 获取CPU温度
     */
    private fun startSensorsTemperature(){
        /*获取系统服务（SENSOR_SERVICE）返回一个SensorManager对象*/
        mSensorManager = context!!.getSystemService(SENSOR_SERVICE) as SensorManager

        if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null){
            /*通过SensorManager获取相应的（温度传感器）Sensor类型对象*/
            mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
            mSensorManager!!.registerListener(mSensorEventListener, mSensor, 2000000)
        }else if (mSensorManager!!.getDefaultSensor(CPUConstant.BMP280Temperature) != null){
            mSensor = mSensorManager!!.getDefaultSensor(CPUConstant.BMP280Temperature)
            mSensorManager!!.registerListener(mSensorEventListener, mSensor, 2000000)
        }
    }

    /*声明一个SensorEventListener对象用于侦听Sensor事件，并重载onSensorChanged方法*/
    private val mSensorEventListener = object : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                temperatureValue = event.values[0]    // 利用这些数据执行一些工作
            }else if (event.sensor.type == CPUConstant.BMP280Temperature){
                temperatureValue = event.values[0]    // 利用这些数据执行一些工作
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // TODO Auto-generated method stub

        }
    }

    private fun unregister() {
        if (mSensorManager != null){

            mSensorManager!!.unregisterListener(mSensorEventListener, mSensor)
        }
    }

    /**
     *更新CPU使用率
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @SuppressLint("SetTextI18n")
    private fun showCpuUsages(cpuUsages: IntArray, freqs: IntArray, minFreqs: IntArray, maxFreqs: IntArray){

        calculateThread = object : Thread() {
            override fun run() {
                super.run()

                var cpuAll = 0

                for(i in 0..7){

                    if (i >= coreCount) {
                        break
                    }

                    val bundle = Bundle()
                    var cpuUsage = 0
                    if (cpuUsages.size > i + 1) {
                        cpuUsage = cpuUsages[i + 1]
                    }

                    val usagesText = "CPU" + (i + 1) + ": " + cpuUsage + "%"

                    cpuAll += cpuUsage

                    // 频率最小值/最大值
                    run {
                        val minFreqText = CpuUtil.formatFreq(minFreqs[i])
                        val maxFreqText = CpuUtil.formatFreq(maxFreqs[i])
                        val messagesText = "$minFreqText - $maxFreqText"
                        bundle.putString("messages",messagesText)
                    }

                    bundle.putFloat("polylines",cpuUsage/100f)

                    val msg = Message()
                    msg.what = 1
                    bundle.putInt("position",i)
                    bundle.putString("usages",usagesText)
                    msg.data = bundle
                    mHandler.sendMessage(msg)

                }
                if (cpuAll != 0){
                    val msg = Message()
                    msg.what = 2
                    msg.obj = cpuAll/coreCount/100f
                    mHandler.sendMessage(msg)
                    Log.e("coreCount coreCount",coreCount.toString())
                }
            }
        }

        calculateThread.start()

        if (isCPUTemperature){
            showCpuTemperature()
        }else{
            cpuTemp.setProgress(temperatureValue/100f)
            cpuTempNum.text = BigDecimal(temperatureValue.toDouble()).setScale(1,BigDecimal.ROUND_HALF_UP).toFloat().toString()+"°"
        }

        battery.setProgress(batteryTemperature/100f)
        batteryNum.text = batteryTemperature.toString()+"°"

        showRamInfo()

    }

    /**
     * 获取内存占用情况
     */
    private fun showRamInfo() {
        val weights = 100 / RAMInfo.getTotalMemory().toFloat()
        val armInfo =  Math.round(weights * (RAMInfo.getTotalMemory().toFloat() - RAMInfo.getAvailMemory(context))*10)/ 10f

        ram.setProgress(armInfo/100f)
        ramNum.text = armInfo.toString()
    }

    /**
     * 获取Battery温度
     */
    private val mBatInfoReceiver = object : BroadcastReceiver(){

        override fun onReceive(context: Context, intent: Intent){
            val action = intent.action

            if (Intent.ACTION_BATTERY_CHANGED == action){
                batteryTemperature = intent.getIntExtra("temperature", 0) / 10f

            }
        }
    }
}
