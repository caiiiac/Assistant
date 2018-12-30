package org.andcreator.assistant.layout;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomBottomSheetBehaviour<V extends View> extends BottomSheetBehavior {
    public CustomBottomSheetBehaviour() {
        super();
    }

    public CustomBottomSheetBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        return false;
    }
}