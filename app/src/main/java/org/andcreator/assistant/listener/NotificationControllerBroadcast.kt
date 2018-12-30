package org.andcreator.assistant.listener


import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * @date: 2018/10/06 15:13
 * @author: lollipop
 *
 */
class NotificationControllerBroadcast(private val callback: NotificationCallback): BroadcastReceiver() {

    companion object {
        private const val MUSIC_SKIP_PREVIOUS = "MUSIC_SKIP_PREVIOUS"
        private const val MUSIC_PAUSE = "MUSIC_PAUSE"
        private const val MUSIC_PLAY = "MUSIC_PLAY"
        private const val MUSIC_SKIP_NEXT = "MUSIC_SKIP_NEXT"

        private const val PENDING_REQUEST_CODE_SKIP_PREVIOUS = 100
        private const val PENDING_REQUEST_CODE_PAUSE = 101
        private const val PENDING_REQUEST_CODE_PLAY = 102
        private const val PENDING_REQUEST_CODE_SKIP_NEXT = 103

        fun pendingPrevious(context: Context): PendingIntent{
            return pendingTo(context,MUSIC_SKIP_PREVIOUS)
        }

        fun pendingPause(context: Context): PendingIntent{
            return pendingTo(context,MUSIC_PAUSE)
        }

        fun pendingPlay(context: Context): PendingIntent{
            return pendingTo(context,MUSIC_PLAY)
        }

        fun pendingNext(context: Context): PendingIntent{
            return pendingTo(context,MUSIC_SKIP_NEXT)
        }

        private fun pendingTo(context: Context,action: String): PendingIntent{
            val code = when(action){
                MUSIC_SKIP_PREVIOUS -> {
                    PENDING_REQUEST_CODE_SKIP_PREVIOUS
                }
                MUSIC_PAUSE -> {
                    PENDING_REQUEST_CODE_PAUSE
                }
                MUSIC_PLAY -> {
                    PENDING_REQUEST_CODE_PLAY
                }
                MUSIC_SKIP_NEXT -> {
                    PENDING_REQUEST_CODE_SKIP_NEXT
                }
                else -> throw MusicException("Unknown action")
            }
            return PendingIntent.getBroadcast(context,code,
                Intent(action),PendingIntent.FLAG_UPDATE_CURRENT)
        }

    }

    fun register(context: Context){
        val intentFilter = IntentFilter()
        intentFilter.addAction(MUSIC_SKIP_PREVIOUS)
        intentFilter.addAction(MUSIC_PAUSE)
        intentFilter.addAction(MUSIC_PLAY)
        intentFilter.addAction(MUSIC_SKIP_NEXT)
        context.registerReceiver(this,intentFilter)
    }

    fun unregister(context: Context){
        context.unregisterReceiver(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            MUSIC_SKIP_PREVIOUS -> {
                callback.callPrevious()
            }
            MUSIC_PAUSE -> {
                callback.callPause()
            }
            MUSIC_PLAY -> {
                callback.callStart()
            }
            MUSIC_SKIP_NEXT -> {
                callback.callNext()
            }
        }
    }

}