package org.andcreator.assistant.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import org.andcreator.assistant.bean.AssistantBean

class DatabaseUtil  private constructor(context: Context, dbName:String, factory: SQLiteDatabase.CursorFactory?, version:Int)
    : SQLiteOpenHelper(context,dbName,factory,version){

    companion object {

        const val DB_NAME = "Assistant.db"
        const val VERSION = 1

        fun readTodayWeather(context: Context):WeatherTodayDBOperate{
            return WeatherTodayDBOperate(DatabaseUtil(context).readableDatabase)
        }

        fun writeTodayWeather(context: Context):WeatherTodayDBOperate{
            return WeatherTodayDBOperate(DatabaseUtil(context).writableDatabase)
        }

        fun readDayWeather(context: Context):WeatherDayDBOperate{
            return WeatherDayDBOperate(DatabaseUtil(context).readableDatabase)
        }

        fun writeDayWeather(context: Context):WeatherDayDBOperate{
            return WeatherDayDBOperate(DatabaseUtil(context).writableDatabase)
        }

        fun readLauncher(context: Context):LauncherDBOperate{
            return LauncherDBOperate(DatabaseUtil(context).readableDatabase)
        }

        fun writeLauncher(context: Context):LauncherDBOperate{
            return LauncherDBOperate(DatabaseUtil(context).writableDatabase)
        }

    }

    private constructor(context: Context):this(context, DB_NAME,null, VERSION)

    private object Assistant{
        const val WEATHER_TODAY_TABLE = "WEATHER_TODAY"
        const val weatherTodayId = "weatherTodayId"
        const val weatherTodayTime = "weatherTodayTime"
        const val weatherTodayStatus = "weatherTodayStatus"
        const val weatherTodayTemp = "weatherTodayTemp"


        const val WEATHER_DAY_TABLE = "WEATHER_DAY"
        const val weatherDayId = "weatherDayId"
        const val weatherDayTime = "weatherDayTime"
        const val weatherDayStatus = "weatherDayStatus"
        const val weatherDayTemp = "weatherDayTemp"
        const val weatherDayWindSpeed = "weatherDayWindSpeed"


        const val LAUNCHER_TABLE = "LAUNCHER"
        const val launcherId = "launcherId"
        const val launcherType = "launcherType"
        const val launcherName = "launcherName"
        const val launcherIcon = "launcherIcon"
        const val launcherX = "launcherX"
        const val launcherY = "launcherY"
        const val launcherWidth = "launcherWidth"
        const val launcherHeight = "launcherHeight"
        const val launcherIntent = "launcherIntent"
        const val launcherWidgetId = "launcherWidgetId"

        const val CREATE_TODAY_WEATHER = "create table $WEATHER_TODAY_TABLE ( " +
            " $weatherTodayId INTEGER PRIMARY KEY, " +
            " $weatherTodayTime TEXT , " +
            " $weatherTodayStatus TEXT , " +
            " $weatherTodayTemp TEXT " +
            " ); "

        const val CREATE_DAY_WEATHER = "create table $WEATHER_DAY_TABLE ( " +
            " $weatherDayId INTEGER PRIMARY KEY, " +
            " $weatherDayStatus TEXT , " +
            " $weatherDayTime TEXT , " +
            " $weatherDayTemp TEXT , " +
            " $weatherDayWindSpeed TEXT " +
            " ); "

        const val CREATE_LAUNCHER = "create table $LAUNCHER_TABLE ( " +
            " $launcherId INTEGER PRIMARY KEY, " +
            " $launcherType TEXT , " +
            " $launcherName TEXT , " +
            " $launcherIcon TEXT , " +
            " $launcherX INTEGER , " +
            " $launcherY INTEGER , " +
            " $launcherWidth INTEGER , " +
            " $launcherHeight INTEGER , " +
            " $launcherIntent TEXT , " +
            " $launcherWidgetId INTEGER " +
            " ); "

        const val SELECT_TODAY_WEATHER_ALL = "SELECT $weatherTodayId , $weatherTodayTime , $weatherTodayStatus , " +
            " $weatherTodayTemp " +
            " FROM  $WEATHER_TODAY_TABLE " +
            " ORDER BY $weatherTodayId DESC "

        const val SELECT_TODAY_WEATHER_BY_ID = "SELECT $weatherTodayId , $weatherTodayTime , $weatherTodayStatus , " +
            " $weatherTodayTemp " +
            " FROM  $WEATHER_TODAY_TABLE " +
            " WHERE $weatherTodayId = ? "

        const val SELECT_DAY_WEATHER_ALL = "SELECT $weatherDayId , $weatherDayStatus , $weatherDayTime , $weatherDayTemp , $weatherDayWindSpeed " +
            " FROM  $WEATHER_DAY_TABLE " +
            " ORDER BY $weatherDayId DESC "

        const val SELECT_DAY_WEATHER_BY_ID = "SELECT $weatherDayId , $weatherDayStatus , $weatherDayTime , $weatherDayTemp , $weatherDayWindSpeed " +
            " FROM  $WEATHER_DAY_TABLE " +
            " WHERE $weatherDayId = ? "

        const val SELECT_LAUNCHER_ALL = "SELECT $launcherId , $launcherType , $launcherName , $launcherIcon , $launcherX , $launcherY , " +
            "$launcherWidth , $launcherHeight , $launcherIntent , $launcherWidgetId " +
            " FROM  $LAUNCHER_TABLE " +
            " ORDER BY $launcherId DESC "

        const val SELECT_LAUNCHER_BY_ID = "SELECT $launcherId , $launcherType , $launcherName , $launcherIcon , $launcherX , $launcherY , " +
            "$launcherWidth , $launcherHeight , $launcherIntent , $launcherWidgetId " +
            " FROM  $LAUNCHER_TABLE " +
            " WHERE $launcherId = ? "

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(Assistant.CREATE_TODAY_WEATHER)
        db?.execSQL(Assistant.CREATE_DAY_WEATHER)
        db?.execSQL(Assistant.CREATE_LAUNCHER)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    class WeatherTodayDBOperate (private val database:SQLiteDatabase){

        fun install(bean:AssistantBean): WeatherTodayDBOperate{

            val values = ContentValues()
            values.put(Assistant.weatherTodayTime,bean.weatherTodayTime)
            values.put(Assistant.weatherTodayStatus,bean.weatherTodayStatus)
            values.put(Assistant.weatherTodayTemp,bean.weatherTodayTemp)
            database.insert(Assistant.WEATHER_TODAY_TABLE,"",values)

            return this
        }

        fun delete(id:String): WeatherTodayDBOperate{

            if(!TextUtils.isEmpty(id)){
                try {
                    database.delete(Assistant.WEATHER_TODAY_TABLE," ${Assistant.weatherTodayId} = ? ",arrayOf(id))
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            return this
        }

        fun update(bean:AssistantBean): WeatherTodayDBOperate{

            val values = ContentValues()
            values.put(Assistant.weatherTodayTime,bean.weatherTodayTime)
            values.put(Assistant.weatherTodayStatus,bean.weatherTodayStatus)
            values.put(Assistant.weatherTodayTemp,bean.weatherTodayTemp)
            database.update(Assistant.WEATHER_TODAY_TABLE,values," ${Assistant.weatherTodayId} = ? ", arrayOf(bean.weatherTodayId))

            return this
        }

        fun selectAll(beanList:ArrayList<AssistantBean>): WeatherTodayDBOperate{

            beanList.clear()

            val cursor = database.rawQuery(Assistant.SELECT_TODAY_WEATHER_ALL,null)
            putData(beanList,cursor)
            cursor.close()

            return this

        }

        fun selectById(id:String,bean:AssistantBean): WeatherTodayDBOperate{

            val cursor = database.rawQuery(Assistant.SELECT_TODAY_WEATHER_BY_ID, arrayOf( id ))

            while (cursor.moveToNext()) {

                putBean(bean,cursor)

                break
            }

            cursor.close()

            return this
        }

        /**
         * 整理数据，将数据库数据整理为Bean对象
         */
        private fun putData(list: ArrayList<AssistantBean>,cursor: Cursor){

            while (cursor.moveToNext()) {

                val bean = AssistantBean()

                putBean(bean,cursor)

                list.add(bean)
            }
        }

        private fun putBean(bean: AssistantBean,cursor: Cursor){
            bean.apply {
                weatherTodayId = ""+cursor.getInt(cursor.getColumnIndex(Assistant.weatherTodayId))
                weatherTodayTime = cursor.getString(cursor.getColumnIndex(Assistant.weatherTodayTime))
                weatherTodayStatus = cursor.getString(cursor.getColumnIndex(Assistant.weatherTodayStatus))
                weatherTodayTemp = cursor.getString(cursor.getColumnIndex(Assistant.weatherTodayTemp))
            }
        }

        fun clearTable(){
            database.execSQL("delete from ${Assistant.WEATHER_TODAY_TABLE}")
        }

        /**
         * 回收销毁当前数据库操作连接
         */
        fun close(){
            database.close()

        }

    }

    class WeatherDayDBOperate (private val database:SQLiteDatabase){

        fun install(bean:AssistantBean): WeatherDayDBOperate{

            val values = ContentValues()
            values.put(Assistant.weatherDayStatus,bean.weatherDayStatus)
            values.put(Assistant.weatherDayTime,bean.weatherDayTime)
            values.put(Assistant.weatherDayTemp,bean.weatherDayTemp)
            values.put(Assistant.weatherDayWindSpeed,bean.weatherDayWindSpeed)
            database.insert(Assistant.WEATHER_DAY_TABLE,"",values)

            return this
        }

        fun delete(id:String): WeatherDayDBOperate{

            if(!TextUtils.isEmpty(id)){
                try {
                    database.delete(Assistant.WEATHER_DAY_TABLE," ${Assistant.weatherDayId} = ? ",arrayOf(id))
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            return this
        }

        fun update(bean:AssistantBean): WeatherDayDBOperate{

            val values = ContentValues()
            values.put(Assistant.weatherDayStatus,bean.weatherDayStatus)
            values.put(Assistant.weatherDayTime,bean.weatherDayTime)
            values.put(Assistant.weatherDayTemp,bean.weatherDayTemp)
            values.put(Assistant.weatherDayWindSpeed,bean.weatherDayWindSpeed)
            database.update(Assistant.WEATHER_DAY_TABLE,values," ${Assistant.weatherDayId} = ? ", arrayOf(bean.weatherDayId))

            return this
        }

        fun selectAll(beanList:ArrayList<AssistantBean>): WeatherDayDBOperate{

            beanList.clear()

            val cursor = database.rawQuery(Assistant.SELECT_DAY_WEATHER_ALL,null)
            putData(beanList,cursor)
            cursor.close()

            return this

        }

        fun selectById(id:String,bean:AssistantBean): WeatherDayDBOperate{

            val cursor = database.rawQuery(Assistant.SELECT_DAY_WEATHER_BY_ID, arrayOf( id ))

            while (cursor.moveToNext()) {

                putBean(bean,cursor)

                break
            }

            cursor.close()

            return this
        }

        /**
         * 整理数据，将数据库数据整理为Bean对象
         */
        private fun putData(list: ArrayList<AssistantBean>,cursor: Cursor){

            while (cursor.moveToNext()) {

                val bean = AssistantBean()

                putBean(bean,cursor)

                list.add(bean)
            }
        }

        private fun putBean(bean: AssistantBean,cursor: Cursor){
            bean.apply {
                weatherDayId = ""+cursor.getInt(cursor.getColumnIndex(Assistant.weatherDayId))
                weatherDayTime = cursor.getString(cursor.getColumnIndex(Assistant.weatherDayTime))
                weatherDayStatus = cursor.getString(cursor.getColumnIndex(Assistant.weatherDayStatus))
                weatherDayTemp = cursor.getString(cursor.getColumnIndex(Assistant.weatherDayTemp))
                weatherDayWindSpeed = cursor.getString(cursor.getColumnIndex(Assistant.weatherDayWindSpeed))
            }
        }

        fun clearTable(){
            database.execSQL("delete from ${Assistant.WEATHER_DAY_TABLE}")
        }

        /**
         * 回收销毁当前数据库操作连接
         */
        fun close(){
            database.close()

        }

    }

    class LauncherDBOperate (private val database:SQLiteDatabase){

        fun install(bean:AssistantBean): Long{

            val values = ContentValues()
            values.put(Assistant.launcherType,bean.launcherType)
            values.put(Assistant.launcherName,bean.launcherName)
            values.put(Assistant.launcherIcon,bean.launcherIcon)
            values.put(Assistant.launcherX,bean.launcherX)
            values.put(Assistant.launcherY,bean.launcherY)
            values.put(Assistant.launcherWidth,bean.launcherWidth)
            values.put(Assistant.launcherHeight,bean.launcherHeight)
            values.put(Assistant.launcherIntent,bean.launcherIntent)
            values.put(Assistant.launcherWidgetId,bean.launcherWidgetId)

            return database.insert(Assistant.LAUNCHER_TABLE,"",values)
        }

        fun delete(id:String): LauncherDBOperate{

            if(!TextUtils.isEmpty(id)){
                try {
                    database.delete(Assistant.LAUNCHER_TABLE," ${Assistant.launcherId} = ? ",arrayOf(id))
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            return this
        }

        fun update(bean:AssistantBean): LauncherDBOperate{

            val values = ContentValues()
            values.put(Assistant.launcherType,bean.launcherType)
            values.put(Assistant.launcherX,bean.launcherX)
            values.put(Assistant.launcherY,bean.launcherY)
            values.put(Assistant.launcherWidth,bean.launcherWidth)
            values.put(Assistant.launcherHeight,bean.launcherHeight)
            database.update(Assistant.LAUNCHER_TABLE,values," ${Assistant.launcherId} = ? ", arrayOf(bean.launcherId))
//            暂时不用修改的数据
//            values.put(Assistant.launcherName,bean.launcherName)
//            values.put(Assistant.launcherIcon,bean.launcherIcon)
//            values.put(Assistant.launcherIntent,bean.launcherIntent)
//            values.put(Assistant.launcherWidgetId,bean.launcherWidgetId)
            return this
        }

        fun selectAll(beanList:ArrayList<AssistantBean>): LauncherDBOperate{

            beanList.clear()

            val cursor = database.rawQuery(Assistant.SELECT_LAUNCHER_ALL,null)
            putData(beanList,cursor)
            cursor.close()

            return this

        }

        fun selectById(id:String,bean:AssistantBean): LauncherDBOperate{

            val cursor = database.rawQuery(Assistant.SELECT_LAUNCHER_BY_ID, arrayOf( id ))

            while (cursor.moveToNext()) {

                putBean(bean,cursor)

                break
            }

            cursor.close()

            return this
        }

        /**
         * 整理数据，将数据库数据整理为Bean对象
         */
        private fun putData(list: ArrayList<AssistantBean>,cursor: Cursor){

            while (cursor.moveToNext()) {

                val bean = AssistantBean()

                putBean(bean,cursor)

                list.add(bean)
            }
        }

        private fun putBean(bean: AssistantBean,cursor: Cursor){
            bean.apply {
                launcherId = ""+cursor.getInt(cursor.getColumnIndex(Assistant.launcherId))
                launcherType = cursor.getString(cursor.getColumnIndex(Assistant.launcherType))
                launcherName = cursor.getString(cursor.getColumnIndex(Assistant.launcherName))
                launcherIcon = cursor.getString(cursor.getColumnIndex(Assistant.launcherIcon))
                launcherX = cursor.getInt(cursor.getColumnIndex(Assistant.launcherX))
                launcherY = cursor.getInt(cursor.getColumnIndex(Assistant.launcherY))
                launcherWidth = cursor.getInt(cursor.getColumnIndex(Assistant.launcherWidth))
                launcherHeight = cursor.getInt(cursor.getColumnIndex(Assistant.launcherHeight))
                launcherIntent = cursor.getString(cursor.getColumnIndex(Assistant.launcherIntent))
                launcherWidgetId = cursor.getInt(cursor.getColumnIndex(Assistant.launcherWidgetId))
            }
        }

        /**
         * 回收销毁当前数据库操作连接
         */
        fun close(){
            database.close()

        }

    }

}