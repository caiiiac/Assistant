package org.andcreator.assistant.listener


/**
 *音乐服务当前状态
 */
enum class MusicStatus(val value: Int) {
    /**
     * 未知
     */
    UNKNOWN(-3),
    /**
     * 警告
     */
    ALERT(-2),
    /**
     * 加载出错
     */
    ERROR(-1),
    /**
     * 正在加载
     */
    LOADING(0),
    /**
     * 开始播放
     */
    START(1),
    /**
     * 暂停
     */
    PAUSE(2),
    /**
     * 停止
     */
    STOP(3),
    /**
     * 进度调整
     */
    DRAG(4),
    /**
     * 正在播放
     */
    PLAYING(5),
    /**
     * 收藏状态变更
     */
    LIKE(6),
    /**
     * 循环状态变更
     */
    LOOP(7),
    /**
     * 音乐播放列表变更
     */
    LIST(8),
    /**
     * 播放结束
     */
    END(9),
    /**
     * 等待加载
     */
    WAITING(10);

    companion object {
        fun parse(value: Int): MusicStatus{
            return when(value){
                ERROR.value -> ERROR
                LOADING.value -> LOADING
                START.value -> START
                PAUSE.value -> PAUSE
                STOP.value -> STOP
                DRAG.value -> DRAG
                PLAYING.value -> PLAYING
                LIKE.value -> LIKE
                LOOP.value -> LOOP
                LIST.value -> LIST
                else -> UNKNOWN
            }
        }
    }

}