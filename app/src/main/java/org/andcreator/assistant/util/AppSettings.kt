package org.andcreator.assistant.util

import android.content.Context
import android.support.v4.content.ContextCompat
import org.andcreator.assistant.R
import org.andcreator.assistant.skin.Skin

object AppSettings {

    private const val KEY_NIGHT_MODE = "KEY_NIGHT_MODE"
    private const val KEY_NIGHT_MODE_LEVEL = "KEY_NIGHT_MODE_LEVEL"

    fun isNightMode(context: Context): Boolean{
        return get(context, KEY_NIGHT_MODE,false)
    }

    fun putNightMode(context: Context,boolean: Boolean){
        put(context, KEY_NIGHT_MODE,boolean)
    }

    fun getNightModeLevel(context: Context):Int{
        return get(context, KEY_NIGHT_MODE_LEVEL,110)
    }

    fun putNightModeLevel(context: Context,level:Int){
        put(context, KEY_NIGHT_MODE_LEVEL,level)
    }

    fun <T> put(context: Context, key:String, value: T){
        PreferencesUtil.put(context,key,value)
    }

    fun <T> put(context: Context, key:Int, value: T){
        PreferencesUtil.put(context,context.getString(key),value)
    }

    fun <T> get(context: Context, key:String, value: T): T{
        return PreferencesUtil.get(context,key,value)
    }

    fun <T> get(context: Context, key:Int, value: T): T{
        return PreferencesUtil.get(context,context.getString(key),value)
    }

    fun updateSkin(skin: Skin, context: Context){
        skin.colorAccent = getColorSettings(context, R.string.key_color_accent,R.color.colorAccent)
        skin.colorPrimary = getColorSettings(context, R.string.key_color_primary,R.color.colorPrimary)
        skin.colorPrimaryDark = getColorSettings(context, R.string.key_color_primary_dark,R.color.colorPrimaryDark)
        skin.colorPrimaryLight = getColorSettings(context, R.string.key_color_primary_light,R.color.colorPrimaryLight)
    }

    private fun getColorSettings(context: Context, name:Int, def:Int): Int{
        return get(context,context.getString(name), ContextCompat.getColor(context,def))
    }

}