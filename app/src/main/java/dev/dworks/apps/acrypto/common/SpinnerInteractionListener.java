package dev.dworks.apps.acrypto.common;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

public class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    boolean userSelect = false;
    private final AdapterView.OnItemSelectedListener mOnItemSelectedListener;

    public SpinnerInteractionListener(AdapterView.OnItemSelectedListener onItemSelectedListener){
        mOnItemSelectedListener = onItemSelectedListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        userSelect = true;
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (userSelect) {
            mOnItemSelectedListener.onItemSelected(parent, view, position, id);
            userSelect = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if (userSelect) {
            mOnItemSelectedListener.onNothingSelected(parent);
            userSelect = false;
        }
    }
}