package org.andcreator.assistant.listener;


import android.view.View;
import org.andcreator.assistant.layout.CellLayout;

/**
 * 监听WidgetCell的拖拽
 * @author hawvu
 */
public interface LoadCompleted {

    /**
     * 拖拽完成
     * @param cell
     * @param widget
     */
    void Completed(CellLayout.Cell cell, View widget);

    /**
     * 正在拖拽
     * @param id
     */
    void onMove(String id);

    /**
     * 开始拖拽
     * @param id
     * @param locationX
     * @param locationY
     */
    void onDragStarted(String id,int locationX,int locationY);

    /**
     * 拖拽Cell到某个位置
     */
    void onDragLocation();

    /**
     * 删除调整大小
     * @param id
     */
    void onRemoveResizeFrame(String id);

    void onLauncherApp(String launcherIntent);

}
