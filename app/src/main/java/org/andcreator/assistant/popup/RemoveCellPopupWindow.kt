package org.andcreator.assistant.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import org.andcreator.assistant.R

class RemoveCellPopupWindow : PopupWindow, View.OnClickListener{

    private lateinit var mCtx: Context

    private var id: String = "-1"

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
        val contentView = LayoutInflater.from(ctx).inflate(R.layout.popup_remove_cell, null, false)
        setContentView(contentView)

        val remove = contentView.findViewById<CardView>(R.id.remove)
        remove.setOnClickListener(this)
    }

    fun setId(id: String){
        this.id = id
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id){
                R.id.remove ->{
                    if (id != "-1"){
                        clickListener.onClick(id)
                    }
                    dismiss()
                }
            }
        }
    }

    interface OnItemClickListener{
        fun onClick(id: String)
    }

    private lateinit var clickListener: OnItemClickListener

    fun setClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

}