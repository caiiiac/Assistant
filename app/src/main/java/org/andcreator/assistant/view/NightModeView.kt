package org.andcreator.assistant.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import org.andcreator.assistant.drawable.NightModeDrawable

class NightModeView(context: Context, attrs: AttributeSet?, defStyleAttr:Int, defStyleRes:Int)
    : View(context,attrs, defStyleAttr,defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr:Int):this(context,attrs,defStyleAttr,0)

    constructor(context: Context, attrs: AttributeSet?):this(context,attrs,0)

    constructor(context: Context):this(context,null)

    private val nightModeDrawable = NightModeDrawable()

    var level:Int
        set(value) {
            nightModeDrawable.alpha = value
        }
        get() {
            return nightModeDrawable.alpha
        }

    init {

        background = nightModeDrawable

        if(isInEditMode){
            level = 50
        }

    }



}