package org.andcreator.assistant.util

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.support.annotation.FloatRange
import android.util.Log
import org.andcreator.assistant.bean.MusicBean
import org.andcreator.assistant.listener.MusicException


/**
 * @date: 2018/8/25 09:47
 * @author: lollipop
 * 播放辅助器
 */
class PlayerHelper(private val callback: Callback) {

    private val mediaPlayer = MediaPlayer()
    private val musicData = MusicBean()

    private val audioAttributesBuild = AudioAttributes.Builder()

    var isReady = false
        private set

    companion object {

        const val ERROR_NOT_READY = "ERROR: player not ready"

    }

    init {
        /**
         * 播放器发生异常时的回调函数
         */
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            callback.onError(what, extra)
            true
        }
        /**
         * 播放器播放结束时的回调函数
         */
        mediaPlayer.setOnCompletionListener {
            callback.onCompletion()
            Log.e("播放结束","播放结束")
        }

        /**
         * 警告或者提示信息的回调函数
         */
        mediaPlayer.setOnInfoListener { mp, what, extra ->
            callback.onInfoListener(what, extra)
            true
        }

        /**
         * 当媒体源准备就绪时候的回调函数
         */
        mediaPlayer.setOnPreparedListener {
            isReady = true
            callback.onPrepared()
        }

        /**
         * 当跳转进度完成时触发
         */
        mediaPlayer.setOnSeekCompleteListener {
            callback.onSeekComplete()
        }

        //设置默认播放类型为音乐
        setPlayType(PlayType.MUSIC)

