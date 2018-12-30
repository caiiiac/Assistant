package org.andcreator.assistant.fragment


import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.*
import android.os.*
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.animation.LinearInterpolator
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_music.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

import org.andcreator.assistant.R
import org.andcreator.assistant.adapter.MusicListAdapter
import org.andcreator.assistant.bean.MusicBean
import org.andcreator.assistant.skin.Skin
import org.andcreator.assistant.skin.SkinFragment
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.SeekBar
import kotlinx.android.synthetic.main.item_apps.view.*
import org.andcreator.assistant.listener.*
import org.andcreator.assistant.service.MusicService
import org.andcreator.assistant.service.MusicServiceBinder
import org.andcreator.assistant.util.*
import org.jetbrains.anko.toast


/**
 * A simple [Fragment] subclass.
 *
 */
class MusicFragment : SkinFragment(),
    MusicListFragment,
    MusicListAdapter.SelectedItemCallback,
    MusicControllerShower,
    ValueAnimator.AnimatorUpdateListener,
    MusicStatusBroadcast.Callback,
    SimpleHandler.HandlerCallback{

    private var mPressedTime: Long = 0
    /**
     * 是否加载
     */
    private var isLoading = false

    /**
     * 音乐列表
     */
    private var dataList = ArrayList<MusicBean>()

    /**
     * 音乐列表适配器
     */
    private lateinit var adapter:MusicListAdapter

    /**
     * 当前播放的Position
     */
    private var playingPosition = -1

    /**
     * 进度条的动画
     */
    private val valueAnimator = ValueAnimator()

    /**
     * 记录音乐进度
     */
    private var progress = 0

    /**
     * 音乐在媒体库的id
     */
    private var cursorId = 0L

    /**
     * 是否绑定了服务/用于解绑服务
     */
    private var isBindService = false

    /**
     * 是否正在拖动进度条
     */
    private var isDragSeekBar = false

    /**
     * 音乐储存的信息
     */
    private lateinit var preferences: SharedPreferences

    /**
     * 写入音乐信息
     */
    private lateinit var editor: SharedPreferences.Editor

    /**
     * MusicService的Binder
     */
    private var musicServiceBinder: MusicServiceBinder? = null

    /**
     * MusicService当前的状态
     */
    private var musicStatus = MusicStatus.UNKNOWN

    /**
     * 音乐状态变化的监听器的实例
     */
    private val musicStatusBroadcast = MusicStatusBroadcast(this)

    /**
     * 当前音乐的信息
     */
    private var playingMusicInfo: MusicBean? = null

    /**
     * 更新Music的handler
     */
    private var handler: Handler = SimpleHandler(this)
    /**
     * 连接服务后播放
     */
    private var connectedPlay = false
    /**
     * 播放后是否跳转进度
     */
    private var connectedSeek = false

    /**
     * isPlay
     */
    private var isPlay = false

    /**
     * 左右声道音量
     */
    private var leftVolumes = 1f
    private var rightVolumes = 1f

    companion object {
        //更新音乐进度的标识
        private const val WHAT_UPDATE_PROGRESS = 123
        //当前音乐进度的标识
        private const val WHAT_FIRST_UPDATE_PROGRESS = 233
    }

    private var progressPreferences = 0
    /**
     * 获得MusicService的Binder
     */
    private val serviceConnection = object : ServiceConnection {
        //断开服务连接的回调
        override fun onServiceDisconnected(name: ComponentName?) {
            musicServiceBinder = null
        }

        //已连接服务的回调
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if(service != null && service is MusicServiceBinder){
                isBindService = true
                //初始化Binder
                musicServiceBinder = service

                //获取音乐状态
                musicStatus = musicServiceBinder?.getStatus()?: MusicStatus.UNKNOWN
                Log.e("musicStatus",musicStatus.toString())

                //如果音乐正在播放，则开始更新进度条
                if(musicStatus == MusicStatus.PLAYING
                    || musicStatus == MusicStatus.START){
                    isPlay = true
                    handler.sendEmptyMessage(WHAT_UPDATE_PROGRESS)
                }

                if (connectedPlay){
                    callChangeMusicList(dataList)
                    playingMusicInfo = dataList[playingPosition]

                    //更新当前音乐的信息
                    onReply(musicStatus,dataList[playingPosition].cursorId)

                    callPlay(playingPosition)
                    isPlay = true

                }else{
                    //获取当前音乐的信息
                    playingMusicInfo = musicServiceBinder?.getMusic()
                    //更新当前音乐的信息
                    onReply(musicStatus,musicServiceBinder?.getMusic()?.cursorId)

                }
                handler.sendEmptyMessage(WHAT_FIRST_UPDATE_PROGRESS)
                connectedPlay = false

                when(preferences.getInt("musicLoopType",0)){
                    1 ->{
                        callLoopTypeChange(LoopType.ListLoop)
                    }
                    3 ->{
                        callLoopTypeChange(LoopType.Random)
                    }
                }
            }
        }
    }

    /**
     * 启动音乐服务
     */
    private fun startMusicService(){

        //如果服务没有开启则开启服务
        if (!ServiceUtils.isServiceRunning(activity!!, "org.andcreator.assistant.service.MusicService")){
            if(Build.VERSION_CODES.O <= Build.VERSION.SDK_INT){
                activity!!.startForegroundService(Intent(activity!!, MusicService::class.java))
            }else{
                activity!!.startService(Intent(activity!!, MusicService::class.java))
            }
            Log.e("启动服务 ","启动服务")
        }

        //绑定到服务并开始接收音乐状态广播
        activity!!.bindService(
            Intent(activity!!, MusicService::class.java),
            serviceConnection, Context.BIND_AUTO_CREATE)
        musicStatusBroadcast.register(activity!!)
    }

    override fun onStart() {
        super.onStart()

        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit()

        //如果服务已启动则绑定服务
        if (ServiceUtils.isServiceRunning(activity!!, "org.andcreator.assistant.service.MusicService")) {
            //绑定到服务并开始接收音乐状态广播
            activity!!.bindService(
                Intent(activity!!, MusicService::class.java),
                serviceConnection, Context.BIND_AUTO_CREATE
            )
            musicStatusBroadcast.register(activity!!)
        }

    }

    override fun setContentView(): Int {
        return R.layout.fragment_music
    }

    override fun lazyLoad() {

        TypefaceUtil.replaceFont(contentView, "fonts/ProductSans.ttf")
        initView()
    }

    private fun log(value: String) {
        Log.d("MainActivity", value)
    }

    private fun initView() {

        val loopIcon = R.drawable.ic_loop_attrs_24dp
        val noLoopIcon = R.drawable.ic_loop_trans_24dp
        val randomIcon = R.drawable.ic_outline_shuffle_24px
        val noRandomIcon = R.drawable.ic_outline_shuffle_trans_24px

        //初始化音乐列表
        musicList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        adapter = MusicListAdapter(context!!,dataList,this)
        adapter.setClickListener(object : MusicListAdapter.OnItemClickListener{
            override fun onClick(position: Int) {
                if(isLoading){
                    return
                }
                callPlay(position)
            }
        })
        musicList.adapter = adapter

        when(preferences.getInt("musicLoopType",0)){
            1 ->{
                loop.setImageResource(loopIcon)
            }
            3 ->{
                loop.setImageResource(loopIcon)
                random.setImageResource(randomIcon)
            }
        }

        //监听监听监听监听监听监听监听监听监听监听监听监听监听监听监听监听监听监听监听
        //初始化SeekBar动画
        valueAnimator.addUpdateListener(this)
        valueAnimator.interpolator = LinearInterpolator()

        //音乐控制
        control.setOnClickListener {

            isPlay = if (isPlay){
                callPause()
                status.setImageResource(R.drawable.ic_outline_play_arrow_24px)
                false
            }else {
                callStart()
                status.setImageResource(R.drawable.ic_outline_pause_24px)
                true
            }
        }

        //上一曲
        skipPrevious.setOnClickListener {
            if(musicServiceBinder == null && playingPosition > 0){
                connectedPlay = true

                adapter.notifyItemChanged(playingPosition)

                playingPosition-=1
                startMusicService()
            }
            musicServiceBinder?.previousPlayer()
        }

        //下一曲
        skipNext.setOnClickListener {
            if(musicServiceBinder == null && playingPosition < dataList.size){
                connectedPlay = true

                adapter.notifyItemChanged(playingPosition)

                playingPosition+=1
                startMusicService()
            }
            musicServiceBinder?.nextPlayer()
        }
        //载入音乐列表
        getData()

        //进度条的拖拽
        musicProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isDragSeekBar){

                    if (musicServiceBinder != null){

                        val all = musicServiceBinder?.getDuration()?:1
                        val cha = progress/1000f

                        callSeekTo((all * cha).toInt())
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (playingMusicInfo != null)
                    valueAnimator.cancel()
                isDragSeekBar = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isDragSeekBar = false
                if (musicServiceBinder != null){

                    val all = musicServiceBinder?.getDuration()?:1
                    val now = musicServiceBinder?.getCurrentPosition()?:1

                    val p = 1.0F * now / all
                    val pro = 1000 * p

                    musicProgress.max = 1000
                    musicProgress.progress = pro.toInt()

                    progress = pro.toInt()
                    if ( musicStatus == MusicStatus.PLAYING){
                        smoothAnimator(1000,(all-now).toLong())
                    }
                }
            }

        })

        //调节音量的界面显示控制
        headImgCrop.setOnClickListener {
            //获取第一次按键时间
            val mNowTime = System.currentTimeMillis()

            //比较两次按键时间差
            val time = 800
            if (mNowTime - mPressedTime > time) {
                mPressedTime = mNowTime
            } else {

                if (musicVolume.visibility != View.VISIBLE){

                    val animator1 = ObjectAnimator.ofFloat(musicCover, "alpha", 1f,0f)
                    animator1.duration = 250
                    animator1.interpolator = DecelerateInterpolator()
                    animator1.addListener(object : Animator.AnimatorListener{
                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            musicVolume.visibility = View.VISIBLE
                            val animator2 = ObjectAnimator.ofFloat(musicVolume, "alpha", 0f,1f)
                            animator2.duration = 250
                            animator2.interpolator = DecelerateInterpolator()
                            animator2.start()

                            musicCover.visibility = View.INVISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {

                        }

                    })
                    animator1.start()

                }else{

                    val animator1 = ObjectAnimator.ofFloat(musicVolume, "alpha", 1f,0f)
                    animator1.duration = 250
                    animator1.interpolator = DecelerateInterpolator()
                    animator1.addListener(object : Animator.AnimatorListener{
                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            musicCover.visibility = View.VISIBLE
                            val animator2 = ObjectAnimator.ofFloat(musicCover, "alpha", 0f,1f)
                            animator2.duration = 250
                            animator2.interpolator = DecelerateInterpolator()
                            animator2.start()

                            musicVolume.visibility = View.INVISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {

                        }

                    })
                    animator1.start()

                }
            }
        }

        //音量调节
        volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                leftVolumes = progress/100f
                rightVolumes = progress/100f
                callVolume(leftVolumes,rightVolumes)

                volumeLeft.progress = progress
                volumeRight.progress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        volumeLeft.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                leftVolumes = progress/100f
                callVolume(leftVolumes,rightVolumes)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        volumeRight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rightVolumes = progress/100f
                callVolume(leftVolumes,rightVolumes)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        //循环变更
        loop.setOnClickListener {

            if(musicServiceBinder != null){

                if (preferences.getInt("musicLoopType",0) == 1){
                    loop.setImageResource(noLoopIcon)
                    editor.putInt("musicLoopType",0)
                    editor.apply()
                    callLoopTypeChange(LoopType.List)
                }else{
                    loop.setImageResource(loopIcon)
                    editor.putInt("musicLoopType",1)
                    editor.apply()
                    callLoopTypeChange(LoopType.ListLoop)
                }
            }
        }

        random.setOnClickListener {

            if(musicServiceBinder != null) {

                if (preferences.getInt("musicLoopType",0) == 3){
                    random.setImageResource(noRandomIcon)
                    editor.putInt("musicLoopType",1)
                    editor.apply()
                    callLoopTypeChange(LoopType.ListLoop)
                }else{
                    random.setImageResource(randomIcon)
                    loop.setImageResource(loopIcon)
                    editor.putInt("musicLoopType",3)
                    editor.apply()
                    callLoopTypeChange(LoopType.Random)
                }
            }
        }
    }

    /**
     * 载入播放列表
     */
    private fun getData(){
        if(isLoading){
            return
        }
        isLoading = true

        doAsync{

            val ctx = context
            if(ctx != null){
                dataList.clear()
                playingMusicInfo = MusicUtil.scanMusic(ctx,dataList,preferences.getLong("musicId",-1))
            }

            callChangeMusicList(dataList)

            uiThread {
                adapter.notifyDataSetChanged()
                isLoading = false

                //如果播放服务不存在则加载上次播放的音乐
                if (!ServiceUtils.isServiceRunning(activity!!, "org.andcreator.assistant.service.MusicService") && playingMusicInfo != null){
                    musicName.text = playingMusicInfo!!.name
                    Glide.with(context!!).load(playingMusicInfo!!.getCover()).error(Glide.with(context!!).load(R.drawable.disruptive_sound)).into(headImg)

                    val all = playingMusicInfo!!.duration
                    val now = preferences.getInt("musicSeek",0)

                    val p = 1.0F * now / all
                    val pro = 1000 * p
                    musicProgress.max = 1000
                    musicProgress.progress = pro.toInt()
                }
                replyPosition()

            }

        }
    }


    override fun getSelectedItemPosition(): Int {
        return playingPosition
    }

    override fun rejectTouch(isReject: Boolean){
//        rejectTouchView.visibility = if(isReject){View.VISIBLE}else{View.INVISIBLE}
    }

    override fun getMusicList(): ArrayList<MusicBean> {
        return dataList
    }

    /**
     * 音乐状态改变回调
     */
    override fun onMusicStatusChange(newStatus: MusicStatus, info: MusicStatusBroadcast.Info) {
        musicStatus = newStatus

        Log.e("musicStatus",newStatus.toString())

        when(musicStatus){

            //音乐开始
            MusicStatus.START -> {
                //获取音乐信息更新音乐进度
                playingMusicInfo = musicServiceBinder?.getMusic()
                handler.sendEmptyMessage(WHAT_UPDATE_PROGRESS)

                onMusicStart(playingMusicInfo?.cursorId)
                isPlay = true
            }

            //正在播放
            MusicStatus.PLAYING -> {
                isPlay = true
                log("musicPlay")
            }

            //音乐暂停
            MusicStatus.PAUSE -> {
                handler.removeMessages(WHAT_UPDATE_PROGRESS)
                onMusicPause()
                isPlay = false
            }

            //音乐停止
            MusicStatus.STOP -> {
                playingMusicInfo = null
                handler.removeMessages(WHAT_UPDATE_PROGRESS)
                onMusicStop()
                isPlay = false
                log("musicStop")
            }

            //音乐加载
            MusicStatus.LOADING -> {

                onMusicLoading()
            }

            //音乐循环
            MusicStatus.LOOP -> {

            }

            else -> {}

        }
    }

    /**
     * 更新音乐进度
     */
    override fun onHandler(message: Message) {
        when(message.what){
            WHAT_UPDATE_PROGRESS -> {

                if(!isPlay){
                    return
                }
                handler.sendEmptyMessageDelayed(WHAT_UPDATE_PROGRESS,1000)
                if(musicServiceBinder == null){
                    return
                }
                val all = musicServiceBinder?.getDuration()?:1
                val now = musicServiceBinder?.getCurrentPosition()?:1
                val p = 1.0F * now / all
                val pro = 1000 * p

                onMusicProgress(pro.toInt(),now.toLong(),all.toLong())
            }

            WHAT_FIRST_UPDATE_PROGRESS -> {
                handler.sendEmptyMessageDelayed(WHAT_UPDATE_PROGRESS,1000)

                if(musicServiceBinder == null){
                    return
                }
                val all = musicServiceBinder?.getDuration()?:1
                val now = musicServiceBinder?.getCurrentPosition()?:1
                val p = 1.0F * now / all
                val pro = 1000 * p

                onNowProgress(pro.toInt(),now.toLong(),all.toLong())
            }
        }
    }

    /**
     * 获取当前MusicBean信息
     */
    override fun onReply(musicStatus: MusicStatus,cursorId: Long?) {
        val musicBean = playingMusicInfo
        replyPosition()

        if (cursorId!= null && cursorId != this.cursorId){
            if(musicBean == null){
                Log.e("空空","NullPointExeption")
                //67071
            }else{

                musicName.text = musicBean.name
                Glide.with(this).load(musicBean.getCover()).error(Glide.with(this).load(R.drawable.disruptive_sound)).into(headImg)
            }

            this.cursorId = cursorId
        }
    }

    /**
     * 更新RecyclerView正在播放的Position
     */
    private fun replyPosition(){
        val musicBean = playingMusicInfo
        val lastPosition = playingPosition

        playingPosition = -1
        if(musicBean != null){
            for(index in 0 until dataList.size){
                val bean = dataList[index]
                if(bean.cursorId == musicBean.cursorId){
                    playingPosition = index
                    break
                }
            }
        }

        Log.e("replyPosition",lastPosition.toString()+"  "+playingPosition.toString())

        if(playingPosition >= 0 && playingPosition < dataList.size){
            adapter.notifyItemChanged(playingPosition)
        }
        if(lastPosition >= 0 && lastPosition < dataList.size){
            adapter.notifyItemChanged(lastPosition)
        }
    }

    /**
     * 播放指定音乐
     */
    private fun callPlay(position: Int){
        val lastPosition = playingPosition
        playingPosition = position
        if(lastPosition >= 0){
            adapter.notifyItemChanged(lastPosition)
        }
        adapter.notifyItemChanged(playingPosition)

        if(musicServiceBinder == null){
            connectedPlay = true
            playingMusicInfo = dataList[position]
            startMusicService()
        }else{
            musicServiceBinder?.playWith(dataList[position])
        }
    }

    /**
     * 开始播放
     */
    private fun callStart(){

        if(musicServiceBinder == null){
            connectedPlay = true
            connectedSeek = true
            progressPreferences = preferences.getInt("musicSeek",0)
            startMusicService()
        }else{
            musicServiceBinder?.startPlayer()
        }

    }

    /**
     * 停止音乐
     */
    private fun callStop(){

        if(musicServiceBinder == null){
            context!!.toast(R.string.music_service_not_found)
            return
        }

        musicServiceBinder?.stopPlayer()
    }

    /**
     * 暂停音乐
     */
    private fun callPause(){
        if(musicServiceBinder == null){
            context!!.toast(R.string.music_service_not_found)
            return
        }

        musicServiceBinder?.pausePlayer()
    }

    /**
     * 跳转进度
     */
    private fun callSeekTo(ms: Int){
        if(musicServiceBinder == null){
            context!!.toast(R.string.music_service_not_found)
            return
        }

        musicServiceBinder?.seekTo(ms)
    }

    private fun callLoopTypeChange(type: LoopType){
        musicServiceBinder?.changeLoopType(type)
    }

    /**
     * 改变音乐播放列表
     */
    private fun callChangeMusicList(musicBeanList: ArrayList<MusicBean>){
        Log.e("Size",musicBeanList.size.toString())
        musicServiceBinder?.changeMusicList(musicBeanList)
    }

    private fun callVolume(leftVolume: Float,rightVolume: Float){
        musicServiceBinder?.changeVolume(leftVolume,rightVolume)
    }

    /******************************************************************************************************/


    /**
     * 已停止播放
     */
    override fun onMusicStop() {
        valueAnimator.cancel()
        status.setImageResource(R.drawable.ic_outline_play_arrow_24px)
    }

    /**
     * 已开始播放
     */
    override fun onMusicStart(cursorId: Long?) {
        if (connectedSeek){

            Log.e("musicXXX",progressPreferences.toString())
            callSeekTo(progressPreferences)
            connectedSeek = false
        }
        Log.e("musicXXX","overrideOnMusicStart")
        onReply(MusicStatus.START,cursorId)
        status.setImageResource(R.drawable.ic_outline_pause_24px)
    }

    /**
     * 已暂停播放
     */
    override fun onMusicPause() {
        valueAnimator.cancel()
        status.setImageResource(R.drawable.ic_outline_play_arrow_24px)
    }

    /**
     * 更新进度条的回调
     */
    override fun onMusicProgress(progress: Int, time: Long, all: Long) {
        Log.e("musicPPP","onMusicProgress")
        if(!isPlay){
            return
        }

        if (musicProgress != null){
            musicProgress.max = 1000
            musicProgress.progress = progress
            this.progress = progress

            smoothAnimator(1000,all - time)
        }
    }

    /**
     * 立刻更新音乐信息和进度条
     */
    override fun onNowProgress(progress: Int, time: Long, all: Long) {
        musicProgress.max = 1000
        musicProgress.progress = progress
        this.progress = progress

        val musicBean = playingMusicInfo
        if(musicBean != null){
            musicName.text = musicBean.name
            Glide.with(this).load(musicBean.getCover()).error(Glide.with(this).load(R.drawable.disruptive_sound)).into(headImg)
        }

        if (isPlay){
            status.setImageResource(R.drawable.ic_outline_pause_24px)
            smoothAnimator(1000,all - time)
        }else {
            status.setImageResource(R.drawable.ic_outline_play_arrow_24px)
        }
    }

    override fun onMusicLoading() {
    }

    /**
     * 进度条动画更新回调
     */
    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if(animation == valueAnimator){
            val pro = animation.animatedValue as Int
            progress = pro
            musicProgress.progress = progress
        }
    }

    /**
     * 开始进度条动画
     */
    private fun smoothAnimator(pro: Int, duration: Long){
        if (pro > 0 && duration > 0){
            valueAnimator.cancel()
            valueAnimator.setIntValues(progress, pro)
            valueAnimator.duration = duration
            valueAnimator.start()
        }
        Log.e("进度条更新","进度条更新")
    }

    override fun onStop() {
        super.onStop()
        if (isBindService && ServiceUtils.isServiceRunning(activity!!, "org.andcreator.assistant.service.MusicService")){
            //解绑服务并通知接收广播
            activity!!.unbindService(serviceConnection)
            musicStatusBroadcast.unregister( activity!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        valueAnimator.cancel()
    }

    override fun onSkinUpdate(skin: Skin) {

    }
}
