package org.andcreator.assistant.constant

import android.content.Context
import android.graphics.Color
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


/**
 * Created by lollipop on 2018/1/31.
 * @author Lollipop
 * 常量类
 */
object MusicConstant {

    const val NotificationChannelId = "LMusic"
    const val NotificationChannelName = "LMusic"
    const val NotificationChannelColor = Color.BLUE

    fun getLogPath(): String{
        return getESDir("log")
    }

    private fun getESDRoot():String{
        return Environment.getExternalStorageDirectory().absolutePath + "/LMusic/"
    }

    private fun getESDir(name:String):String{
        return getESDRoot() +name
    }

    fun getAppImgPath(context: Context): String {
        return context.filesDir.absolutePath + "/img"
    }

    fun getCacheImgPath(context: Context): String {
        return context.cacheDir.path + "/img"
    }

    fun getCacheSmallImgPath(context: Context): String {
        return context.cacheDir.path + "/img/small"
    }

    fun getCacheVoicePath(context: Context): String {
        return context.cacheDir.path + "/voice"
    }

    fun getSDImgPath(): String {
        return getESDRoot() + "/img"
    }

    fun getBGImgPath(): String {
        return getESDRoot() + "/img/bg"
    }

    fun getSDSmallImgPath(): String {
        return getESDRoot() + "/img/small"
    }

    fun getSDVoicePath(): String {
        return getESDRoot() + "/voice"
    }

    fun getSDTxtPath(): String {
        return getESDRoot() + "/txt"
    }

    fun getSDLogPath(): String {
        return getESDRoot() + "/log"
    }

    fun getSDAppPath(context: Context): String {
        return getESDRoot() + "/app"
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    fun getVersion(context: Context): String {
        return try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            val version = info.versionName
            version
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }

    }

    fun getVersionCode(context: Context): String {
        return try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            val version = info.versionCode.toString() + ""
            version
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }

    }

    fun getMapStylePath(context: Context): String{
        val styleName = "map_style.data"
        var outputStream: FileOutputStream? = null
        var inputStream: InputStream? = null
        val filePath: String = context.filesDir.absolutePath
        try {
            inputStream = context.assets.open(styleName)
            val b = ByteArray(inputStream.available())
            inputStream.read(b)

            val file = File("$filePath/$styleName")
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            outputStream = FileOutputStream(file)
            outputStream.write(b)

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close()

                if (outputStream != null)
                    outputStream.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return "$filePath/$styleName"
    }

}