        //默认用于媒体
        setUsage(UsageType.MEDIA)

    }

    /**
     * 设置数据
     */
    fun setData(bean: MusicBean){
        try {
            musicData.copy(bean)
            mediaPlayer.setDataSource(musicData.path)
        } catch (e: Exception){
            callback.onPlayerError("setData()", e)
        }
    }

    /**
     * 设置播放的媒体类型
     */
    fun setPlayType(type: PlayType){
        audioAttributesBuild.setContentType(type.value)
    }

    /**
     * 设置用途
     */
    fun setUsage(type: UsageType){
        audioAttributesBuild.setUsage(type.value)
    }

    enum class PlayType(val value: Int){
        /**
         * 未知类型
         */
        UNKNOWN(AudioAttributes.CONTENT_TYPE_UNKNOWN),
        /**
         * 语音
         */
        SPEECH(AudioAttributes.CONTENT_TYPE_SPEECH),
        /**
         * 音乐
         */
        MUSIC(AudioAttributes.CONTENT_TYPE_MUSIC),
        /**
         * 视频
         */
        MOVIE(AudioAttributes.CONTENT_TYPE_MOVIE),
        /**
         * 动作，比如点击音效
         */
        SONIFICATION(AudioAttributes.CONTENT_TYPE_SONIFICATION)
    }

    enum class UsageType(val value: Int){
        /**
         * 未知类型用途
         */
        UNKNOWN(AudioAttributes.USAGE_UNKNOWN),
        /**
         * 用于媒体播放
         */
        MEDIA(AudioAttributes.USAGE_MEDIA),
        /**
         * 用于语音通话类
         */
        VOICE_COMMUNICATION(AudioAttributes.USAGE_VOICE_COMMUNICATION),
        /**
         * 通信中的提示音，比如忙音
         */
        VOICE_COMMUNICATION_SIGNALLING(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING),
        /**
         * 提醒时用的类型，如警告音
         */
        ALARM(AudioAttributes.USAGE_ALARM),
        /**
         * 消息通知类
         */
        USAGE_NOTIFICATION(AudioAttributes.USAGE_NOTIFICATION),
        /**
         * 电话铃声使用的类型
         */
        NOTIFICATION_RINGTONE(AudioAttributes.USAGE_NOTIFICATION_RINGTONE),
        /**
         * 通话开始或者结束时使用的类型
         */
        NOTIFICATION_COMMUNICATION_REQUEST(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST),
        /**
         * 即时通讯时的提示音，如IM类
         */
        NOTIFICATION_COMMUNICATION_INSTANT(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT),
        /**
         * 延时通讯类的提示音，比如电子邮件
         */
        NOTIFICATION_COMMUNICATION_DELAYED(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_DELAYED),
        /**
         * 引起用户注意的提示音，如电量不足
         */
        NOTIFICATION_EVENT(AudioAttributes.USAGE_NOTIFICATION_EVENT),
        /**
         * 用于辅助功能的语音提醒
         */
        ASSISTANCE_ACCESSIBILITY(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY),
        /**
         * 用于导航时
         */
        ASSISTANCE_NAVIGATION_GUIDANCE(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE),
        /**
         * 用于用户界面的声音，不理解具体含义
         */
        ASSISTANCE_SONIFICATION(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION),
        /**
         * 用于游戏
         */
        GAME(AudioAttributes.USAGE_GAME),
        /**
         * 语音指令的反馈声音
         */
        USAGE_ASSISTANT(AudioAttributes.USAGE_ASSISTANT)
    }

    /**
     * 是否真正播放的状态位
     */
    val isPlaying: Boolean
        get() = mediaPlayer.isPlaying

    /**
     * 设置左右声道的音量比，
     * 取值范围是0~1.
     */
    fun setVolume(@FloatRange(from = 0.0,to = 1.0) leftVolume: Float,
                  @FloatRange(from = 0.0,to = 1.0) rightVolume: Float){
        try {
            mediaPlayer.setVolume(leftVolume, rightVolume)
        }catch (e: Exception){
            callback.onPlayerError("setVolume($leftVolume,$rightVolume)", e)
        }
    }

    /**
     * 是否循环播放的状态位，
     * 用来判断和设置是否循环
     */
    var isLooping: Boolean
        get() = mediaPlayer.isLooping
        set(value) {mediaPlayer.isLooping = value}

    /**
     * 停止播放
     */
    fun stop(){
        if(!isReady){
            callback.onPlayerError("stop()", MusicException(ERROR_NOT_READY))
            return
        }
        isReady = false
        try {
            mediaPlayer.stop()
            mediaPlayer.reset()
        }catch (e: Exception){
            callback.onPlayerError("stop()", e)
        }
    }

    /**
     * 暂停播放
     */
    fun pause(){
        if(!isReady){
            callback.onPlayerError("pause()", MusicException(ERROR_NOT_READY))
            return
        }
        try {
            mediaPlayer.pause()
        }catch (e: Exception){
            callback.onPlayerError("pause()", e)
        }
    }

    /**
     * 开始播放
     */
    fun start(){
        if(!isReady){
            callback.onPlayerError("start()", MusicException(ERROR_NOT_READY))
            return
        }
        try {
            mediaPlayer.start()
        }catch (e: Exception){
            callback.onPlayerError("start()", e)
        }
    }

    /**
     * 释放资源。
     * 当播放停止时，使用此方法释放播放器资源
     */
    fun release(){
        isReady = false
        try {
            mediaPlayer.release()
        }catch (e: Exception){
            callback.onPlayerError("release()", e)
        }
    }

    /**
     * 跳转到指定毫秒数
     */
    fun seekTo(ms: Int){
        if(!isReady){
            return
        }
        try {
            mediaPlayer.seekTo(ms)
        }catch (e: Exception){
            callback.onPlayerError("seekTo($ms)", e)
        }
    }

    /**
     * 选择指定音轨。
     * 默认会选中第一个音轨
     * 如果已经播放，那么立即播放指定音轨。
     * 如果没有播放，那么立即标记指定音轨。
     * 如果多次选中，那么最后一次选中生效
     * 如果没有初始化，那么会发生错误。
     * 如果是定时文本（不理解），那么必须手动选择
     */
    fun selectTrack(index: Int){
        try {
            mediaPlayer.selectTrack(index)
        }catch (e: Exception){
            callback.onPlayerError("selectTrack($index)", e)
        }
    }

    /**
     * 取消选中一条音轨
     * 目前，轨道必须是定时文本轨道，并且不能取消选择音频或视频轨道
     */
    fun deselectTrack(index: Int){
        try {
            mediaPlayer.deselectTrack(index)
        }catch (e: Exception){
            callback.onPlayerError("deselectTrack($index)", e)
        }
    }

    /**
     * 获取当前播放文件的音轨信息
     * 轨道信息数组。 轨道总数是阵列长度。
     * 如果在调用任何addTimedTextSource方法之后添加了外部定时文本源，则必须再次调用。
     */
    fun getTrackInfo(): Array<MediaPlayer.TrackInfo?>{
        return try {
            mediaPlayer.trackInfo
        }catch (e: Exception){
            callback.onPlayerError("getTrackInfo()", e)
            arrayOfNulls(0)
        }
    }

    /**
     * 设置下一个媒体播放器，可以再setData后的任何时候
     * 但是如果设置的循环，那么不会触发下一个播放器
     * 当当前播放器播放结束时，将尽可能自然的过渡到下一个歌曲
     */
    fun setNextMediaPlayer(player: MediaPlayer?){
        try {
            mediaPlayer.setNextMediaPlayer(player)
        }catch (e: Exception){
            callback.onPlayerError("setNextMediaPlayer(${player != null})", e)
        }
    }

    /**
     * 播放文件的持续时间
     * 单位为ms
     */
    val duration: Int
        get() = mediaPlayer.duration

    /**
     * 当前播放的位置，
     * 单位是ms
     */
    val currentPosition: Int
        get() = mediaPlayer.currentPosition

    /**
     * 异步加载媒体数据
     * 不提供同步方式
     */
    fun prepare(){
        if(isReady){
            stop()
        }
        isReady = false
        //加载媒体数据前设置数据类型
        setAudioAttributes(audioAttributesBuild.build())
        try {
            mediaPlayer.prepareAsync()
        }catch (e: Exception){
            callback.onPlayerError("prepare()", e)
        }
    }

    /**
     * 设置媒体播放属性
     * 必须在加载媒体数据前设置属性
     */
    private fun setAudioAttributes(attributes: AudioAttributes){
        try {
            mediaPlayer.setAudioAttributes(attributes)
        }catch (e: Exception){
            callback.onPlayerError("prepare()", e)
        }
    }

    interface Callback{
        /**
         * 当调用方法发生异常时
         */
        fun onPlayerError(funName: String, e: Exception)

        /**
         * 当播放器主动抛出异常时
         */
        fun onError(what: Int, extra: Int)

        /**
         * 当播放结束时
         */
        fun onCompletion()

        /**
         * 警告或者提示信息
         */
        fun onInfoListener(what: Int, extra: Int)

        /**
         * 当准备就绪时
         */
        fun onPrepared()

        /**
         * 当进度跳转完成时
         */
        fun onSeekComplete()
    }

}