package org.andcreator.assistant.fragment


import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_home.*
import org.andcreator.assistant.AssistantApplication

import org.andcreator.assistant.R
import org.andcreator.assistant.SharedVariable
import org.andcreator.assistant.activity.MainActivity
import org.andcreator.assistant.activity.SettingsActivity
import org.andcreator.assistant.bean.AssistantBean
import org.andcreator.assistant.dialog.AllAppActivity
import org.andcreator.assistant.layout.CellLayout
import org.andcreator.assistant.listener.LoadCompleted
import org.andcreator.assistant.listener.ResizeWidgetListener
import org.andcreator.assistant.skin.Skin
import org.andcreator.assistant.skin.SkinFragment
import org.andcreator.assistant.util.DatabaseUtil
import org.andcreator.assistant.util.DisplayUtil
import org.andcreator.assistant.popup.HomePopupWindow
import org.andcreator.assistant.popup.RemoveCellPopupWindow
import org.andcreator.assistant.util.OtherUtils
import org.andcreator.assistant.widget.WidgetHost
import org.andcreator.assistant.widget.WidgetView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.util.ArrayList


/**
 * A simple [Fragment] subclass.
 *
 */
class HomeFragment : SkinFragment(), LoadCompleted,ResizeWidgetListener {

    private lateinit var mAppWidgetHost: WidgetHost
    private lateinit var mAppWidgetManager: AppWidgetManager
    private lateinit var popupHome: HomePopupWindow
    private lateinit var popupCell: RemoveCellPopupWindow
    private val appItemCode = 1
    private lateinit var launcherDBOperate: DatabaseUtil.LauncherDBOperate
    private val cellData: ArrayList<AssistantBean> = ArrayList()

    override fun setContentView(): Int {
        return R.layout.fragment_home
    }

    override fun lazyLoad() {

        launcherDBOperate = DatabaseUtil.writeLauncher(context!!)
        initView()
    }

    override fun onSkinUpdate(skin: Skin) {

    }

    private fun initView(){

        val resources = this.resources
        val dm = resources.displayMetrics
        val height = dm.heightPixels
        val offset = -(height - DisplayUtil.dip2px(context,480F).toFloat())

        wallpaper.translationY = offset
        val mainActivity: MainActivity = activity as MainActivity
        mainActivity.setScrollListener(object : MainActivity.ScrollListener{
            override fun moveDown() {
                wallpaper.translationY = 0F
            }

            override fun moveUp() {
                wallpaper.translationY = offset
            }

            override fun move(p1: Float) {
                wallpaper.translationY = offset-offset*p1
            }

        })

        //先加载壁纸
        Glide.with(this).load(R.drawable.wallpaper_3).into(wallpaper)

        //初始化Launcher控件
        mAppWidgetManager = AppWidgetManager.getInstance(activity!!.applicationContext)
        mAppWidgetHost = WidgetHost(activity!!.applicationContext, 0xfffff)
        //开始监听widget的变化
        mAppWidgetHost.startListening()
        popupHome = HomePopupWindow(activity!!)
        popupCell = RemoveCellPopupWindow(activity!!)

        cellHome.setOnLongClickListener {
            popupHome.showAtLocation(homeScreen, Gravity.TOP or Gravity.START,OtherUtils.getScreenWidth(activity!!)/4,(OtherUtils.getScreenHeight(activity!!)/2.4).toInt())
            true
        }

        popupHome.setClickListener(object : HomePopupWindow.OnItemClickListener{
            override fun onClick(position: Int) {
                when(position){
                    1 ->{
                        startActivityForResult(Intent(context,AllAppActivity::class.java),appItemCode)
                    }
                    2 ->{
                        showWidgetChooser()
                    }
                    4 ->{
                        startActivityForResult(Intent(context,SettingsActivity::class.java),appItemCode)
                    }
                }
            }
        })

        popupCell.setClickListener(object : RemoveCellPopupWindow.OnItemClickListener{
            override fun onClick(id: String) {
                cellHome.removeCell(id)
            }
        })

        //监听小部件的拖拽
        cellHome.setLoadCompleted(this)
        //监听小部件大小调节
        dragLayer.setLocationListener(this)
        //载入Launcher
        loadLauncher()
    }

