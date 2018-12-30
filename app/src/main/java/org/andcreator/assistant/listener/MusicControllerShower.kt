package org.andcreator.assistant.listener

/**
 *
 * Fragment与Activity之间的回调(Music)
 */
interface MusicControllerShower {
    /**
     * 音乐停止
     */
    fun onMusicStop()

    /**
     * 音乐开始
     */
    fun onMusicStart(cursorId: Long?)

    /**
     * 音乐暂停
     */
    fun onMusicPause()

    /**
     * 更新音乐进度条
     */
    fun onMusicProgress(progress: Int,time: Long,all: Long)

    /**
     * 载入音乐
     */
    fun onMusicLoading()

    /**
     * 更新音乐信息
     */
    fun onReply(musicStatus: MusicStatus,cursorId: Long?)

    /**
     * 更新当前音乐进度
     */
    fun onNowProgress(progress: Int, time: Long, all: Long)
}