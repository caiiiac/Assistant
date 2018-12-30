package org.andcreator.assistant.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.andcreator.assistant.R
import org.andcreator.assistant.bean.AppsItemBean

class AppsItemAdapter(private val context: Context,
                      private var dataList: ArrayList<AppsItemBean>) :
    RecyclerView.Adapter<AppsItemAdapter.ViewHolder>() {


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder.create(LayoutInflater.from(context),p0)
    }

    override fun getItemCount(): Int {

        return dataList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.onBind(dataList[p1],context,clickListener)
    }

    interface OnItemLongClickListener{
        fun onClick(name: String,icon: Drawable,pkgName: String,activityName: String)
    }

    private lateinit var clickListener: OnItemLongClickListener

    fun setLongClickListener(clickListener: OnItemLongClickListener) {
        this.clickListener = clickListener
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
                return ViewHolder(inflater.inflate(R.layout.item_apps, parent, false))
            }
        }

        private val name = itemView.findViewById<TextView>(R.id.name)
        private val icon = itemView.findViewById<ImageView>(R.id.icon)

        fun onBind(bean: AppsItemBean, context: Context,clickListener: OnItemLongClickListener){
            name.text = bean.name
            Glide.with(context).load(bean.icon).into(icon)

            icon.setOnClickListener {
                context.startActivity(bean.intent)
            }

            icon.setOnLongClickListener {
                clickListener.onClick(bean.name,bean.icon,bean.pkgName,bean.activityName)
                true
            }
        }
    }
}