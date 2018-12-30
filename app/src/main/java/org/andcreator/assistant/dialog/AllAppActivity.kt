package org.andcreator.assistant.dialog

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_all_app.*
import org.andcreator.assistant.R
import org.andcreator.assistant.SharedVariable
import org.andcreator.assistant.adapter.AppsItemAdapter
import org.andcreator.assistant.bean.AppsItemBean
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.support.annotation.NonNull




class AllAppActivity : AppCompatActivity() {

    private val appsList =  ArrayList<AppsItemBean>()
    private lateinit var adapter: AppsItemAdapter
    private lateinit var calculateThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_app)
        val m = windowManager
        val d = m.defaultDisplay //为获取屏幕宽高
        val p = window.attributes
//        p.height = (int) (d.getHeight()*0.9); //高度设置为屏幕的0.8
        p.width = d.width * 1 //宽度设置为屏幕的0.8
        window.attributes = p
        initView()
    }

    private fun initView(){
        val gridLayoutManager = GridLayoutManager(this,5)
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL
        allApps.layoutManager = gridLayoutManager
        adapter =  AppsItemAdapter(this,appsList)

        adapter.setLongClickListener(object : AppsItemAdapter.OnItemLongClickListener{
            override fun onClick(name: String,icon: Drawable, pkgName: String, activityName: String) {
                //保存图标
                SharedVariable.setDrawable(icon)
                calculateThread = object : Thread() {
                    override fun run() {
                        super.run()
                        saveIcon(icon, "$pkgName.png")
                    }
                }
                calculateThread.start()

                val intent = Intent()
                intent.putExtra("name",name)
                intent.putExtra("pkgName",pkgName)
                intent.putExtra("activityName",activityName)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }

        })

        allApps.adapter = adapter

        loadApps()
    }

    private fun loadApps() {

        doAsync {

            appsList.clear()

            val pm = packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN,null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos = pm.queryIntentActivities(mainIntent,0)
            // 调用系统排序 ， 根据name排序
            // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
            Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(pm))
            for (reInfo: ResolveInfo in resolveInfos){
                val activityName = reInfo.activityInfo.name // 获得该应用程序的启动Activity的name
                val pkgName = reInfo.activityInfo.packageName // 获得应用程序的包名
                val appLabel = reInfo.loadLabel(pm) as String // 获得应用程序的Label
                val icon = reInfo.loadIcon(pm) // 获得应用程序图标
                // 为应用程序的启动Activity 准备Intent
                val launchIntent = Intent()
                launchIntent.component = ComponentName(
                    pkgName,
                    activityName
                )
                // 创建一个AppInfo对象，并赋值
                appsList.add(AppsItemBean(appLabel,icon,launchIntent,pkgName,activityName)) // 添加至列表中
            }

            uiThread {
                loading.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun saveIcon(icon: Drawable,name: String) {

        val bmp = getBitmapFromDrawable(icon)

        val file = File(filesDir,name)
        val out = FileOutputStream(file)
        try {
            if (!file.exists()) {
                val files = File(file.parent)
                files.mkdirs()
                file.createNewFile()
            }

            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            out.flush()
            out.close()
        }

    }


    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }
}
