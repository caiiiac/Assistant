package org.andcreator.assistant.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import org.andcreator.assistant.bean.MusicBean
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions.centerCropTransform
import com.bumptech.glide.request.target.Target
import de.hdodenhof.circleimageview.CircleImageView
import org.andcreator.assistant.AssistantApplication.Companion.context
import org.andcreator.assistant.R
import org.andcreator.assistant.util.DisplayUtil
import org.andcreator.assistant.util.TypefaceUtil
import org.andcreator.assistant.view.FastScrollRecyclerView
import java.util.*


/**
 * @date: 2018-10-31 03:27
 * @author: and
 */
class MusicListAdapter(private val context: Context,
                       private var dataList: ArrayList<MusicBean>,
                       private val selectedItemCallback: SelectedItemCallback):
    RecyclerView.Adapter<MusicListAdapter.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder.create(LayoutInflater.from(context),p0)
    }

    override fun getItemCount(): Int {

        return dataList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.onBind(dataList[p1],context,p1,clickListener,isThis(p1))
    }

    interface OnItemClickListener{
        fun onClick(position: Int)
    }

    private lateinit var clickListener: OnItemClickListener

    fun setClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val loadFailedProcessor = LoadFailedProcessor(context)

        companion object {
            fun create(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
                return ViewHolder(inflater.inflate(R.layout.item_musics, parent, false))
            }

        }
        private val titleView = itemView.findViewById<TextView>(R.id.titleView)
        private val infoView = itemView.findViewById<TextView>(R.id.infoView)
        private val bgView = itemView.findViewById<CircleImageView>(R.id.bgView)
        private val time = itemView.findViewById<TextView>(R.id.time)
        private val choose = itemView.findViewById<CardView>(R.id.choose)

        fun onBind(
            bean: MusicBean,
            context: Context,
            position: Int,
            clickListener: OnItemClickListener,
            b: Boolean
        ){

            TypefaceUtil.replaceFont(itemView, "fonts/ProductSans.ttf")
            titleView.text = bean.name
            infoView.text = ("${bean.artist} - ${bean.album}")
            time.text = timeConversion(bean.duration)

//            Glide.with(context).load(bean.getCover()).error(Glide.with(context).load(R.drawable.headphones_icon).apply(centerCropTransform())).apply(centerCropTransform()).into(bgView)
//            Glide.with(context).load(R.drawable.headphones_icon).apply(centerCropTransform()).into(bgView)

            Glide.with(context).load(bean).listener(loadFailedProcessor).into(bgView)
//            Glide.with(context).load(bean).into(bgView)

            choose.setOnClickListener {
                clickListener.onClick(position)
            }
            if (b){
                choose.setCardBackgroundColor(context.resources.getColor(R.color.colorAccent))
                choose.elevation = 2f
                choose.cardElevation = 4f
                choose.maxCardElevation = 4f
                titleView.setTextColor(context.resources.getColor(R.color.white))
                infoView.setTextColor(context.resources.getColor(R.color.white))
                time.setTextColor(context.resources.getColor(R.color.white))
            }else{
                choose.setCardBackgroundColor(context.resources.getColor(R.color.background))
                choose.elevation = 0f
                choose.cardElevation = 0f
                choose.maxCardElevation = 0f
                titleView.setTextColor(context.resources.getColor(R.color.text_music))
                infoView.setTextColor(context.resources.getColor(R.color.text_music))
                time.setTextColor(context.resources.getColor(R.color.colorAccent))
            }
        }

        private fun timeConversion(s: Long):String{
            val ss = s / 1000
            val hour = ss / 3600
            val minute = ss % 3600 / 60
            val second = ss % 60

            return when (hour) {
                0L -> {
                    if (second < 10){
                        minute.toString()+":0"+second.toString()
                    }else{
                        minute.toString()+":"+second.toString()
                    }
                }
                else -> {

                    if (second < 10){
                        hour.toString()+":0"+minute.toString()+":"+second.toString()
                    }else{
                        hour.toString()+":"+minute.toString()+":"+second.toString()
                    }
                }
            }
        }

        class LoadFailedProcessor(private val context: Context): RequestListener<Drawable> {

            private var selectedDrawable = context.getDrawable(R.drawable.headphones_icon)

            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                if(target == null){
                    return false
                }
                target.onLoadFailed(selectedDrawable)

                return true
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                return false
            }
        }

    }

    interface SelectedItemCallback{
        fun getSelectedItemPosition(): Int
    }

    private fun isThis(position: Int) = selectedItemCallback.getSelectedItemPosition() == position

    override fun getSectionName(position: Int): String {
        return dataList[position].name.substring(0, 1).toUpperCase(Locale.ENGLISH)
    }
}