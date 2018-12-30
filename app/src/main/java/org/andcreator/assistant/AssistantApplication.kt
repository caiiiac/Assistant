package org.andcreator.assistant

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import org.andcreator.assistant.skin.Skin
import org.andcreator.assistant.skin.SkinActivity
import org.andcreator.assistant.util.AppSettings
import java.util.ArrayList

@SuppressLint("Registered")
class AssistantApplication: Application(),Application.ActivityLifecycleCallbacks{

    private val activities = ArrayList<Activity>()


    companion object {

        val appSkin: Skin = Skin()
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        registerActivityLifecycleCallbacks(this)

        onSkinChange()
    }

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
        if(activity != null){
            activities.remove(activity)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        if(activity != null){
            activities.add(activity)
        }
    }

    private fun requestUpdateSkin(){
        for(activity in activities){
            if(activity is SkinActivity){
                activity.updateSkin(appSkin)
            }
        }
    }

    fun onSkinChange(){
        AppSettings.updateSkin(appSkin,this)
        requestUpdateSkin()
    }

}