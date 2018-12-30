package org.andcreator.assistant.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.andcreator.assistant.R

import kotlinx.android.synthetic.main.activity_city.*
import org.andcreator.assistant.util.DisplayUtil
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import org.andcreator.assistant.adapter.CityAdapter
import org.andcreator.assistant.bean.CityBean
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException


class CityActivity : AppCompatActivity() {

    private var expand = false
    private lateinit var adapter: CityAdapter
    private val citys = ArrayList<CityBean>()
    private val allCity = ArrayList<CityBean>()

    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city)

        editor = PreferenceManager.getDefaultSharedPreferences(this).edit()

        loadAll()
        initView()
        initClick()
    }

    private fun initView(){
        cityRecycler.layoutManager = LinearLayoutManager(this)
        adapter = CityAdapter(this,citys)


        adapter.setClickListener(object : CityAdapter.OnItemClickListener{
            override fun onClick(city: String, weatherCode: String) {
                editor.putString("city",city)
                editor.putString("weatherCode",weatherCode)
                editor.apply()

                animatorFinish()
            }

        })
        cityRecycler.adapter = adapter
    }

    private fun searchCity(cityName: String){

        doAsync {
            citys.clear()
            if (allCity.isNotEmpty()){
                for (bean in allCity){
                    if (bean.cityName.contains(cityName) || bean.province.contains(cityName) || bean.county.contains(cityName)){
                        citys.add(bean)
                    }
                }
            }

            uiThread {
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadAll(){
        doAsync {
            allCity.clear()
            val xml = resources.getXml(R.xml.city)
            var type = xml.eventType
            try {
                var province = ""
                var cityName = ""

                while (type != XmlPullParser.END_DOCUMENT){

                    when(type){
                        XmlPullParser.START_TAG ->{
                            var county = ""
                            var weatherCode = ""

                            if (xml.name == "province"){
                                if (xml.getAttributeValue(1).isNotEmpty())
                                    province = xml.getAttributeValue(1)
                            }
                            if (xml.name == "city"){
                                if (xml.getAttributeValue(1).isNotEmpty())
                                    cityName = xml.getAttributeValue(1)
                            }
                            if (xml.name == "county"){
                                county = xml.getAttributeValue(1)
                                weatherCode = xml.getAttributeValue(2)
                            }
                            if (county.isNotEmpty()){
                                allCity.add(CityBean(province,cityName,county,weatherCode))
                            }

                        }
                        XmlPullParser.TEXT ->{

                        }
                    }
                    type = xml.next()
                }
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun initClick(){

        locationButton.setOnClickListener {

            if (expand){
                if (inputCity.text.isNotEmpty()){
                    searchCity(inputCity.text.toString())
                    closeKeyBoard()
                }else{
                    Snackbar.make(frame,"你还没有输入城市",Snackbar.LENGTH_SHORT).show()
                }
            }

        }

        searchButton.setOnClickListener {
            animator()
        }
    }

    private fun animator(){
        if (expand){
            val animator = ValueAnimator.ofFloat(250f,108f)
            animator.duration = 500

            animator.addUpdateListener { animation ->
                val curValue = animation.animatedValue as Float

                val p = searchBox.layoutParams
                p.width = DisplayUtil.dip2px(this,curValue)
                searchBox.layoutParams = p

            }
            animator.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {

                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {
                    val animator7 = ObjectAnimator.ofFloat(inputCity, "alpha", 1f,0f)
                    animator7.duration = 250
                    animator7.interpolator = DecelerateInterpolator()
                    animator7.addListener(object : Animator.AnimatorListener{
                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            inputCity.visibility = View.INVISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {
                            closeKeyBoard()
                            citys.clear()
                            adapter.notifyDataSetChanged()
                        }

                    })
                    animator7.start()
                }

            })
            animator.start()

            val animator2 = ObjectAnimator.ofFloat(searchBox, "translationY", DisplayUtil.dip2px(this,-200f).toFloat(), 0f)
            animator2.duration = 500
            animator2.interpolator = DecelerateInterpolator()
            animator2.start()

            val animator3 = ObjectAnimator.ofFloat(location, "alpha", 1f,0f)
            animator3.duration = 250
            animator3.interpolator = DecelerateInterpolator()
            animator3.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    location.setImageResource(R.drawable.ic_location_on_white_24dp)
                    val animator4 = ObjectAnimator.ofFloat(location, "alpha", 0f,1f)
                    animator4.duration = 250
                    animator4.interpolator = DecelerateInterpolator()
                    animator4.start()
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }

            })
            animator3.start()

            val animator5 = ObjectAnimator.ofFloat(searchButton, "alpha", 1f,0f)
            animator5.duration = 250
            animator5.interpolator = DecelerateInterpolator()
            animator5.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    searchButton.setImageResource(R.drawable.ic_search_black_24dp)
                    val animator6 = ObjectAnimator.ofFloat(searchButton, "alpha", 0f,1f)
                    animator6.duration = 250
                    animator6.interpolator = DecelerateInterpolator()
                    animator6.start()
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }

            })
            animator5.start()
            expand = false
        }else{
            val animator = ValueAnimator.ofFloat(108f, 250f)
            animator.duration = 500

            animator.addUpdateListener { animation ->
                val curValue = animation.animatedValue as Float

                val p = searchBox.layoutParams
                p.width = DisplayUtil.dip2px(this,curValue)
                searchBox.layoutParams = p

            }

            animator.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    val animator7 = ObjectAnimator.ofFloat(inputCity, "alpha", 0f,1f)
                    animator7.duration = 250
                    animator7.interpolator = DecelerateInterpolator()
                    animator7.addListener(object : Animator.AnimatorListener{
                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {

                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {
                            inputCity.visibility = View.VISIBLE

                            inputCity.isFocusable = true
                            inputCity.isFocusableInTouchMode = true
                            inputCity.requestFocus()

                            val inputManager =
                                inputCity.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputManager.showSoftInput(inputCity, 0)

                        }

                    })
                    animator7.start()
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }

            })
            animator.start()

            val animator2 = ObjectAnimator.ofFloat(searchBox, "translationY", 0f,
                DisplayUtil.dip2px(this,-200f).toFloat()
            )
            animator2.duration = 500
            animator2.interpolator = DecelerateInterpolator()
            animator2.start()
            expand = true

            val animator3 = ObjectAnimator.ofFloat(location, "alpha", 1f,0f)
            animator3.duration = 250
            animator3.interpolator = DecelerateInterpolator()
            animator3.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    location.setImageResource(R.drawable.ic_search_white_24dp)
                    val animator4 = ObjectAnimator.ofFloat(location, "alpha", 0f,1f)
                    animator4.duration = 250
                    animator4.interpolator = DecelerateInterpolator()
                    animator4.start()
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }

            })
            animator3.start()

            val animator5 = ObjectAnimator.ofFloat(searchButton, "alpha", 1f,0f)
            animator5.duration = 250
            animator5.interpolator = DecelerateInterpolator()
            animator5.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    searchButton.setImageResource(R.drawable.ic_close_black_24dp)
                    val animator6 = ObjectAnimator.ofFloat(searchButton, "alpha", 0f,1f)
                    animator6.duration = 250
                    animator6.interpolator = DecelerateInterpolator()
                    animator6.start()
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }

            })
            animator5.start()
            expand = true
        }
    }

    private fun animatorFinish(){
        val animator = ValueAnimator.ofFloat(250f,108f)
        animator.duration = 500

        animator.addUpdateListener { animation ->
            val curValue = animation.animatedValue as Float

            val p = searchBox.layoutParams
            p.width = DisplayUtil.dip2px(this,curValue)
            searchBox.layoutParams = p

        }
        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                setResult(Activity.RESULT_OK)
                finish()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {
                val animator7 = ObjectAnimator.ofFloat(inputCity, "alpha", 1f,0f)
                animator7.duration = 250
                animator7.interpolator = DecelerateInterpolator()
                animator7.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationRepeat(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        inputCity.visibility = View.INVISIBLE
                    }

                    override fun onAnimationCancel(animation: Animator?) {

                    }

                    override fun onAnimationStart(animation: Animator?) {
                        closeKeyBoard()
                        citys.clear()
                        adapter.notifyDataSetChanged()
                    }

                })
                animator7.start()
            }

        })
        animator.start()

        val animator2 = ObjectAnimator.ofFloat(searchBox, "translationY", DisplayUtil.dip2px(this,-200f).toFloat(), 0f)
        animator2.duration = 500
        animator2.interpolator = DecelerateInterpolator()
        animator2.start()

        val animator3 = ObjectAnimator.ofFloat(location, "alpha", 1f,0f)
        animator3.duration = 250
        animator3.interpolator = DecelerateInterpolator()
        animator3.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                location.setImageResource(R.drawable.ic_location_on_white_24dp)
                val animator4 = ObjectAnimator.ofFloat(location, "alpha", 0f,1f)
                animator4.duration = 250
                animator4.interpolator = DecelerateInterpolator()
                animator4.start()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
        animator3.start()

        val animator5 = ObjectAnimator.ofFloat(searchButton, "alpha", 1f,0f)
        animator5.duration = 250
        animator5.interpolator = DecelerateInterpolator()
        animator5.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                searchButton.setImageResource(R.drawable.ic_search_black_24dp)
                val animator6 = ObjectAnimator.ofFloat(searchButton, "alpha", 0f,1f)
                animator6.duration = 250
                animator6.interpolator = DecelerateInterpolator()
                animator6.start()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
        animator5.start()
    }

    fun closeKeyBoard() {
        val inputMethodManager =
            inputCity.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(inputCity.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}
