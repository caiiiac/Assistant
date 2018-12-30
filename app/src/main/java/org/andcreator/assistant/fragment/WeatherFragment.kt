package org.andcreator.assistant.fragment


import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_weather.*

import org.andcreator.assistant.R
import org.andcreator.assistant.activity.CityActivity
import org.andcreator.assistant.adapter.DayWeatherAdapter
import org.andcreator.assistant.bean.AssistantBean
import org.andcreator.assistant.bean.DayWeatherBean
import org.andcreator.assistant.dialog.ColorChangeDialog
import org.andcreator.assistant.skin.Skin
import org.andcreator.assistant.skin.SkinFragment
import org.andcreator.assistant.util.DatabaseUtil
import org.andcreator.assistant.util.OtherUtils
import org.andcreator.assistant.util.TypefaceUtil
import org.andcreator.assistant.util.WeatherUtil
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class WeatherFragment : SkinFragment() {

    private var dayWeatherData = ArrayList<DayWeatherBean>()
    private var hoursWeatherData = ArrayList<DayWeatherBean>()

    private lateinit var dayAdapter: DayWeatherAdapter
    private lateinit var hoursAdapter: DayWeatherAdapter

    private lateinit var weatherUtil: WeatherUtil

    private lateinit var editor: SharedPreferences.Editor
    private lateinit var preferences: SharedPreferences

    private lateinit var calculateThread: Thread

    private var weatherTodayDBUtil: DatabaseUtil.WeatherTodayDBOperate? = null

    private var weatherDayDBUtil: DatabaseUtil.WeatherDayDBOperate? = null


    private val todayData: ArrayList<AssistantBean> = ArrayList()

    private val dayData: ArrayList<AssistantBean> = ArrayList()

    companion object {
        const val CHOOSE_CITY = 0
    }

    private var mHandler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg!!.what){
                1 ->{

                    dayAdapter = DayWeatherAdapter(context!!,dayWeatherData)
                    dayWeather.adapter = dayAdapter

                    hoursAdapter = DayWeatherAdapter(context!!,hoursWeatherData)
                    hoursWeather.adapter = hoursAdapter

                    windSpeed.text = weatherUtil.todayWindSpeed
                    humidity.text = weatherUtil.todayHumidity
                    highest.text = weatherUtil.todayHighest
                    temp.text = weatherUtil.nowTemps
                    Glide.with(context!!).load(OtherUtils.getWeatherEnum(weatherUtil.todayStatus,false).getIcon()).into(weatherIcon)
                }

                2 ->{
                    windSpeed.text = msg.data.getString("weatherWindSpeed")
                    humidity.text = msg.data.getString("weatherHumidity")
                    highest.text = msg.data.getString("weatherHighest")
                    temp.text = msg.data.getString("weatherTemp")
                    Glide.with(context!!).load(OtherUtils.getWeatherEnum(msg.data.getString("weatherStatus"),false).getIcon()).into(weatherIcon)

                    dayAdapter = DayWeatherAdapter(context!!,dayWeatherData)
                    dayWeather.adapter = dayAdapter

                    hoursAdapter = DayWeatherAdapter(context!!,hoursWeatherData)
                    hoursWeather.adapter = hoursAdapter
                }
            }
        }
    }

    override fun setContentView(): Int {
        return R.layout.fragment_weather
    }

    override fun lazyLoad() {

        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        weatherTodayDBUtil = DatabaseUtil.writeTodayWeather(context!!)
        weatherDayDBUtil = DatabaseUtil.writeDayWeather(context!!)

        TypefaceUtil.replaceFont(contentView, "fonts/ProductSans.ttf")

        initView()
    }

    override fun onSkinUpdate(skin: Skin) {

    }

    private fun initView() {

        Glide.with(this).load(R.drawable.night_1).into(background)
        Glide.with(this).load(OtherUtils.getWeatherEnum("none",false).getIcon()).into(weatherIcon)

        dayWeather.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        hoursWeather.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)

        city.text = preferences.getString("city","北京")
        date.text = "${OtherUtils.getWeek(Date(System.currentTimeMillis()))} | ${SimpleDateFormat("MM月 dd | yyyy").format(Date())}"

        if (OtherUtils.getTimeDifference(preferences.getString("weatherRefresh","2016-05-01 12:00"))){
            loadWeatherNetwork()
        }else{
            loadWeatherDatabase()
        }


        location.setOnClickListener {
            startActivityForResult(Intent(context,CityActivity::class.java),CHOOSE_CITY)
        }

        refresh.setOnClickListener {
            loadWeatherNetwork()
            val animator = ObjectAnimator.ofFloat(refresh,"rotation",0f,360f)
            animator.duration = 500
            animator.interpolator = AnticipateInterpolator()
            animator.start()
        }

        nightMode.setOnClickListener {
            clickListener.onClick()
        }
        colorTheme.setOnClickListener {
            ColorChangeDialog().show(activity!!.supportFragmentManager)
        }
    }

    /**
     * 更新天气
     */
    private fun loadWeatherNetwork(){

        calculateThread = object : Thread() {
            override fun run() {
                super.run()

                weatherUtil = WeatherUtil(preferences.getString("weatherCode","101010100"))
                dayWeatherData = weatherUtil.dayWeatherData
                hoursWeatherData = weatherUtil.hoursWeatherData

                val msg = Message()
                msg.what = 1
                mHandler.sendMessage(msg)

                saveToDatabase(weatherUtil)

            }
        }

        calculateThread.start()

    }

    /**
     * 从数据库加载天气
     */
    private fun loadWeatherDatabase(){

        val bundle = Bundle()
        bundle.putString("weatherWindSpeed",preferences.getString("weatherWindSpeed","未知"))
        bundle.putString("weatherHumidity",preferences.getString("weatherHumidity","未知"))
        bundle.putString("weatherHighest",preferences.getString("weatherHighest","未知"))
        bundle.putString("weatherTemp",preferences.getString("weatherTemp","未知"))
        bundle.putString("weatherStatus",preferences.getString("weatherStatus","未知"))

        weatherTodayDBUtil!!.selectAll(todayData)
        weatherDayDBUtil!!.selectAll(dayData)

        hoursWeatherData.clear()
        dayWeatherData.clear()
        for (data in todayData){
            hoursWeatherData.add(DayWeatherBean(data.weatherTodayTime,data.weatherTodayStatus,data.weatherTodayTemp))
        }

        for (data in dayData){
            dayWeatherData.add(DayWeatherBean(data.weatherDayTime,data.weatherDayStatus,data.weatherDayTemp))
        }

        val msg = Message()
        msg.what = 2
        msg.data = bundle
        mHandler.sendMessage(msg)
    }

    /**
     * 保存到数据库
     */
    private fun saveToDatabase(weatherUtil: WeatherUtil){

        //清空数据库
        weatherTodayDBUtil!!.clearTable()
        weatherDayDBUtil!!.clearTable()

        //更新时间
        val simpleFormat = SimpleDateFormat("yyyy-MM-dd hh:mm").format(Date())
        editor.putString("weatherRefresh",simpleFormat)

        editor.putString("weatherWindSpeed",weatherUtil.todayWindSpeed)
        editor.putString("weatherHumidity",weatherUtil.todayHumidity)
        editor.putString("weatherHighest",weatherUtil.todayHighest)
        editor.putString("weatherTemp",weatherUtil.nowTemps)
        editor.putString("weatherStatus",weatherUtil.todayStatus)
        editor.apply()

        val hoursBean = weatherUtil.hoursWeatherData
        val dayBean = weatherUtil.dayWeatherData2

        val dataTodayBean = AssistantBean()
        val dataDayBean = AssistantBean()

        for (data in hoursBean){
            dataTodayBean.weatherTodayTime = data.date
            dataTodayBean.weatherTodayStatus = data.weather
            dataTodayBean.weatherTodayTemp = data.temp
            weatherTodayDBUtil!!.install(dataTodayBean)
        }

        for (data in dayBean){
            dataDayBean.weatherDayTime = data.date
            dataDayBean.weatherDayStatus = data.weather
            dataDayBean.weatherDayTemp = data.temp
            dataDayBean.weatherDayWindSpeed = data.windSpeed
            weatherDayDBUtil!!.install(dataDayBean)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CHOOSE_CITY ->{
                if (resultCode == Activity.RESULT_OK){
                    city.text = preferences.getString("city","北京")
                    loadWeatherNetwork()
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick()
    }

    private lateinit var clickListener: OnClickListener

    fun setClickListener(clickListener: OnClickListener) {
        this.clickListener = clickListener
    }

    override fun onDestroy() {
        super.onDestroy()

        if (weatherTodayDBUtil != null)
            weatherTodayDBUtil!!.close()
        if (weatherDayDBUtil != null)
            weatherDayDBUtil!!.close()
    }
}
