package org.andcreator.assistant.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.andcreator.assistant.AssistantApplication
import org.andcreator.assistant.R
import org.andcreator.assistant.SharedVariable
import org.andcreator.assistant.dialog.ColorChangeDialog
import org.andcreator.assistant.fragment.*
import org.andcreator.assistant.skin.Skin
import org.andcreator.assistant.skin.SkinActivity
import org.andcreator.assistant.util.AppSettings
import org.andcreator.pagerbottomsheet.BottomSheetUtils
import org.andcreator.pagerbottomsheet.ViewPagerBottomSheetBehavior
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.ArrayList


class MainActivity : SkinActivity()
    , ColorChangeDialog.OnColorChangeCallback{

    /**
     * 获取未授权的权限
     */
    private lateinit var permissionList:MutableList<String>

    /**
     * 请求权限的返回值
     */
    private val permissionCode = 1

    /**
     * ViewPager适配器
     */
    private lateinit var adapter: SectionsPagerAdapter

    /**
     * 当前Pager
     */
    private var pagers = 2

    /**
     * ViewPagerBottomSheetBehavior不解释
     */
    private lateinit var behavior: ViewPagerBottomSheetBehavior<*>

    /**
     * 记录BottomSheet状态
     */
    private var bottomSheetStatus = ViewPagerBottomSheetBehavior.STATE_COLLAPSED

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.qr -> {
                    pager.currentItem = 0
                    return@OnNavigationItemSelectedListener true
                }
                R.id.music -> {
                    pager.currentItem = 1
                    return@OnNavigationItemSelectedListener true
                }
                R.id.home -> {
                    pager.currentItem = 2
                    return@OnNavigationItemSelectedListener true
                }
                R.id.weather -> {
                    pager.currentItem = 3
                    return@OnNavigationItemSelectedListener true
                }
                R.id.monitor -> {
                    pager.currentItem = 4
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        setContentView(R.layout.activity_main)

         val animationSlideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom)
        animationSlideInLeft.duration = 500

            animationSlideInLeft.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {

                }

                override fun onAnimationStart(animation: Animation?) {

                }

            })

        mainLayout.startAnimation(animationSlideInLeft)

        //获取权限
        getPermission()

        initView()
    }

    private fun initView(){

        //初始化ViewPager
        adapter = SectionsPagerAdapter(supportFragmentManager)
        pager.offscreenPageLimit = 4
        setupViewPager(pager)
        pager.currentItem = 2

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = navigation.menu.getItem(2).itemId

        BottomSheetUtils.setupViewPager(pager)

        behavior = ViewPagerBottomSheetBehavior.from(pager)
        behavior.setBottomSheetCallback(object: ViewPagerBottomSheetBehavior.BottomSheetCallback(){

            override fun onStateChanged(p0: View, p1: Int) {
                /*
                 STATE_COLLAPSED： 默认的折叠状态， bottom sheets只在底部显示一部分布局。显示高度可以通过 app:behavior_peekHeight 设置（默认是0）
                 STATE_DRAGGING ： 过渡状态，此时用户正在向上或者向下拖动bottom sheet
                 STATE_SETTLING: 视图从脱离手指自由滑动到最终停下的这一小段时间
                 STATE_EXPANDED： bottom sheet 处于完全展开的状态：当bottom sheet的高度低于CoordinatorLayout容器时，整个bottom sheet都可见；或者CoordinatorLayout容器已经被bottom sheet填满。
                 STATE_HIDDEN ： 默认无此状态（可通过app:behavior_hideable 启用此状态），启用后用户将能通过向下滑动完全隐藏 bottom sheet
                 */
                when(p1){
                    ViewPagerBottomSheetBehavior.STATE_COLLAPSED ->{

                        //折叠状态使用状态栏亮色导航栏暗色
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                        window.statusBarColor = Color.TRANSPARENT

                        //如果不是在主页滑动，则向下移动壁纸
                        if (pagers != 2){
                            SharedVariable.setBehavior(false)
                            scrollListener.moveUp()
                        }

                        bottomSheetStatus = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
                    }

                    ViewPagerBottomSheetBehavior.STATE_EXPANDED ->{

                        //展开状态使用状态栏和导航栏暗色
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                        window.statusBarColor = Color.TRANSPARENT

                        //如果不是在主页滑动，则向上移动壁纸
                        if (pagers != 2){
                            SharedVariable.setBehavior(true)
                            scrollListener.moveDown()
                        }

                        bottomSheetStatus = ViewPagerBottomSheetBehavior.STATE_EXPANDED
                    }
                    ViewPagerBottomSheetBehavior.STATE_HIDDEN ->{
                        //如果向下滑动隐藏后，关闭
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    ViewPagerBottomSheetBehavior.STATE_DRAGGING ->{

                    }
                    ViewPagerBottomSheetBehavior.STATE_SETTLING ->{

                    }
                }
            }

            override fun onSlide(p0: View, p1: Float) {
                //滑动时，如果向上滑动，则联动底部导航和上部提示
                if (p1 > 0){
                    navigation_bottom.translationY = p1 * 1000
                }else{
                    navigation_bottom.translationY = -p1 * 1000
                }

                toast.translationY = -p1 * 1000
                //滑动时，如果Pager是主页，则联动壁纸一起滑动
                if (pagers == 2){
                    scrollListener.move(p1)
                }
                if (p1 < 0.3){

                }
            }
        })

        //监听页面切换
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (positionOffset > 0.3 || positionOffset < 0.3){
                    behavior.state = bottomSheetStatus
                }
            }
            override fun onPageSelected(position: Int) {

                when(position){
                    0 ->{
                        navigation.selectedItemId = R.id.qr
                        pagers = 0
                    }

                    1 ->{
                        navigation.selectedItemId = R.id.music
                        pagers = 1
                    }

                    2 ->{
                        navigation.selectedItemId = R.id.home
                        pagers = 2
                    }

                    3 ->{
                        val weatherFragment = adapter.getFragment(3) as WeatherFragment
                        weatherFragment.setClickListener(object : WeatherFragment.OnClickListener{
                            override fun onClick() {
                                changeNightMode()
                            }
                        })

                        navigation.selectedItemId = R.id.weather
                        pagers = 3
                    }

                    4 ->{
                        navigation.selectedItemId = R.id.monitor
                        pagers = 4
                    }
                }
            }
        })
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        val homeFragment = adapter.getFragment(2) as HomeFragment
        homeFragment.setHomeListener(object : HomeFragment.OnHomeListener{
            override fun onLauncherApp(launcherIntent: String) {

                if (behavior.state == ViewPagerBottomSheetBehavior.STATE_COLLAPSED){
                    behavior.state = ViewPagerBottomSheetBehavior.STATE_HIDDEN
                }
                val pkg = launcherIntent.split("#")
                val launchIntent = Intent()
                launchIntent.component = ComponentName(pkg[0], pkg[1])
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                AssistantApplication.context.startActivity(launchIntent)
                overridePendingTransition(R.anim.app_in, R.anim.app_out)

            }

        })

    }

    /**
     * 获取权限
     */
    private fun getPermission(){
        val permission = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA)
        permissionList = mutableListOf()
        permissionList.clear()

        //获取未授权的权限
        for (permit:String in permission){
            if (ContextCompat.checkSelfPermission(this@MainActivity, permit) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permit)
            }
        }

        if (!permissionList.isEmpty()){
            //请求权限方法
            val permissions = permissionList.toTypedArray()
            ActivityCompat.requestPermissions(this@MainActivity, permissions, permissionCode)
        }
    }


    override fun onBackPressed() {
        //如果未折叠，先折叠
        when {
            behavior.state == ViewPagerBottomSheetBehavior.STATE_EXPANDED -> behavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
            behavior.state == ViewPagerBottomSheetBehavior.STATE_COLLAPSED -> behavior.state = ViewPagerBottomSheetBehavior.STATE_HIDDEN
            else -> {
                finish()
                overridePendingTransition(android.R.anim.fade_in, R.anim.fade_out)
            }
        }
    }

    //向ViewPager适配器里添加Fragment
    private fun setupViewPager(viewPager: ViewPager) {
        adapter.addFragment(QRFragment(),"")
        adapter.addFragment(MusicFragment(),"")
        adapter.addFragment(HomeFragment(),"")
        adapter.addFragment(WeatherFragment(),"")
        adapter.addFragment(MonitorFragment(),"")
        viewPager.adapter = adapter
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter (fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }

        fun getFragment(position: Int): Fragment {
            return mFragmentList[position]
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            permissionCode ->{
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"请转到设置授予权限，否则无法正常使用",Toast.LENGTH_SHORT).show()
                }
            }
            else ->{}
        }
    }

    //更新主题色的回调
    override fun onSkinUpdate(skin: Skin) {
//        tabLayout.setTabIndicatorColor(skin.colorAccent)
    }

    override fun OnChanged(color: Int) {
        AppSettings.put(this,R.string.key_color_primary,color)
        AppSettings.put(this,R.string.key_color_accent,color)
        requestUpdateSkin()
    }

    //滑动回调
    interface ScrollListener{
        fun move(p1: Float)
        fun moveDown()
        fun moveUp()
    }

    private lateinit var scrollListener: ScrollListener

    fun setScrollListener(scrollListener: ScrollListener){
        this.scrollListener = scrollListener
    }
}