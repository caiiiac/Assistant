package org.andcreator.assistant.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import org.andcreator.assistant.R
import org.andcreator.assistant.bean.CityBean

class CityAdapter(val context: Context,val data: List<CityBean> ) : RecyclerView.Adapter<CityAdapter.CityHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CityHolder {
        return CityHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_city,p0,false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: CityHolder, p1: Int) {

        val animator1 = ObjectAnimator.ofFloat(p0.itemView, "alpha", 0f, 1f)
        animator1.setDuration(400).start()

        val bean = data[p1]
        p0.cityName.text = bean.county
        p0.choose.setOnClickListener {
            clickListener.onClick(bean.county,bean.weatherCode)
        }
    }

    class CityHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cityName: TextView = itemView.findViewById(R.id.cityName)
        val choose: CardView = itemView.findViewById(R.id.choose)
    }


    interface OnItemClickListener{
        fun onClick(city: String,weatherCode: String)
    }

    private lateinit var clickListener: OnItemClickListener

    fun setClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }
}