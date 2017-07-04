package dev.dworks.apps.acrypto.common;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by HaKr on 19/06/17.
 */

public class ChartOnTouchListener implements View.OnTouchListener {
    ScrollView mScrollView;

    public ChartOnTouchListener(ScrollView scrollView){
        mScrollView = scrollView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                //mScrollView.requestDisallowInterceptTouchEvent(true);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                //mScrollView.requestDisallowInterceptTouchEvent(false);
                break;
            }
        }

        return false;
    }
}