    /**
     * 加载Launcher
     */
    private fun loadLauncher(){
        doAsync {

            launcherDBOperate.selectAll(cellData)

            uiThread {
                for (cells in cellData){
                    when(cells.launcherType){
                        AssistantBean.APP ->loadApps(cells)
                        AssistantBean.WIDGET ->loadWidget(cells)
                    }
                }

                cellHome.invalidateCell()
            }
        }
    }

    //载入App
    @SuppressLint("CheckResult")
    private fun loadApps(appBean: AssistantBean){
        val icon = ImageView(activity)
        val params = ViewGroup.LayoutParams(100, 100)
        icon.layoutParams = params
        icon.setPadding(DisplayUtil.dip2px(context,14F),DisplayUtil.dip2px(context,14F),DisplayUtil.dip2px(context,14F),DisplayUtil.dip2px(context,14F))
        icon.scaleType = ImageView.ScaleType.FIT_CENTER

        val file = File(context!!.filesDir,appBean.launcherIcon)
        val iconUri = FileProvider.getUriForFile(context!!, "${context!!.packageName}.provider", file)
        icon.setImageURI(iconUri)

        val cell = CellLayout.Cell(appBean.launcherId,"app", icon,appBean.launcherIntent)
        cell.widthNum = appBean.launcherWidth
        cell.heightNum = appBean.launcherHeight
        cell.expectColumnIndex = appBean.launcherX
        cell.expectRowIndex = appBean.launcherY
        cellHome.addCell(cell)

    }

    //载入Widget
    private fun loadWidget(widgetBean: AssistantBean){

        val appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetBean.launcherWidgetId)
        val hostView:WidgetView =
            mAppWidgetHost.createView(AssistantApplication.context, widgetBean.launcherWidgetId, appWidgetInfo) as WidgetView
        val params = ViewGroup.LayoutParams(appWidgetInfo.minWidth, appWidgetInfo.minHeight)
        hostView.layoutParams = params

