package org.andcreator.assistant.layout;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import org.andcreator.assistant.AssistantApplication;
import org.andcreator.assistant.bean.AssistantBean;
import org.andcreator.assistant.listener.LoadCompleted;
import org.andcreator.assistant.util.DatabaseUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author hawvu
 */
public class CellLayout extends ViewGroup implements View.OnDragListener {

    /**
     * 列数
     */
    private int columns = 5;
    /**
     * 行数
     */
    private int rows = 9;
    /**
     * 每格的宽度
     */
    private int perCellWidth;
    /**
     * 每格的高度
     */
    private int perCellHeight;
    /**
     * 界面上所有Cell
     */
    private ArrayList<Cell> cells;
    /**
     * 界面上所有Widget类型的Cell
     */
    private HashMap<String,Cell> widgets;
    /**
     * 存放添加失败的Cell
     */
    private ArrayList<Cell> needRemoveCells;
    /**
     * 每格的状态
     */
    private boolean[][] cellHolds;
    /**
     * context
     */
    private Context context;
    /**
     * 加载情况回调接口
     */
    private LoadCompleted loadCompleted;

    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private DatabaseUtil.LauncherDBOperate launcherDBOperate;

    /**
     * 初始化
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public CellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        cells = new ArrayList<>();
        widgets = new HashMap<>();
        cellHolds = new boolean[rows][columns];
        needRemoveCells = new ArrayList<>();
        setOnDragListener(this);

        launcherDBOperate = DatabaseUtil.Companion.writeLauncher(context);
    }

    /**
     * 添加一个Cell
     * @param cell
     */
    public void newCell(Cell cell) {
        //添加到Cell列表中
        cells.add(cell);

        if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
            //如果界面已经显示了，那么立刻进行一次位置计算
            int nowL, nowT, locateX, locateY;
            initCell(cell);
            Point p = findLeftAndTop(cell);
            cell.setExpectRowIndex(p.x);
            cell.setExpectColumnIndex(p.y);

            locateX = cell.getExpectColumnIndex();
            locateY = cell.getExpectRowIndex();

            nowL = locateX * perCellWidth;
            nowT = locateY * perCellHeight;

            cell.setLocationX(nowL);
            cell.setLocationY(nowT);
        }

