package dev.dworks.apps.acrypto.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LockableViewPager extends ViewPager {
    private boolean swipeable;

    public LockableViewPager(Context context) {
        super(context);
    }

    public LockableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.swipeable = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return swipeable && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return swipeable && super.onInterceptTouchEvent(event);
    }

    public void setSwipeable(boolean swipeable) {
        this.swipeable = swipeable;
    }
}