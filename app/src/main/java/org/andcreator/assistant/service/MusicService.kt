package org.andcreator.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.graphics.Palette
import android.util.Log
import org.andcreator.assistant.R
import org.andcreator.assistant.bean.MusicBean
import org.andcreator.assistant.listener.*
import org.andcreator.assistant.constant.MusicConstant
import org.andcreator.assistant.util.LoopType
import org.andcreator.assistant.util.PlayerHelper
import java.util.*

class MusicService : Service(),
MusicServiceBinder.ServiceCallback,
PlayerHelper.Callback, NotificationCallback {

    private val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
            or PlaybackStateCompat.ACTION_PAUSE
            or PlaybackStateCompat.ACTION_PLAY_PAUSE
            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            or PlaybackStateCompat.ACTION_STOP
            or PlaybackStateCompat.ACTION_SEEK_TO)

    companion object {
        private const val FOREGROUND_CODE = 1040
        private const val MEDIA_SESSION_TAG = "Music"
    }

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager

    private val binderList = ArrayList<MusicServiceBinder>()

    private var notificationColor = Color.TRANSPARENT

    private lateinit var editor: SharedPreferences.Editor

    /**
     * 默认的封面图
     */
    private var defaultLargeIcon: Bitmap? = null

    /**
     * 播放辅助类
     */
    private val playerHelper = PlayerHelper(this)

    /**
     * 播放列表
     */
    private val musicList = ArrayList<MusicBean>()

    /**
     * 当前播放音乐的列表
     */
    private var playingMusicIndex = -1

    /**
     * 循环模式
     */
    private var loopType = LoopType.List

    /**
     * 消息控制台 广播接收器
     * 用于接收消息中按钮点击的事件
     */
    private val notificationControllerBroadcast = NotificationControllerBroadcast(this)

    /**
     * 当前音乐的封面图片
     */
    private var coverBitmap: Bitmap? = null

    /**
     * 当前播放音乐封面的key
     * 用于区别音乐是否变化，如果变化则重新获取封面图片
     */
    private var coverKey = ""

    /**
     * log输出的方法
     */
    private fun log(value: String){
        Log.e("MusicService", value)
    }

    /**
     * 播放状态
     */
    private var playerStatus = MusicStatus.UNKNOWN
    /**
     * 正在播放的音乐信息
     */
    private var musicBean: MusicBean? = null

    private var position = 0

    /**
     * 初始化方法
     */
    override fun onCreate() {
        super.onCreate()
        createChannel()
        mediaSession = MediaSessionCompat(this,MEDIA_SESSION_TAG)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationControllerBroadcast.register(this)
        editor = PreferenceManager.getDefaultSharedPreferences(this).edit()

    }

    override fun onBind(intent: Intent): IBinder {
        log("onBind($intent)")
        val binder = MusicServiceBinder(this)
        binderList.add(binder)
        return binder
    }


    /**
     * 请求播放
     */
    override fun callStart() {
        log("callStart()")
        if(!canPlay()){
            return
        }
        log("callStart() - true")
        if(playerStatus == MusicStatus.WAITING){
            log("callStart() - playerHelper.prepare()")
            playerHelper.prepare()
            createStatusBroadcast(MusicStatus.LOADING).send()
            updateNotification()
        }else if(playerHelper.isReady){
            log("callStart() - start")
            playerHelper.start()
            createStatusBroadcast(MusicStatus.START).send()
            createStatusBroadcast(MusicStatus.PLAYING).send()
            updateNotification()
        }
    }

    /**
     * 请求暂停
     */
    override fun callPause() {
        log("callPause()")
        if(!canPlay()){
            return
        }
        log("callPause() - true")
        if(isPlaying()){
            playerHelper.pause()
        }
        createStatusBroadcast(MusicStatus.PAUSE).send()
        updateNotification()
        stopForeground(false)

        if (musicBean != null){

            editor.putLong("musicId",musicBean!!.cursorId)
            editor.putString("musicName",musicBean!!.fileName)
            editor.putInt("musicSeek",position)
            editor.apply()
        }

    }

    /**
     * 请求停止
     */
    override fun callStop() {
        log("callStop()")
        if(!canPlay()){
            return
        }
        if(playerHelper.isReady){
            log("callStop() - true")
            playerHelper.stop()
        }
        createStatusBroadcast(MusicStatus.STOP).send()
        updateNotification()
    }

    /**
     * 请求强制下一曲
     */
    override fun callNext() {
        if(musicList.isEmpty()){
            return
        }
        callStop()
        when(loopType){
            LoopType.SingleLoop,LoopType.List,LoopType.ListLoop -> {
                playingMusicIndex++
                playByIndex()
            }
            LoopType.Random -> {
                var newPosition = (System.currentTimeMillis() % musicList.size).toInt()
                if(newPosition == playingMusicIndex){
                    newPosition ++
                }
                playingMusicIndex = newPosition % musicList.size
                playByIndex()
            }
        }
    }

    /**
     * 请求强制上一曲
     */
    override fun callPrevious() {
        if(musicList.isEmpty()){
            return
        }
        callStop()
        when(loopType){
            LoopType.SingleLoop,LoopType.List,LoopType.ListLoop -> {
                playingMusicIndex--
                if(playingMusicIndex < 0){
                    playingMusicIndex = musicList.size-1
                }
                playByIndex()
            }
            LoopType.Random -> {
                var newPosition = (System.currentTimeMillis() % musicList.size).toInt()
                if(newPosition == playingMusicIndex){
                    newPosition ++
                }
                playingMusicIndex = newPosition % musicList.size
                playByIndex()
            }
        }
    }

    /**
     * 请求修改当前播放音乐的收藏状态
     */
    override fun callLikeTypeChange(isLike: Boolean) {
        log("callLikeTypeChange()")
        if(!canPlay()){
            return
        }
        log("callLikeTypeChange() - true")
        musicList[playingMusicIndex].isLike = isLike
        // 修改数据库喜欢状态
        updateNotification()
//        createStatusBroadcast(MusicStatus.LIKE).send()
    }

    /**
     * 请求修改循环状态
     */
    override fun callLoopTypeChange(type: LoopType) {
        log("callLoopTypeChange($type)")
        if(!canPlay()){
            return
        }
        log("callLoopTypeChange($type) - true")
        this.loopType = type
        playerHelper.isLooping = loopType == LoopType.SingleLoop
        updateNotification()
//        createStatusBroadcast(MusicStatus.LOOP).send()
    }

    /**
     * 请求修改播放列表
     */
    override fun callChangeMusicList(musicBeanList: ArrayList<MusicBean>) {
        log("callChangeMusicList(${musicBeanList.size})")
        val tempList = LinkedList<MusicBean>()
        tempList.addAll(musicList)
        musicList.clear()
        //记录当前播放音频地址
        val playingKey = if(playingMusicIndex >= 0 && playingMusicIndex < tempList.size){
            tempList[playingMusicIndex].path
        }else{
            ""
        }

        for(i in 0 until musicBeanList.size){
            val bean = musicBeanList[i]
            val musicBean = if(tempList.isNotEmpty()){
                tempList.removeFirst()
            }else{
                MusicBean()
            }
            //如果地址相同，那么记录为新的播放地址
            if(bean.path == playingKey){
                playingMusicIndex = i
            }
            musicBean.copy(bean)
            musicList.add(bean)
        }
        tempList.clear()
//        createStatusBroadcast(MusicStatus.LIST).send()
    }

    /**
     * 请求播放指定的音频文件
     */
    override fun callPlayWith(bean: MusicBean) {
        log("callPlayWith()")
        callStop()
        var index = -1
        for(i in 0 until musicList.size){
            val b = musicList[i]
            if(b.cursorId == bean.cursorId && b.id == bean.id){
                index = i
                break
            }
        }

        if(index >= 0){
            playingMusicIndex = index
        }else{
            playingMusicIndex = 0
            val newBean = MusicBean()
            newBean.copy(bean)
            musicList.add(0,newBean)
        }
        playByIndex()
    }

    /**
     * 请求调整当前播放音乐的进度
     * 单位：ms 毫秒
     */
    override fun callSeekTo(ms: Int) {
        log("callSeekTo($ms)")
        if(!canPlay() || !playerHelper.isReady){
            return
        }
        log("callSeekTo($ms) - true")
        playerHelper.seekTo(ms)
        val progress = 1.0F * ms / playerHelper.duration
//        createStatusBroadcast(MusicStatus.DRAG).progress(progress).send()
    }

    /**
     * 修改左右声道音量
     */
    override fun callVolume(leftVolume: Float, rightVolume: Float) {
        playerHelper.setVolume(leftVolume,rightVolume)
    }

    /**
     * 获取播放的音频列表
     * 返回当前播放列表的复制品
     */
    override fun getMusicList(): ArrayList<MusicBean> {
        log("getMusicList()")
        val newList = ArrayList<MusicBean>(musicList.size)
        for(bean in musicList){
            newList.add(MusicBean().apply {
                copy(bean)
            })
        }
        return newList
    }

    /**
     * 返回当前播放状态
     */
    override fun getStatus(): MusicStatus {
        return playerStatus
    }

    /**
     * 获取正在播放的音频文件
     */
    override fun getMusic(): MusicBean? {
        if(musicList.isEmpty() || playingMusicIndex < 0 || playingMusicIndex >= musicList.size){
            return null
        }
        return MusicBean().apply {
            copy(musicList[playingMusicIndex])
        }
    }

    /**
     * 当播放发生错误
     * @funName 播放辅助类中相应方法名
     * @e 发生的异常
     */
    override fun onPlayerError(funName: String, e: Exception) {
        log("onPlayerError($funName, $e)")
        createStatusBroadcast(MusicStatus.ERROR).errorCode(0).errorValue(funName + e.localizedMessage).send()
    }

    /**
     * 当播放辅助器中主动抛出异常
     * @what 异常类型
     * @extra 描述
     */
    override fun onError(what: Int, extra: Int) {
        log("onError($what, $extra)")
        createStatusBroadcast(MusicStatus.ERROR).errorCode(what).errorValue("code: $extra").send()
    }

    /**
     * 当播放结束
     */
    override fun onCompletion() {
        log("播放结束()")

        callStop()
        playNext()
    }

    /**
     * 当播放器抛出警告信息
     */
    override fun onInfoListener(what: Int, extra: Int) {
        log("onInfoListener($what, $extra)")
        createStatusBroadcast(MusicStatus.ALERT).errorCode(what).errorValue("code: $extra").send()
    }

    /**
     * 当播放器初始化完成
     */
    override fun onPrepared() {
        log("onPrepared()")
        if(playerStatus == MusicStatus.LOADING){
            log("onPrepared() - start")
            playerHelper.start()
            createStatusBroadcast(MusicStatus.START).send()
            createStatusBroadcast(MusicStatus.PLAYING).send()
            updateNotification()
        }
    }

    /**
     * 当播放器进度调整完成
     */
    override fun onSeekComplete() {
        log("onSeekComplete()")
        val progress = 1.0F * playerHelper.currentPosition / playerHelper.duration
//        createStatusBroadcast(MusicStatus.DRAG).progress(progress).send()
    }

    /**
     * 获取当前播放音频长度
     * 单位：md 毫秒
     */
    override fun getDuration(): Int {
        val duration = playerHelper.duration
        log("getDuration(): $duration")
        return duration
    }

    /**
     * 获取当前播放进度
     * 单位：ms 毫秒
     */
    override fun getCurrentPosition(): Int {
        position = playerHelper.currentPosition
        log("getCurrentPosition(): $position")
        return position
    }

    /**
     * 是否正在播放
     *
     */
    private fun isPlaying(): Boolean{
        val isPlaying = playerHelper.isPlaying
        log("isPlaying(): $isPlaying")
        return isPlaying
    }

    /**
     * 当服务得到启动命令
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand($intent, $flags, $startId): START_STICKY")
        val notification = createMusicNotification(
            getString(R.string.app_name),
            getString(R.string.app_name),
            getLargeIcon())
        startForeground(FOREGROUND_CODE,notification)
        getDrawable(R.drawable.disruptive_sound_small)
        return START_STICKY
    }

    /**
     * 获取一个非空的默认封面图
     */
    private fun getLargeIcon(): Bitmap{
        log("getLargeIcon()")
        return if(defaultLargeIcon?.isRecycled == false){
            defaultLargeIcon!!
        }else{
            defaultLargeIcon = drawableToBitmap(getDrawable(R.drawable.disruptive_sound_small)!!)
            defaultLargeIcon!!
        }
    }

    /**
     * 将Drawable转换为Bitmap
     */
    private fun drawableToBitmap(drawable: Drawable):Bitmap {
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        val config =  if(drawable.opacity != PixelFormat.OPAQUE){
            Bitmap.Config.ARGB_8888
        }else{
            Bitmap.Config.RGB_565
        }
        val bitmap = Bitmap.createBitmap(w, h, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0,0, w, h)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 创建一个音乐消息
     */
    private fun createMusicNotification(title: String, msg: String, largeIcon: Bitmap): Notification {

        val previousTitle = getString(R.string.music_skip_previous)
        val previousIntent = NotificationControllerBroadcast.pendingPrevious(this)

        val nextTitle = getString(R.string.music_skip_next)
        val nextIntent = NotificationControllerBroadcast.pendingNext(this)

        val builder =  NotificationCompat.Builder(this, MusicConstant.NotificationChannelId)
            .setSmallIcon(R.drawable.ic_music_note_black_24dp)
            .setContentTitle(title)
            .setContentText(msg)
            .setLargeIcon(largeIcon)
            .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0,1,2))
            .setAutoCancel(false)
