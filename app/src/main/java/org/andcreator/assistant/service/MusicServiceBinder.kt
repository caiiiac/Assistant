package org.andcreator.assistant.service

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import org.andcreator.assistant.bean.MusicBean
import org.andcreator.assistant.listener.MusicStatus
import org.andcreator.assistant.util.LoopType


/**
 * @date: 2018/09/04 23:19
 * @author: lollipop
 * 音乐服务的Binder
 */
class MusicServiceBinder(private var serviceCallback: ServiceCallback?): Binder(), IInterface {

    /**
     * 连接对象是否存活的标识位
     * 此标识需要在连接对象销毁时更改记录的状态
     */
    private var isAlive = true

    /**
     * 状态检查的监听器集合
     */
    private val recipientList = ArrayList<IBinder.DeathRecipient>()

    fun onDestroy(){
        isAlive = false
        serviceCallback = null
        for(recipient in recipientList){
            recipient.binderDied()
        }
    }

    override fun asBinder(): IBinder {
        return this
    }

    /**
     * 修改播放列表
     * @param musicBeanList 音乐列表
     */
    fun changeMusicList(musicBeanList: ArrayList<MusicBean>){
        serviceCallback?.callChangeMusicList(musicBeanList)
    }

    /**
     * 修改循环方式
     * @param type 循环方式

     */
    fun changeLoopType(type: LoopType){
        serviceCallback?.callLoopTypeChange(type)
    }

    /**
     * 修改当前音乐的喜欢状态
     */
    fun changeLikeStatue(isLike: Boolean){
        serviceCallback?.callLikeTypeChange(isLike)
    }

    /**
     * 开始播放
     */
    fun startPlayer(){
        serviceCallback?.callStart()
    }

    /**
     * 暂停播放
     */
    fun pausePlayer(){
        serviceCallback?.callPause()
    }

    /**
     * 停止播放
     */
    fun stopPlayer(){
        serviceCallback?.callStop()
    }

    /**
     * 跳转到指定位置
     */
    fun seekTo(ms: Int){
        serviceCallback?.callSeekTo(ms)
    }

    /**
     *调节左右声道音量
     */
    fun changeVolume(leftVolume: Float,rightVolume: Float){
        serviceCallback?.callVolume(leftVolume,rightVolume)
    }
    /**
     * 播放指定的歌曲
     * 上一曲
     * 下一曲
     * 点播
     * 都将调用此方法
     * 如果本歌曲已经在列表中，将跳转播放，如果不在，
     * 那么将加入播放列表顶部，并且播放
     */
    fun playWith(bean: MusicBean){
        serviceCallback?.callPlayWith(bean)
    }

    /**
     * 获取播放列表的集合
     */
    fun getMusicList(): ArrayList<MusicBean>?{
        return serviceCallback?.getMusicList()
    }

    fun getMusic(): MusicBean?{
        return serviceCallback?.getMusic()
    }

    /**
     * 获取歌曲时长
     * @return 单位为ms
     */
    fun getDuration(): Int {
        return serviceCallback?.getDuration()?:0
    }

    /**
     * 获取当前进度
     * @return 单位为ms
     */
    fun getCurrentPosition(): Int {
        return serviceCallback?.getCurrentPosition()?:0
    }

    /**
     * 获取当前播放状态
     */
    fun getStatus(): MusicStatus{
        return serviceCallback?.getStatus()?: MusicStatus.UNKNOWN
    }

    /**
     * 下一首
     */
    fun previousPlayer(){
        serviceCallback?.callPrevious()
    }

    /**
     * 上一首
     */
    fun nextPlayer(){
        serviceCallback?.callNext()
    }

    interface ServiceCallback{
        fun callStart()

        fun callPause()

        fun callStop()

        fun callLikeTypeChange(isLike: Boolean)

        fun callLoopTypeChange(type: LoopType)

        fun callChangeMusicList(musicBeanList: ArrayList<MusicBean>)

        fun callPlayWith(bean: MusicBean)

        fun callSeekTo(ms: Int)

        fun getMusicList(): ArrayList<MusicBean>

        fun getDuration(): Int

        fun getCurrentPosition(): Int

        fun getStatus(): MusicStatus

        fun getMusic(): MusicBean?

        fun callNext()

        fun callPrevious()

        fun callVolume(leftVolume: Float,rightVolume: Float)
    }

}