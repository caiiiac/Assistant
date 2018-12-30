package org.andcreator.assistant.util


/**
 * @date: 2018/09/04 23:14
 * @author: lollipop
 * 循环方式
 */
enum class LoopType(val value: Int) {

    /**
     * 列表按顺序播放
     */
    List(0),
    /**
     * 按列表循环，播放最后一首之后再次播放第一首
     */
    ListLoop(1),
    /**
     * 单曲循环，只循环当前
     */
    SingleLoop(2),
    /**
     * 随机播放
     */
    Random(3);

    companion object {
        fun parse(value: Int): LoopType{
            return when(value){
                ListLoop.value -> ListLoop
                SingleLoop.value -> SingleLoop
                Random.value -> Random
                else -> List
            }
        }
    }

}