//                .setColorized(true)
            .addAction(R.drawable.ic_outline_skip_previous_24px,previousTitle,previousIntent)

        if(notificationColor != Color.TRANSPARENT){
            builder.color = notificationColor
            builder.setColorized(true)
        }

        if(playerStatus == MusicStatus.PLAYING){
            val pauseTitle = getString(R.string.music_pause)
            val pauseIntent = NotificationControllerBroadcast.pendingPause(this)
            builder.addAction(R.drawable.ic_pause_black_24dp,pauseTitle,pauseIntent)
        }else{
            val playTitle = getString(R.string.music_play)
            val playIntent = NotificationControllerBroadcast.pendingPlay(this)
            builder.addAction(R.drawable.ic_outline_play_arrow_24px,playTitle,playIntent)
        }

        builder.addAction(R.drawable.ic_outline_skip_next_24px,nextTitle,nextIntent)

        return builder.build()
    }

    /**
     * 创建消息渠道
     */
    private fun createChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelId = MusicConstant.NotificationChannelId
            val channelName = MusicConstant.NotificationChannelName
            val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lightColor = MusicConstant.NotificationChannelColor
            chan.lockscreenVisibility = Notification.VISIBILITY_SECRET
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
        }
    }

    /**
     * 更新当前播放音频的消息
     */
    private fun updateNotification(){
        updateNotification(musicList[playingMusicIndex])
    }

    /**
     * 更新消息为指定音频文件
     */
    private fun updateNotification(musicBean: MusicBean){
        if(coverKey != musicBean.path){
            coverKey = musicBean.path
            coverBitmap?.recycle()
            coverBitmap = musicBean.getCover()

            notificationColor = Color.TRANSPARENT
            val showingBitmap = coverBitmap?:getLargeIcon()
            Palette.Builder(showingBitmap).generate {
                ColorChangeBroadcast.sendBroadcast(this@MusicService, it!!)
                notificationColor = it.getMutedColor(Color.TRANSPARENT)
                updateNotification()
            }

        }
        updateNotification(musicBean.name, musicBean.artist, coverBitmap?:getLargeIcon())
    }

    /**
     * 传入显示信息，以更新状态栏信息
     */
    private fun updateNotification(title: String, msg: String, largeIcon: Bitmap){

        val notification = createMusicNotification(title, msg, largeIcon)
        startForeground(FOREGROUND_CODE,notification)
        notificationManager.notify(FOREGROUND_CODE,notification)
    }

    /**
     * 当销毁时的生命周期方法
     */
    override fun onDestroy() {
        super.onDestroy()
        defaultLargeIcon?.recycle()
        defaultLargeIcon = null

        if (musicBean != null){

            editor.putString("musicName",musicBean!!.name)
            editor.putLong("musicId",musicBean!!.cursorId)
            editor.putInt("musicSeek",position)
            editor.apply()
        }

        for(binder in binderList){
            binder.onDestroy()
        }
        binderList.clear()

        playerHelper.release()

        notificationControllerBroadcast.unregister(this)
    }

    /**
     * 创建一个状态变更广播
     */
    private fun createStatusBroadcast(status: MusicStatus): MusicStatusBroadcast.Builder{
        playerStatus = status
        val build = MusicStatusBroadcast.create(this,playerStatus)
        if(playingMusicIndex > 0 && playingMusicIndex < musicList.size){
            val bean = musicList[playingMusicIndex]
            build.musicId(bean.id)
                .cursorId(bean.cursorId)
                .loopType(loopType)
                .isLike(bean.isLike)
        }
        return build
    }

    /**
     * 是否可以播放
     */
    private fun canPlay(): Boolean{
        if(musicList.isEmpty()){
            return false
        }
        if(playingMusicIndex < 0 || playingMusicIndex >= musicList.size){
            return false
        }
        return true
    }

    /**
     * 播放指定位置的音频
     */
    private fun playByIndex(){
        playingMusicIndex %= musicList.size
        val musicBean = musicList[playingMusicIndex]
        log(playingMusicIndex.toString()+"等待播放的index" + musicList.size.toString())
        playerHelper.setData(musicBean)
        this.musicBean = musicBean
        createStatusBroadcast(MusicStatus.WAITING).send()
        callStart()
    }

    /**
     * 根据循环模式自动播放下一曲
     */
    private fun playNext(){
        if(musicList.isEmpty()){
            log("不可能")
            return
        }
        when(loopType){
            LoopType.List -> if(playingMusicIndex < musicList.size-1){
                playingMusicIndex++
                playByIndex()
            }else{Log.e("什么鬼",playingMusicIndex.toString() + "<" + musicList.size.toString())}
            LoopType.SingleLoop -> {
                // 不作任何操作，交由播放器循环
                createStatusBroadcast(MusicStatus.START).send()
            }
            LoopType.ListLoop -> {
                playingMusicIndex++
                playByIndex()
            }
            LoopType.Random -> {
                var newPosition = (System.currentTimeMillis() % musicList.size).toInt()
                if(newPosition == playingMusicIndex){
                    newPosition ++
                }
                playingMusicIndex = newPosition % musicList.size
                playByIndex()
            }
        }
    }

}
