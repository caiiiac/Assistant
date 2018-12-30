package org.andcreator.assistant.listener

/**
 * 监听WidgetCell调节大小
 */
interface ResizeWidgetListener {

    fun onMove(widgetId: String,deltaX: Int,deltaY: Int,direction: Int)
    fun onUp()

//    fun onCompleted()
}