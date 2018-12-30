package org.andcreator.assistant

import android.graphics.drawable.Drawable

object SharedVariable{

    private var appIcon: Drawable? = null
    private var behavior: Boolean = false

    fun getDrawable(): Drawable? {
        return appIcon
    }

    fun setDrawable(appIcon: Drawable){
        this.appIcon = appIcon
    }

    fun getBehavior(): Boolean{
        return behavior
    }

    fun setBehavior(behavior: Boolean){
        this.behavior = behavior
    }
}