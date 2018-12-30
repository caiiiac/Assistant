package org.andcreator.assistant.bean

import java.text.SimpleDateFormat
import java.util.*

class AssistantBean {

    companion object {

        private const val ONE_SECOND = 1000L

        private const val ONE_MINUTE = ONE_SECOND * 60

        private const val ONE_HOUR = ONE_MINUTE * 60

        private val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd-HH:mm:ss", Locale.getDefault())

        const val APP = "app"

        const val WIDGET = "widget"

    }

    //天气表的id
    var weatherTodayId = ""
    //时 时间
    var weatherTodayTime = ""
    //时 天气状态
    var weatherTodayStatus = ""
    //时 温度
    var weatherTodayTemp = ""

    var weatherDayId = ""
    //天 时间
    var weatherDayTime = ""
    //天 天气状态
    var weatherDayStatus = ""
    //天 温度
    var weatherDayTemp = ""
    //天 weatherDayStatus
    var weatherDayWindSpeed = ""

    //Launcher表的id
    var launcherId = ""

    //Launcher摆放部件类型
    var launcherType = ""

    //Launcher摆放部件名称
    var launcherName = ""

    //Launcher摆放app图标
    var launcherIcon = ""

    //Launcher部件摆放X起始格点
    var launcherX = 1

    //Launcher部件摆放Y起始格点
    var launcherY = 1

    //Launcher部件的宽
    var launcherWidth = 1

    //Launcher部件的高
    var launcherHeight = 1

    //Launcher App点击的Intent
    var launcherIntent = ""

    //Launcher小部件Id
    var launcherWidgetId = -1
}