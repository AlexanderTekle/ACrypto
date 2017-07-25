package dev.dworks.apps.acrypto.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.R;

/**
 * Created by HaKr on 25-Jul-17.
 */

public class SimpleSpinner extends android.support.v7.widget.AppCompatSpinner{
    boolean userSelect = false;

    public SimpleSpinner(Context context) {
        super(context);
    }

    public SimpleSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        userSelect = true;
        return super.onTouchEvent(event);
    }

    @Override
    public void setOnItemSelectedListener(@Nullable AdapterView.OnItemSelectedListener listener) {
        super.setOnItemSelectedListener(new OnItemSelectedListener(listener));
    }

    public <T> void setItems(@NonNull ArrayList<T> items) {
        setItems(items, R.layout.item_spinner_default);
    }

    public <T> void setItems(@NonNull ArrayList<T> items, int layoutId) {
        ArrayAdapter adapter = (ArrayAdapter) getAdapter();
        if(null == adapter){
            adapter = new ArrayAdapter<T>(this.getContext(), layoutId);
            adapter.setDropDownViewResource(R.layout.item_spinner_default);
            setAdapter(adapter);
        }
        adapter.updateAll(items);
    }

    public void setSelection(String value){
        int index = 0;
        SpinnerAdapter adapter = getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                index = i;
                break; // terminate loop
            }
        }
        setSelection(index);
    }

    public class ArrayAdapter<T> extends android.widget.ArrayAdapter {

        public ArrayAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
        }

        public void updateAll(@Nullable ArrayList<T> items) {
            clear();
            addAll(items);
        }
    }

    public class OnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        private final AdapterView.OnItemSelectedListener mOnItemSelectedListener;
        public OnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener){
            mOnItemSelectedListener = onItemSelectedListener;
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
}
