package org.andcreator.assistant.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.andcreator.assistant.R
import org.andcreator.assistant.bean.DayWeatherBean
import android.text.style.AbsoluteSizeSpan
import org.andcreator.assistant.util.OtherUtils


class DayWeatherAdapter(private val context: Context,
                        private val data: ArrayList<DayWeatherBean>) : RecyclerView.Adapter<DayWeatherAdapter.DayWeatherHolder>() {


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DayWeatherHolder {
        return DayWeatherHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_weather_day,p0,false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: DayWeatherHolder, p1: Int) {

        val animator1 = ObjectAnimator.ofFloat(p0.itemView, "alpha", 0f, 1f)
        animator1.setDuration(400).start()

        val bean = data[p1]
        p0.day.text = bean.date

        Glide.with(context).load(OtherUtils.getWeatherEnum(bean.weather,false).getIcon()).into(p0.weather)

        if (bean.temp.indexOf("/") > 0){
            val spannable = SpannableString(bean.temp)
            spannable.setSpan(AbsoluteSizeSpan(14, true), bean.temp.indexOf("/"),bean.temp.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            p0.temp.text = spannable
        }else{
            p0.temp.text = bean.temp
        }
    }

    class DayWeatherHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val day: TextView = itemView.findViewById(R.id.day)
        val weather: ImageView = itemView.findViewById(R.id.weather)
        val temp: TextView = itemView.findViewById(R.id.temp)
    }
}