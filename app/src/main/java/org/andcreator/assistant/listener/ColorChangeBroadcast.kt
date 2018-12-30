package org.andcreator.assistant.listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import org.andcreator.assistant.R


/**
 * @date: 2018/09/02 21:15
 * @author: lollipop
 * 颜色改变的广播
 */
class ColorChangeBroadcast(private val callback: Callback): BroadcastReceiver() {
    companion object {
        /**
         * 主题色
         */
        const val colorDominant = "colorDominant"
        /**
         * 有活力
         */
        const val colorVibrant = "colorVibrant"
        /**
         * 有活力 暗色
         */
        const val colorVibrantDark = "colorVibrantDark"
        /**
         * 有活力 亮色
         */
        const val colorVibrantLight = "colorVibrantLight"
        /**
         * 柔和
         */
        const val colorMuted = "colorMuted"
        /**
         * 柔和 暗色
         */
        const val colorMutedDark = "colorMutedDark"
        /**
         * 柔和 亮色
         */
        const val colorMutedLight = "colorMutedLight"

        const val ACTION = "ACTION_COLOR_CHANGE"

        fun sendBroadcast(context: Context, palette: Palette){
            val defColor = ContextCompat.getColor(context, R.color.colorPrimary)
            sendBroadcast(context,palette.getVibrantColor(defColor),
                palette.getDarkVibrantColor(defColor),
                palette.getLightVibrantColor(defColor),
                palette.getMutedColor(defColor),
                palette.getDarkMutedColor(defColor),
                palette.getLightMutedColor(defColor),
                palette.getDominantColor(defColor))
        }

        private fun sendBroadcast(context: Context,
                                  vibrant: Int, vibrantDark: Int, vibrantLight: Int,
                                  muted: Int, mutedDark: Int, mutedLight: Int, dominant: Int){
            val intent = Intent(ACTION)
            intent.putExtra(colorVibrant,vibrant)
            intent.putExtra(colorVibrantDark,vibrantDark)
            intent.putExtra(colorVibrantLight,vibrantLight)
            intent.putExtra(colorMuted,muted)
            intent.putExtra(colorMutedDark,mutedDark)
            intent.putExtra(colorMutedLight,mutedLight)
            intent.putExtra(colorDominant,dominant)
            context.sendBroadcast(intent)
        }

    }

    fun register(context: Context){
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION)
        context.registerReceiver(this,intentFilter)
    }

    fun unregister(context: Context){
        context.unregisterReceiver(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null){
            callback.onColorBroadcast(intent)
        }
    }

    interface Callback{
        fun onColorBroadcast(intent: Intent)
    }

}