        if (cell != null){
            //更新数据库
            updateCell(cell);
        }
    }

    /**
     * 加载一个Cell
     * @param cell
     */
    public void addCell(Cell cell) {
        //添加到Cell列表中
        cells.add(cell);

        View child = cell.getContentView();
        addView(child);

        //标记点
        fillCellLayout(cell.expectColumnIndex,cell.expectRowIndex,cell.widthNum,cell.heightNum);
    }

    /**
     * 删除一个Cell
     * @param id
     */
    public void removeCell(String id){
        needRemoveCells.clear();
        Cell cell = null;
        String type = "app";
        for (Cell c : cells){
            if (c.id.equals(id)){
                type = c.tag;
                needRemoveCells.add(c);
                cell = c;
                break;
            }
        }
        if (!needRemoveCells.isEmpty()){
            deleteCell(id);
            cells.removeAll(needRemoveCells);

            //去除标记

            boolean[][] tempCellHold;
            tempCellHold = cellHolds.clone();
            for (int i = cell.getExpectColumnIndex(); i < cell.getExpectColumnIndex() + cell.getWidthNum(); i++) {
                for (int j = cell.getExpectRowIndex(); j < cell.getExpectRowIndex() + cell.getHeightNum(); j++) {
                    tempCellHold[j][i] = false;
                }
            }
            cellHolds = tempCellHold;

            //更新界面
            requestLayout();
            if (type.equals("widget")){
                loadCompleted.onRemoveResizeFrame(id);
            }
        }
    }

    int childWidthSpec, childHeightSpec, childExpectCellWidthNum, childExpectCellHeightNum;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        removeAllViews();
        perCellWidth = getMeasuredWidth() / columns;
        perCellHeight = getMeasuredHeight() / rows;
        // 获取到getMeasuredWidth后，进行一次cell的测量
        for (Cell cell : cells) {
            initCell(cell);
        }
    }

    /**
     * 为Cell测量并赋值
     * @param cell
     */
    private void initCell(Cell cell) {
        View child = cell.getContentView();
        addView(child);
        childWidthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.AT_MOST);
        childHeightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.AT_MOST);
        measureChild(child, childWidthSpec, childHeightSpec);
        //计算出cell要占据几格
        if (!(cell.getWidthNum()!=0 && cell.getHeightNum()!=0)){
            childExpectCellWidthNum = (int) Math.ceil(child.getMeasuredWidth() / (perCellWidth * 1.0f));
            childExpectCellHeightNum = (int) Math.ceil(child.getMeasuredHeight() / (perCellHeight * 1.0f));
            cell.setWidthNum(childExpectCellWidthNum);
            cell.setHeightNum(childExpectCellHeightNum);
        }

    }

    /**
     * 排列所有Cell
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int nowL, nowT, locateX, locateY;
        int cellWidth, cellHeight;
        needRemoveCells.clear();
        for (Cell cell : cells) {
            if (cell.getExpectColumnIndex() >= 0 && cell.getExpectRowIndex() >= 0) {
                locateX = cell.getExpectColumnIndex();
                locateY = cell.getExpectRowIndex();
            } else {
                Point p = findLeftAndTop(cell);
                cell.setExpectRowIndex(p.x);
                cell.setExpectColumnIndex(p.y);

                locateX = cell.getExpectColumnIndex();
                locateY = cell.getExpectRowIndex();
                if (p.x == -1 || p.y == -1) {
                    Toast.makeText(context,"没有足够空间放置",Toast.LENGTH_SHORT).show();
                    needRemoveCells.add(cell);
                    continue;
                }
            }

            nowL = locateX * perCellWidth;
            nowT = locateY * perCellHeight;

            cell.setLocationX(nowL);
            cell.setLocationY(nowT);

            //根据格数计算出宽高
            cellWidth = cell.getWidthNum() * perCellWidth;
            cellHeight = cell.getHeightNum() * perCellHeight;

            cell.setCellWidth(cellWidth);
            cell.setCellHeight(cellHeight);

            //修改cell的layoutParam的大小，不然会导致cell的view中的gravity失效
            cell.getContentView().getLayoutParams().width = cellWidth;
            cell.getContentView().getLayoutParams().height = cellHeight;
            cell.getContentView().layout(nowL, nowT, nowL + cellWidth, nowT + cellHeight);

            String id = cell.getId();

            if ("widget".equals(cell.getTag()) && !widgets.containsKey(id)){
                loadCompleted.Completed(cell,cell.getContentView());
                widgets.put(id,cell);

                cell.getContentView().requestLayout();

            }else if ("widget".equals(cell.getTag()) && widgets.containsKey(id)){
                if (widgets.get(id).getId().equals(cell.getId())){
                    loadCompleted.onMove(cell.getId());
                }
            }
            if ("app".equals(cell.getTag())){

                cell.setClickListener(launcherIntent -> loadCompleted.onLauncherApp(launcherIntent));
            }
        }
        for (Cell needRemoveCell : needRemoveCells) {
            removeView(needRemoveCell.getContentView());
        }

        cells.removeAll(needRemoveCells);

        Log.e("onLayout","onLayoutonLayoutonLayout");
    }

    /**
     * 查找足够的空间放置
     * @param cell
     * @return
     */
    private Point findLeftAndTop(Cell cell) {
        Point result = new Point(-1, -1);
        boolean isEnough;
        for (int row = 0; row <= rows - cell.getHeightNum(); row++) {
            for (int column = 0; column <= columns - cell.getWidthNum(); column++) {
                isEnough = checkIsEnough(cellHolds, column, row, cell.getWidthNum(), cell.getHeightNum());
                if (isEnough) {
                    fillCellLayout(column, row, cell.getWidthNum(), cell.getHeightNum());
                    result.set(row, column);
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * 判断是否可以放置cell
     * @param myCellHolds
     * @param startX
     * @param startY
     * @param width
     * @param height
     * @return
     */
    private boolean checkIsEnough(boolean[][] myCellHolds, int startX, int startY, int width, int height) {
        if (startX + width > columns || startY + height > rows) {
            return false;
        }
        for (int i = startX; i < startX + width; i++) {
            for (int j = startY; j < startY + height; j++) {
                if (myCellHolds[j][i]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 将被放置的位置标记
     * @param startX
     * @param startY
     * @param width
     * @param height
     */
    private void fillCellLayout(int startX, int startY, int width, int height) {
        if (startX + width > columns || startY + height > rows) {
            Log.e("标记失败","标记失败");
            return;
        }
        for (int i = startX; i < startX + width; i++) {
            for (int j = startY; j < startY + height; j++) {
                cellHolds[j][i] = true;
                Log.e("标记","j="+j+"  i="+i);
            }
        }
    }

    /**
     * 重新调整Cell大小
     * @param id
     * @param deltaX
     * @param deltaY
     * @param direction
     */
    public void resizeCell(String id,int deltaX,int deltaY,int direction){

        Cell cell = widgets.get(id);
        float tempColumnIndex = 1f*deltaX/perCellWidth;
        float tempRowIndex = 1f*deltaY / perCellHeight;

        if(cell != null){

            Log.e("标记xxx",cellHolds[4][1] +"");

            switch (direction){
                case 0:
                    //左
                    if (cell.expectColumnIndex - tempColumnIndex > 0.5){

                        boolean[][] tempCellHold = cellHolds.clone();
                        for (int i = cell.getExpectColumnIndex(); i < cell.getExpectColumnIndex() + cell.getWidthNum(); i++) {
                            for (int j = cell.getExpectRowIndex(); j < cell.getExpectRowIndex() + cell.getHeightNum(); j++) {
                                if (j<columns && i<rows){
                                    tempCellHold[j][i] = false;
                                }
                            }
                        }

                        //判断是否可以放置
                        if (!checkIsEnough(tempCellHold,(int) tempColumnIndex,cell.expectRowIndex,cell.widthNum+1,cell.heightNum)){
                            break;
                        }

                        cellHolds = tempCellHold;
                        cell.setExpectColumnIndex((int) tempColumnIndex);
                        cell.setWidthNum(cell.widthNum+1);
                        fillCellLayout(cell.getExpectColumnIndex(), cell.getExpectRowIndex(), cell.getWidthNum(), cell.getHeightNum());
                        requestLayout();
                        cell.contentView.invalidate();
                        Log.e("left_out",tempColumnIndex+ "  " + cell.expectColumnIndex);

                    }else if (tempColumnIndex - cell.expectColumnIndex > 0.5){

                        if (cell.getWidthNum() > 1){
                            boolean[][] tempCellHold = cellHolds.clone();
                            for (int i = cell.getExpectColumnIndex(); i < cell.getExpectColumnIndex() + cell.getWidthNum(); i++) {
                                for (int j = cell.getExpectRowIndex(); j < cell.getExpectRowIndex() + cell.getHeightNum(); j++) {

                                    if (j<columns && i<rows){
                                        tempCellHold[j][i] = false;
                                    }
                                }
                            }

                            //判断是否可以放置
                            if (!checkIsEnough(tempCellHold,(int) tempColumnIndex+1,cell.expectRowIndex,cell.widthNum+1,cell.heightNum)){
                                break;
                            }

                            cellHolds = tempCellHold;
                            cell.setExpectColumnIndex((int) tempColumnIndex+1);
                            cell.setWidthNum(cell.widthNum-1);
                            fillCellLayout(cell.getExpectColumnIndex(), cell.getExpectRowIndex(), cell.getWidthNum(), cell.getHeightNum());
                            requestLayout();
                            cell.contentView.invalidate();
                            Log.e("left_in",cell.getExpectColumnIndex()+"");
                        }
                    }

                    break;
                case 1:
                    //右
                    if (tempColumnIndex - (cell.expectColumnIndex+cell.widthNum) > 0.5){

                        boolean[][] tempCellHold = cellHolds.clone();
                        for (int i = cell.getExpectColumnIndex(); i < cell.getExpectColumnIndex() + cell.getWidthNum(); i++) {
                            for (int j = cell.getExpectRowIndex(); j < cell.getExpectRowIndex() + cell.getHeightNum(); j++) {

                                if (j<columns && i<rows){
                                    tempCellHold[j][i] = false;
                                }
                            }
                        }

                        //判断是否可以放置
                        if (!checkIsEnough(tempCellHold,cell.expectColumnIndex,cell.expectRowIndex,cell.widthNum+1,cell.heightNum)){
                            break;
                        }

                        cellHolds = tempCellHold;
                        cell.setWidthNum(cell.widthNum+1);
                        fillCellLayout(cell.getExpectColumnIndex(), cell.getExpectRowIndex(), cell.getWidthNum(), cell.getHeightNum());
                        requestLayout();
                        cell.contentView.invalidate();
                        Log.e("right_out",tempColumnIndex+ "  " + cell.expectColumnIndex);
                    }else if (cell.expectColumnIndex+cell.widthNum - tempColumnIndex > 0.5){

                        if (cell.getWidthNum() > 1){
                            boolean[][] tempCellHold = cellHolds.clone();
                            for (int i = cell.getExpectColumnIndex(); i < cell.getExpectColumnIndex() + cell.getWidthNum(); i++) {
                                for (int j = cell.getExpectRowIndex(); j < cell.getExpectRowIndex() + cell.getHeightNum(); j++) {

                                    if (j<columns && i<rows){
                                        tempCellHold[j][i] = false;
                                    }
                                }
                            }

                            //判断是否可以放置
                            if (!checkIsEnough(tempCellHold,cell.expectColumnIndex,cell.expectRowIndex,cell.widthNum-1,cell.heightNum)){
                                break;
                            }

                            cellHolds = tempCellHold;
                            cell.setWidthNum(cell.widthNum-1);
                            fillCellLayout(cell.getExpectColumnIndex(), cell.getExpectRowIndex(), cell.getWidthNum(), cell.getHeightNum());
                            requestLayout();
                            cell.contentView.invalidate();
                            Log.e("right_in",cell.getExpectColumnIndex()+"");
                        }
                    }

                    break;
                case 2:
                    //上
                    if (cell.expectRowIndex - tempRowIndex > 0.5){

                        boolean[][] tempCellHold = cellHolds.clone();
                        for (int i = cell.getExpectColumnIndex(); i < cell.getExpectColumnIndex() + cell.getWidthNum(); i++) {
                            for (int j = cell.getExpectRowIndex(); j < cell.getExpectRowIndex() + cell.getHeightNum(); j++) {

                                if (j<columns && i<rows){
                                    tempCellHold[j][i] = false;
                                }
                            }
                        }

                        //判断是否可以放置
                        if (!checkIsEnough(tempCellHold,cell.expectColumnIndex,(int) tempRowIndex,cell.widthNum,cell.heightNum+1)){
                            break;
                        }

                        cellHolds = tempCellHold;
                        cell.setExpectRowIndex((int) tempRowIndex);
                        cell.setHeightNum(cell.heightNum+1);
                        fillCellLayout(cell.getExpectColumnIndex(), cell.getExpectRowIndex(), cell.getWidthNum(), cell.getHeightNum());
                        requestLayout();
                        cell.contentView.invalidate();
                        Log.e("top_out",tempRowIndex+ "  " + cell.expectRowIndex);
                    }else if (tempRowIndex - cell.expectRowIndex > 0.5){

                        if (cell.getHeightNum() > 1){
                            boolean[][] tempCellHold = cellHolds.clone();
                            for (int i = cell.getExpectColumnIndex(); i < cell.getExpectColumnIndex() + cell.getWidthNum(); i++) {
                                for (int j = cell.getExpectRowIndex(); j < cell.getExpectRowIndex() + cell.getHeightNum(); j++) {

                                    if (j<columns && i<rows){
                                        tempCellHold[j][i] = false;
                                    }
                                }
                            }

                            //判断是否可以放置
                            if (!checkIsEnough(tempCellHold,cell.expectColumnIndex,(int) tempRowIndex+1,cell.widthNum,cell.heightNum-1)){
                                break;
                            }

                            cellHolds = tempCellHold;
                            cell.setExpectRowIndex((int) tempRowIndex+1);
                            cell.setHeightNum(cell.heightNum-1);
                            fillCellLayout(cell.getExpectColumnIndex(), cell.getExpectRowIndex(), cell.getWidthNum(), cell.getHeightNum());
                            requestLayout();
                            cell.contentView.invalidate();
                            Log.e("top_in",cell.getExpectRowIndex()+"");
                        }
                    }

                    break;
                case 3:
                    //下
                    if (tempRowIndex - (cell.expectRowIndex+cell.heightNum) > 0.5){

                        boolean[][] tempCellHold = cellHolds.clone();
                        for (int i = cell.getExpectColumnIndex(); i < cell.getExpectColumnIndex() + cell.getWidthNum(); i++) {
                            for (int j = cell.getExpectRowIndex(); j < cell.getExpectRowIndex() + cell.getHeightNum(); j++) {

                                if (j<columns && i<rows){
                                    tempCellHold[j][i] = false;
                                }
                            }
                        }

                        //判断是否可以放置
                        if (!checkIsEnough(tempCellHold,cell.expectColumnIndex,cell.expectRowIndex,cell.widthNum,cell.heightNum+1)){
                            break;
                        }

                        cellHolds = tempCellHold;
                        cell.setHeightNum(cell.heightNum+1);
                        fillCellLayout(cell.getExpectColumnIndex(), cell.getExpectRowIndex(), cell.getWidthNum(), cell.getHeightNum());
                        requestLayout();
                        cell.contentView.invalidate();
                        Log.e("bottom_out",tempRowIndex+ "  " + cell.expectRowIndex);
                    }else if (cell.expectRowIndex+cell.heightNum - tempRowIndex > 0.5){

                        if (cell.getHeightNum() > 1){
                            boolean[][] tempCellHold = cellHolds.clone();
                            for (int i = cell.getExpectColumnIndex(); i < cell.getExpectColumnIndex() + cell.getWidthNum(); i++) {
                                for (int j = cell.getExpectRowIndex(); j < cell.getExpectRowIndex() + cell.getHeightNum(); j++) {

                                    if (j<columns && i<rows){
                                        tempCellHold[j][i] = false;
                                    }
                                }
                            }

                            //判断是否可以放置
                            if (!checkIsEnough(tempCellHold,cell.expectColumnIndex,cell.expectRowIndex,cell.widthNum,(cell.heightNum-1))){
                                break;
                            }

                            cellHolds = tempCellHold;
                            cell.setHeightNum(cell.heightNum-1);
                            fillCellLayout(cell.getExpectColumnIndex(), cell.getExpectRowIndex(), cell.getWidthNum(), cell.getHeightNum());
                            requestLayout();
                            cell.contentView.invalidate();
                            Log.e("bottom_in",cell.getExpectRowIndex()+"");
                        }
                    }

                    break;
                default:
                    break;
            }

            //更新数据库
            updateCell(cell);
        }
    }

    public void invalidateCell(){
        requestLayout();
    }

    private boolean[][] tempCellHolds;
    //记录移动次数
    private int moveFrequency;

    @Override
    public boolean onDrag(View v, DragEvent event) {
        Cell cell = (Cell) event.getLocalState();
        if(cell != null){

            Log.e("开始移动","Drag");

            int tempColumnIndex = (int) (event.getX() / perCellWidth);
            int tempRowIndex = (int) (event.getY() / perCellHeight);

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    moveFrequency = 0;
                    tempCellHolds = cellHolds.clone();
                    for (int i = cell.getExpectColumnIndex(); i < cell.getExpectColumnIndex() + cell.getWidthNum(); i++) {
                        for (int j = cell.getExpectRowIndex(); j < cell.getExpectRowIndex() + cell.getHeightNum(); j++) {
                            tempCellHolds[j][i] = false;
                        }
                    }

                    //弹出移除popup
                    loadCompleted.onDragStarted(cell.id,cell.locationX,cell.locationY);

                    break;
                case DragEvent.ACTION_DRAG_ENTERED:

                    break;
                case DragEvent.ACTION_DRAG_LOCATION:

                    if (tempColumnIndex != cell.expectColumnIndex || tempRowIndex != cell.expectRowIndex){
                        moveFrequency++;
                        if (moveFrequency > 1)
                            loadCompleted.onDragLocation();
                    }

                    if (checkIsEnough(tempCellHolds, tempColumnIndex, tempRowIndex, cell.getWidthNum(), cell.getHeightNum())) {
                        cellHolds = tempCellHolds;
                        cell.setExpectColumnIndex(tempColumnIndex);
                        cell.setExpectRowIndex(tempRowIndex);
                        requestLayout();
                    }

                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    cell.setLongClick(true);
                    break;
                case DragEvent.ACTION_DROP:

                    if (checkIsEnough(tempCellHolds, tempColumnIndex, tempRowIndex, cell.getWidthNum(), cell.getHeightNum())) {
                        cellHolds = tempCellHolds;
                        fillCellLayout(tempColumnIndex, tempRowIndex, cell.getWidthNum(), cell.getHeightNum());
                        cell.setExpectColumnIndex(tempColumnIndex);
                        cell.setExpectRowIndex(tempRowIndex);
                        cell.setLongClick(true);
                        requestLayout();
                    }
                    cell.getContentView().setVisibility(View.VISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:

                    break;
                default:break;
            }

            cell.setLongClick(true);
        }

        if (cell != null){
            //更新数据库
            updateCell(cell);
        }
        return true;
    }

    /**
     * 保存Cell到数据
     * @param cell
     */
    private void updateCell(Cell cell){
        AssistantBean cellBean = new AssistantBean();
        cellBean.setLauncherId(cell.id);
        cellBean.setLauncherType(cell.tag);
        cellBean.setLauncherX(cell.expectColumnIndex);
        cellBean.setLauncherY(cell.expectRowIndex);
        cellBean.setLauncherWidth(cell.widthNum);
        cellBean.setLauncherHeight(cell.heightNum);

        launcherDBOperate.update(cellBean);
    }

    /**
     * 从数据库删除Cell
     * @param id
     */
    private void deleteCell(String id){
        launcherDBOperate.delete(id);
    }

    public void close(){
        launcherDBOperate.close();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        close();
    }

    /**
     * 设置监听接口
     * @param loadCompleted
     */
    public void setLoadCompleted(LoadCompleted loadCompleted) {
        this.loadCompleted = loadCompleted;
    }

    public static class Cell {
        /**
         * tag，用于识别图标还是小部件
         */
        private String tag;
        /**
         * id，用于识别不同的控件
         */
        private String id;
        /**
         * Cell content view
         */
        private View contentView;
        /**
         * 横向占据的格数
         */
        private int widthNum;
        /**
         * 纵向占据的格数
         */
        private int heightNum;
        /**
         * 计算出的可摆放的位置
         */
        private int expectColumnIndex = -1, expectRowIndex = -1;
        /**
         * 当前占用位置的坐标
         */
        private int locationX,locationY;
        /**
         * Cell宽高
         */
        private int cellWidth,cellHeight;
        /**
         * 解决多次触发导致数据不一致问题
         */
        private boolean isLongClick = true;

        private File iconFile;

        private String appIntent;

        public Cell(String id,String tag, View view,String appIntent) {
            this.tag = tag;
            this.id = id;
            this.contentView = view;
            this.appIntent = appIntent;

            this.contentView.setOnLongClickListener(v -> {
                if (isLongClick){
                    isLongClick = false;

                    Log.e("ssssss","长按");
                    DragShadowBuilder builder = new DragShadowBuilder(v);
                    v.startDrag(null, builder, Cell.this, 0);
                }else {
                    Log.e("ssssss","长按失败");
                }
                return true;
            });

            if (appIntent != null && tag.equals("app")){

                this.contentView.setOnClickListener(v -> {
                    if (clickListener != null){
                        clickListener.onClick(appIntent);
                    }
                });

            }else {
                Log.e("launcherFilde","launcherFilde");
            }
        }

        interface OnItemClickListener{
            void onClick(String launcherIntent);
        }

        private OnItemClickListener clickListener;

        public void setClickListener(OnItemClickListener clickListener ) {
            this.clickListener = clickListener;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public View getContentView() {
            return contentView;
        }

        public void setContentView(View contentView) {
            this.contentView = contentView;
        }

        public int getWidthNum() {
            return widthNum;
        }

        public void setWidthNum(int widthNum) {
            this.widthNum = widthNum;
        }

        public void setHeightNum(int heightNum) {
            this.heightNum = heightNum;
        }

        public int getHeightNum() {
            return heightNum;
        }

        public int getExpectColumnIndex() {
            return expectColumnIndex;
        }

        public void setExpectColumnIndex(int expectColumnIndex) {
            this.expectColumnIndex = expectColumnIndex;
        }

        public int getExpectRowIndex() {
            return expectRowIndex;
        }

        public void setExpectRowIndex(int expectRowIndex) {
            this.expectRowIndex = expectRowIndex;
        }

        public int getLocationX() {
            return locationX;
        }

        public int getLocationY() {
            return locationY;
        }

        public void setLocationX(int locationX) {
            this.locationX = locationX;
        }

        public void setLocationY(int locationY) {
            this.locationY = locationY;
        }

        public int getCellWidth() {
            return cellWidth;
        }

        public void setCellWidth(int cellWidth) {
            this.cellWidth = cellWidth;
        }

        public int getCellHeight() {
            return cellHeight;
        }

        public void setCellHeight(int cellHeight) {
            this.cellHeight = cellHeight;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isLongClick() {
            return isLongClick;
        }

        public void setLongClick(boolean longClick) {
            isLongClick = longClick;
        }

        public File getIconFile() {
            return iconFile;
        }

        public void setIconFile(File iconFile) {
            this.iconFile = iconFile;
        }

        public String getAppIntent(){
            return appIntent;
        }

        public void setAppIntent(String appIntent){
            this.appIntent = appIntent;
        }
    }
}