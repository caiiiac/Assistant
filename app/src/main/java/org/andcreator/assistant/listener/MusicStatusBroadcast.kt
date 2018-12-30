package org.andcreator.assistant.listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.andcreator.assistant.util.LoopType


/**
 * @date: 2018/09/08 20:11
 * @author: lollipop
 * 音乐状态变化的监听器
 */
class MusicStatusBroadcast(private val callback: MusicStatusBroadcast.Callback): BroadcastReceiver() {

    companion object {
        private const val MUSIC_ID = "MUSIC_ID"
        private const val CURSOR_ID = "CURSOR_ID"
        private const val LOOP_TYPE = "LOOP_TYPE"
        private const val PROGRESS = "PROGRESS"
        private const val ERROR_CODE = "ERROR_CODE"
        private const val ERROR_VALUE = "ERROR_VALUE"
        private const val MUSIC_STATUS = "MUSIC_STATUS"
        private const val LIKE_TYPE = "LIKE_TYPE"

        const val ACTION = "Music_Status_Broadcast"

        fun create(context: Context, status: MusicStatus): Builder{
            return Builder(context, status)
        }

    }

    fun register(context: Context){
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION)
        context.registerReceiver(this,intentFilter)
    }

    fun unregister(context: Context){
        context.unregisterReceiver(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null){
            val status = intent.getIntExtra(MUSIC_STATUS,MusicStatus.UNKNOWN.value).parseMusicStatus()
            callback.onMusicStatusChange(status, intent.parseInfo())
        }
    }

    private fun Intent.parseInfo(): Info{
        return Info().apply{
            musicId = getLongExtra(MUSIC_ID, 0L)
            cursorId = getLongExtra(CURSOR_ID, 0L)
            loopType = getIntExtra(LOOP_TYPE, LoopType.List.value).parseLoopType()
            progress = getFloatExtra(PROGRESS,0F)
            errorCode = getIntExtra(ERROR_CODE, 0)
            errorValue = getStringExtra(ERROR_VALUE)
            isLike = getBooleanExtra(LIKE_TYPE, false)
        }
    }

    private fun Int.parseMusicStatus(): MusicStatus{
        return MusicStatus.parse(this)
    }

    private fun Int.parseLoopType(): LoopType{
        return LoopType.parse(this)
    }

    interface Callback{
        fun onMusicStatusChange(newStatus: MusicStatus, info: Info)
    }

    class Info{
        var musicId = 0L
        var cursorId = 0L
        var loopType = LoopType.List
        var progress = 0.0F
        var errorCode = 0
        var errorValue = ""
        var isLike = false
    }

    class Builder(private val context: Context, private val status: MusicStatus){
        private var loopType = LoopType.List
        private var progress = 0.0F
        private var errorCode = 0
        private var errorValue = ""
        private var musicId = 0L
        private var cursorId  = 0L
        private var isLike = false

        fun loopType(type: LoopType): Builder{
            loopType = type
            return this
        }
        fun progress(pro: Float): Builder{
            progress = pro
            return this
        }
        fun errorCode(code: Int): Builder{
            errorCode = code
            return this
        }
        fun errorValue(value: String): Builder{
            errorValue = value
            return this
        }
        fun musicId(id: Long): Builder{
            musicId = id
            return this
        }
        fun cursorId(id: Long): Builder{
            cursorId = id
            return this
        }
        fun isLike(type: Boolean): Builder{
            isLike = type
            return this
        }

        fun send(){
            val intent = Intent(ACTION)
            intent.putExtra(MUSIC_ID,musicId)
            intent.putExtra(CURSOR_ID,cursorId)
            intent.putExtra(MUSIC_STATUS,status.value)
            intent.putExtra(LOOP_TYPE,loopType.value)
            intent.putExtra(PROGRESS,progress)
            intent.putExtra(ERROR_CODE,errorCode)
            intent.putExtra(ERROR_VALUE,errorValue)
            intent.putExtra(LIKE_TYPE,isLike)
            context.sendBroadcast(intent)
        }
    }

}