        val cell = CellLayout.Cell(widgetBean.launcherId,"widget", hostView,null)
        cell.widthNum = widgetBean.launcherWidth
        cell.heightNum = widgetBean.launcherHeight
        cell.expectColumnIndex = widgetBean.launcherX
        cell.expectRowIndex = widgetBean.launcherY
        cellHome.addCell(cell)
    }

    /**
     * 添加App图标
     */
    private fun addAppIcon(icon: Drawable,id: String,launcherIntent: String){
        val imageView = ImageView(activity)
        val params = ViewGroup.LayoutParams(100, 100)
        imageView.layoutParams = params
        imageView.setImageDrawable(icon)
        imageView.setPadding(DisplayUtil.dip2px(context,14F),DisplayUtil.dip2px(context,14F),DisplayUtil.dip2px(context,14F),DisplayUtil.dip2px(context,14F))
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        val cell = CellLayout.Cell(id,"app", imageView,launcherIntent)
        cellHome.newCell(cell)
    }

    private fun showWidgetChooser() {
        val appWidgetId = mAppWidgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        startActivityForResult(pickIntent, 0xaa)
    }

    /**
     * 选取小部件和App返回数据
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                0xaa -> addAppWidget(data!!)
                0xbb -> completeAddAppWidget(data!!)
                appItemCode ->{
                    //程序名称
                    val name: String = data!!.getStringExtra("name")
                    //程序包名
                    val pkgName: String = data.getStringExtra("pkgName")
                    //程序Activity明
                    val activityName: String = data.getStringExtra("activityName")
                    //程序图标
                    val icon = SharedVariable.getDrawable()

                    //保存到数据库
                    val appBean = AssistantBean()
                    appBean.launcherType = AssistantBean.APP
                    appBean.launcherName = name
                    appBean.launcherIcon = "$pkgName.png"
                    appBean.launcherX = 0
                    appBean.launcherY = 0
                    appBean.launcherWidth = 1
                    appBean.launcherHeight = 1
                    appBean.launcherIntent = "$pkgName#$activityName"

                    //添加到桌面
                    if (icon != null){
                        addAppIcon(icon,launcherDBOperate.install(appBean).toString(),"$pkgName#$activityName")
                    }
                }
            }
        } else if (requestCode == 0xaa && resultCode == Activity.RESULT_CANCELED && data != null) {
            // Clean up the appWidgetId if we canceled
            val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId)
            }
        }
    }

    /**
     * 添加小部件并加载配置
     */
    private fun addAppWidget(data: Intent) {
        val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

        val customWidget = data.getStringExtra("custom_widget")
        if ("search_widget" == customWidget) {
            //这里直接将search_widget删掉了
            mAppWidgetHost.deleteAppWidgetId(appWidgetId)
        } else {
            val appWidget = mAppWidgetManager.getAppWidgetInfo(appWidgetId)

            if (appWidget.configure != null) {
                //有配置，弹出配置
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                intent.component = appWidget.configure
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

                startActivityForResult(intent, 0xbb)
            } else {
                //没有配置，直接添加
                completeAddAppWidget(data)
            }
        }
    }

    /**
     * 添加widget
     *
     * @param data
     */
    private fun completeAddAppWidget(data: Intent) {
        val extras = data.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId)
        val hostView:WidgetView =
            mAppWidgetHost.createView(AssistantApplication.context, appWidgetId, appWidgetInfo) as WidgetView
        val params = ViewGroup.LayoutParams(appWidgetInfo.minWidth, appWidgetInfo.minHeight)
        hostView.layoutParams = params
        //保存到数据库
        val widgetBean = AssistantBean()
        widgetBean.launcherType = AssistantBean.WIDGET
        widgetBean.launcherX = 0
        widgetBean.launcherY = 0
        widgetBean.launcherWidth = 0
        widgetBean.launcherHeight = 0
        widgetBean.launcherWidgetId = appWidgetId

        //添加到Launcher
        val cell = CellLayout.Cell(launcherDBOperate.install(widgetBean).toString(),"widget", hostView,null)
        cellHome.newCell(cell)
    }

    override fun Completed(cell: CellLayout.Cell,widget: View) {
        dragLayer.addResizeFrame(this.activity!!,widget,cell)
    }

    override fun onMove(id: String) {
        dragLayer.moveResizeFrame(id)
    }

    override fun onDragStarted(id: String,locationX: Int,locationY: Int) {
        popupCell.showAtLocation(homeScreen, Gravity.TOP or Gravity.START,locationX,locationY)
        popupCell.setId(id)
        dragLayer.showResizeFrame(id)
    }

    override fun onDragLocation() {
        popupCell.dismiss()
    }

    override fun onRemoveResizeFrame(id: String) {
        dragLayer.removeResizeFrame(id)
    }

    override fun onLauncherApp(launcherIntent: String) {
        homeListener.onLauncherApp(launcherIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAppWidgetHost.stopListening()

        launcherDBOperate.close()
    }

    /**
     * 调节小部件大小
     * @param widgetId 小部件Id
     * @param deltaX 当前拖动的X坐标
     * @param deltaY 当前拖动的Y坐标
     * @param direction 当前拖动方向
     */
    override fun onMove(widgetId: String,deltaX: Int, deltaY: Int,direction: Int) {
        cellHome.resizeCell(widgetId,deltaX,deltaY,direction)
    }

    override fun onUp() {
        cellHome.invalidateCell()
        Log.e("onLayout","invalidateCell")
    }

    interface OnHomeListener{
        fun onLauncherApp(launcherIntent: String)
    }
    private lateinit var homeListener: OnHomeListener

    fun setHomeListener(homeListener: OnHomeListener) {
        this.homeListener = homeListener
    }

}
