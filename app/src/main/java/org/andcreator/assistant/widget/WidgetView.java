package org.andcreator.assistant.widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;

/**
 * Important!! ReadMe
 * We are now using the old method to detect widget long press, this fixed all the "randomly disappearing" behaviour of widgets
 * However, you will need to move a bit to trigger the long press, when dragging. But this can be useful, as we can implement a
 * popup menu of the widget when it was being pressed.
 */
public class WidgetView extends AppWidgetHostView {
    private OnTouchListener _onTouchListener;
    private OnLongClickListener _longClick;
    private long _down;

    public WidgetView(Context context) {
        super(context);
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        _onTouchListener = onTouchListener;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        _longClick = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (_onTouchListener != null)
            _onTouchListener.onTouch(this, ev);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                _down = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                boolean upVal = System.currentTimeMillis() - _down > 300L;
                if (upVal) {
                    _longClick.onLongClick(WidgetView.this);
                }
                break;
                default:break;
        }
        return false;
    }
}