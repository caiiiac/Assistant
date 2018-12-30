package org.andcreator.assistant.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import org.andcreator.assistant.R


class HomePopupWindow : PopupWindow,View.OnClickListener{
    private lateinit var mCtx: Context

    constructor(ctx: Context) : super(ctx) {
        init(ctx, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    constructor(ctx: Context, w: Int, h: Int) : this(ctx) {
        init(ctx, w, h)
    }

    @SuppressLint("InflateParams")
    private fun init(ctx: Context, w: Int, h: Int) {
        mCtx = ctx
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.animationStyle = R.style.AnimationPreview
        val contentView = LayoutInflater.from(ctx).inflate(R.layout.popup_home, null, false)
        setContentView(contentView)

        val allApps = contentView.findViewById<TextView>(R.id.allApps)
        val widget = contentView.findViewById<TextView>(R.id.widget)
        val wallpaper = contentView.findViewById<TextView>(R.id.wallpaper)
        val settings = contentView.findViewById<TextView>(R.id.settings)

        allApps.setOnClickListener(this)
        widget.setOnClickListener(this)
        wallpaper.setOnClickListener(this)
        settings.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id){
                R.id.allApps ->{
                    clickListener.onClick(1)
                    dismiss()
                }
                R.id.widget ->{
                    clickListener.onClick(2)
                    dismiss()
                }
                R.id.wallpaper ->{
                    clickListener.onClick(3)
                    dismiss()
                }
                R.id.settings ->{
                    clickListener.onClick(4)
                    dismiss()
                }
            }
        }
    }

    interface OnItemClickListener{
        fun onClick(position: Int)
    }

    private lateinit var clickListener: OnItemClickListener

    fun setClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

}
