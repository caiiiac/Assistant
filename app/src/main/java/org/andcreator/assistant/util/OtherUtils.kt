package org.andcreator.assistant.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import org.andcreator.assistant.R
import java.io.ByteArrayOutputStream
import java.util.*
import java.text.SimpleDateFormat


object OtherUtils {

    fun Bitmap2Bytes(bm: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }


    /**
     * 实现文本复制功能
     * @param content
     */
    fun copy(content: String, context: Context) {
        // 得到剪贴板管理器
        val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = ClipData.newPlainText(context.getString(R.string.app_name),content.trim())
        cmb.primaryClip = data
    }

    fun getWeatherEnum(weather: String,isNight: Boolean): WeatherIcon{
        if (isNight){

            return when {
                weather.contains("晴转多云") -> WeatherIcon.FEW_CLOUDS_NIGHT
                weather.contains("多云转晴") -> WeatherIcon.FEW_CLOUDS_NIGHT
                weather.contains("雨夹雪") -> WeatherIcon.SNOW_RAIN
                weather.contains("雷阵雨") -> WeatherIcon.STORM_NIGHT
                weather.contains("小雨") -> WeatherIcon.DRIZZLE_NIGHT
                weather.contains("中雨") -> WeatherIcon.RAIN_NIGHT
                weather.contains("大雨") -> WeatherIcon.SHOWERS_NIGHT
                weather.contains("暴雨") -> WeatherIcon.SHOWERS_NIGHT
                weather.contains("小雪") -> WeatherIcon.SNOW_SCATTERED_NIGHT
                weather.contains("中雪") -> WeatherIcon.SNOW_SCATTERED_NIGHT
                weather.contains("大雪") -> WeatherIcon.SNOW
                weather.contains("冰雹") -> WeatherIcon.HAIL
                weather.contains("多云") -> WeatherIcon.HAZE
                weather.contains("冻雨") -> WeatherIcon.HAIL
                weather.contains("雨") -> WeatherIcon.SHOWERS_NIGHT
                weather.contains("风") -> WeatherIcon.WIND
                weather.contains("霾") -> WeatherIcon.HAZE
                weather.contains("阴") -> WeatherIcon.FEW_CLOUDS_NIGHT
                weather.contains("晴") -> WeatherIcon.CLEAR_NIGHT
                weather.contains("雾") -> WeatherIcon.FOG

                weather.contains("雷") -> WeatherIcon.STORM
                weather.contains("阵雨") -> WeatherIcon.RAIN_NIGHT
                else -> WeatherIcon.NONE_AVAILABLE
            }

        }else{

            return when {
                weather.contains("晴转多云") -> WeatherIcon.FEW_CLOUDS
                weather.contains("多云转晴") -> WeatherIcon.FEW_CLOUDS
                weather.contains("雨夹雪") -> WeatherIcon.SNOW_RAIN
                weather.contains("雷阵雨") -> WeatherIcon.STORM_DAY
                weather.contains("小雨") -> WeatherIcon.DRIZZLE_DAY
                weather.contains("中雨") -> WeatherIcon.RAIN_DAY
                weather.contains("大雨") -> WeatherIcon.SHOWERS_DAY
                weather.contains("暴雨") -> WeatherIcon.SHOWERS_DAY
                weather.contains("小雪") -> WeatherIcon.SNOW_SCATTERED_DAY
                weather.contains("中雪") -> WeatherIcon.SNOW_SCATTERED_DAY
                weather.contains("大雪") -> WeatherIcon.SNOW
                weather.contains("冰雹") -> WeatherIcon.HAIL
                weather.contains("多云") -> WeatherIcon.CLOUD
                weather.contains("冻雨") -> WeatherIcon.HAIL
                weather.contains("雨") -> WeatherIcon.SHOWERS_DAY
                weather.contains("阴") -> WeatherIcon.FEW_CLOUDS_NIGHT
                weather.contains("晴") -> WeatherIcon.CLEAR
                weather.contains("雾") -> WeatherIcon.FOG
                weather.contains("风") -> WeatherIcon.WIND
                weather.contains("霾") -> WeatherIcon.HAZE

                weather.contains("雷") -> WeatherIcon.STORM_DAY
                weather.contains("阵雨") -> WeatherIcon.RAIN_DAY
                else -> WeatherIcon.NONE_AVAILABLE
            }
        }
    }

    fun getWeek(date: Date): String {
        val weeks = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val cal = Calendar.getInstance()
        cal.time = date
        var weekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        if (weekIndex < 0) {
            weekIndex = 0
        }
        return weeks[weekIndex]
    }

    fun getTimeDifference(startTime: String) : Boolean{
        val simpleFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val now = SimpleDateFormat("yyyy-MM-dd hh:mm").format(Date())

        val from = simpleFormat.parse(startTime).time
        val to = simpleFormat.parse(now).time
        val hours = Math.abs(((to - from) / (1000 * 60 * 60)).toInt())

        return hours >= 2
    }

    /**
     * 获取屏幕高度(px
     */
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * 获取屏幕宽度(px)
     */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }


}