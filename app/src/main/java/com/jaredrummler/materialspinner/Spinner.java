//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jaredrummler.materialspinner;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.jaredrummler.materialspinner.R.dimen;
import com.jaredrummler.materialspinner.R.drawable;
import com.jaredrummler.materialspinner.R.styleable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class Spinner extends AppCompatTextView {
    private Spinner.OnNothingSelectedListener onNothingSelectedListener;
    private Spinner.OnItemSelectedListener onItemSelectedListener;
    private MaterialSpinnerBaseAdapter adapter;
    private PopupWindow popupWindow;
    private ListView listView;
    private Drawable arrowDrawable;
    private boolean hideArrow;
    private boolean nothingSelected;
    private int popupWindowMaxHeight;
    private int popupWindowHeight;
    private int popupWindowWidth;
    private int selectedIndex;
    private int backgroundColor;
    private int arrowColor;
    private int arrowColorDisabled;
    private int textColor;
    private int numberOfItems;

    public Spinner(Context context) {
        super(context);
        this.init(context, (AttributeSet)null);
    }

    public Spinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs);
    }

    @SuppressLint("WrongConstant")
    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, styleable.MaterialSpinner);
        int defaultColor = this.getTextColors().getDefaultColor();
        boolean rtl = Utils.isRtl(context);

        try {
            this.backgroundColor = ta.getColor(styleable.MaterialSpinner_ms_background_color, -1);
            this.textColor = ta.getColor(styleable.MaterialSpinner_ms_text_color, defaultColor);
            this.arrowColor = ta.getColor(styleable.MaterialSpinner_ms_arrow_tint, this.textColor);
            this.hideArrow = ta.getBoolean(styleable.MaterialSpinner_ms_hide_arrow, false);
            this.popupWindowMaxHeight = ta.getDimensionPixelSize(styleable.MaterialSpinner_ms_dropdown_max_height, 0);
            this.popupWindowHeight = ta.getLayoutDimension(styleable.MaterialSpinner_ms_dropdown_height, -2);
            this.arrowColorDisabled = Utils.lighter(this.arrowColor, 0.8F);
        } finally {
            ta.recycle();
        }

        Resources resources = this.getResources();
        int right;
        int bottom;
        int top;
        int left = right = bottom = top = resources.getDimensionPixelSize(dimen.ms__padding_top);
        if(rtl) {
            right = resources.getDimensionPixelSize(dimen.ms__padding_left);
        } else {
            left = resources.getDimensionPixelSize(dimen.ms__padding_left);
        }

        this.setGravity(8388627);
        this.setClickable(true);
        this.setPadding(left, top, right, bottom);
        this.setBackgroundResource(drawable.ms__selector);
        if(VERSION.SDK_INT >= 17 && rtl) {
            this.setLayoutDirection(1);
            this.setTextDirection(4);
        }

        if(!this.hideArrow) {
            this.arrowDrawable = Utils.getDrawable(context, drawable.ms__arrow).mutate();
            this.arrowDrawable.setColorFilter(this.arrowColor, Mode.SRC_IN);
            if(rtl) {
                this.setCompoundDrawablesWithIntrinsicBounds(this.arrowDrawable, (Drawable)null, (Drawable)null, (Drawable)null);
            } else {
                this.setCompoundDrawablesWithIntrinsicBounds((Drawable)null, (Drawable)null, this.arrowDrawable, (Drawable)null);
            }
        }

        this.listView = new ListView(context);
        this.listView.setId(this.getId());
        this.listView.setDivider((Drawable)null);
        this.listView.setItemsCanFocus(true);
        this.listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position >= Spinner.this.selectedIndex && position < Spinner.this.adapter.getCount()) {
                    ++position;
                }

                Spinner.this.selectedIndex = position;
                Spinner.this.nothingSelected = false;
                Object item = Spinner.this.adapter.get(position);
                Spinner.this.adapter.notifyItemSelected(position);
                Spinner.this.setText(item.toString());
                Spinner.this.collapse();
                if(Spinner.this.onItemSelectedListener != null) {
                    Spinner.this.onItemSelectedListener.onItemSelected(Spinner.this, position, id, item);
                }

            }
        });
        this.popupWindow = new PopupWindow(context);
        this.popupWindow.setContentView(this.listView);
        this.popupWindow.setOutsideTouchable(true);
        this.popupWindow.setFocusable(true);
        if(VERSION.SDK_INT >= 21) {
            this.popupWindow.setElevation(16.0F);
            this.popupWindow.setBackgroundDrawable(Utils.getDrawable(context, drawable.ms__drawable));
        } else {
            this.popupWindow.setBackgroundDrawable(Utils.getDrawable(context, drawable.ms__drop_down_shadow));
        }

        if(this.backgroundColor != -1) {
            this.setBackgroundColor(this.backgroundColor);
        }

        if(this.textColor != defaultColor) {
            this.setTextColor(this.textColor);
        }

        this.popupWindow.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                if(Spinner.this.nothingSelected && Spinner.this.onNothingSelectedListener != null) {
                    Spinner.this.onNothingSelectedListener.onNothingSelected(Spinner.this);
                }

                if(!Spinner.this.hideArrow) {
                    Spinner.this.animateArrow(false);
                }

            }
        });
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.popupWindow.setHeight(this.calculatePopupWindowHeight());
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if(event.getAction() == 1 && this.isEnabled() && this.isClickable()) {
            if(!this.popupWindow.isShowing()) {
                this.expand();
            } else {
                this.collapse();
            }
        }

        return super.onTouchEvent(event);
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        Drawable background = this.getBackground();
        if(background instanceof StateListDrawable) {
            try {
                Method getStateDrawable = StateListDrawable.class.getDeclaredMethod("getStateDrawable", new Class[]{Integer.TYPE});
                if(!getStateDrawable.isAccessible()) {
                    getStateDrawable.setAccessible(true);
                }

                int[] colors = new int[]{Utils.darker(color, 0.85F), color};

                for(int i = 0; i < colors.length; ++i) {
                    ColorDrawable drawable = (ColorDrawable)getStateDrawable.invoke(background, new Object[]{Integer.valueOf(i)});
                    drawable.setColor(colors[i]);
                }
            } catch (Exception var7) {
                Log.e("MaterialSpinner", "Error setting background color", var7);
            }
        } else if(background != null) {
            background.setColorFilter(color, Mode.SRC_IN);
        }

        this.popupWindow.getBackground().setColorFilter(color, Mode.SRC_IN);
    }

    public void setTextColor(int color) {
        this.textColor = color;
        super.setTextColor(color);
    }

    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("state", super.onSaveInstanceState());
        bundle.putInt("selected_index", this.selectedIndex);
        if(this.popupWindow != null) {
            bundle.putBoolean("is_popup_showing", this.popupWindow.isShowing());
            this.collapse();
        } else {
            bundle.putBoolean("is_popup_showing", false);
        }

        return bundle;
    }

    public void onRestoreInstanceState(Parcelable savedState) {
        if(savedState instanceof Bundle) {
            Bundle bundle = (Bundle)savedState;
            this.selectedIndex = bundle.getInt("selected_index");
            if(this.adapter != null) {
                this.setText(this.adapter.get(this.selectedIndex).toString());
                this.adapter.notifyItemSelected(this.selectedIndex);
            }

            if(bundle.getBoolean("is_popup_showing") && this.popupWindow != null) {
                this.post(new Runnable() {
                    public void run() {
                        Spinner.this.expand();
                    }
                });
            }

            savedState = bundle.getParcelable("state");
        }

        super.onRestoreInstanceState(savedState);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(this.arrowDrawable != null) {
            this.arrowDrawable.setColorFilter(enabled?this.arrowColor:this.arrowColorDisabled, Mode.SRC_IN);
        }

    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public void setSelectedIndex(int position) {
        if(this.adapter != null) {
            if(position < 0 || position > this.adapter.getCount()) {
                throw new IllegalArgumentException("Position must be lower than adapter count!");
            }

            this.adapter.notifyItemSelected(position);
            this.selectedIndex = position;
            this.setText(this.adapter.get(position).toString());
        }

    }

    public void setOnItemSelectedListener(@Nullable Spinner.OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public void setOnNothingSelectedListener(@Nullable Spinner.OnNothingSelectedListener onNothingSelectedListener) {
        this.onNothingSelectedListener = onNothingSelectedListener;
    }

    public <T> void setItems(@NonNull List<T> items) {
        this.numberOfItems = items.size();
        this.adapter = (new MaterialSpinnerAdapter(this.getContext(), items)).setTextColor(this.textColor);
        this.setAdapterInternal(this.adapter);
    }

    public <T> void setItems(@NonNull T... items) {
        this.setItems(Arrays.asList(items));
    }

    public <T> List<T> getItems() {
        return this.adapter == null?null:this.adapter.getItems();
    }

    public void setAdapter(@NonNull ListAdapter adapter) {
        this.adapter = new MaterialSpinnerAdapterWrapper(this.getContext(), adapter);
        this.setAdapterInternal(this.adapter);
    }

    public <T> void setAdapter(MaterialSpinnerAdapter<T> adapter) {
        this.adapter = adapter;
        this.setAdapterInternal(adapter);
    }

    private void setAdapterInternal(@NonNull MaterialSpinnerBaseAdapter adapter) {
        this.listView.setAdapter(adapter);
        if(this.selectedIndex >= this.numberOfItems) {
            this.selectedIndex = 0;
        }

        this.setText(adapter.get(this.selectedIndex).toString());
    }

    public void expand() {
        if(!this.hideArrow) {
            this.animateArrow(true);
        }

        this.nothingSelected = true;
        if(VERSION.SDK_INT >= 23) {
            this.popupWindow.setOverlapAnchor(false);
            this.popupWindow.showAsDropDown(this);
        } else {
            int[] location = new int[2];
            this.getLocationOnScreen(location);
            int x = location[0];
            int y = this.getHeight() + location[1];
            this.popupWindow.showAtLocation(this, 8388659, x, y);
        }

    }

    public void collapse() {
        if(!this.hideArrow) {
            this.animateArrow(false);
        }

        this.popupWindow.dismiss();
    }

    public void setArrowColor(@ColorInt int color) {
        this.arrowColor = color;
        this.arrowColorDisabled = Utils.lighter(this.arrowColor, 0.8F);
        if(this.arrowDrawable != null) {
            this.arrowDrawable.setColorFilter(this.arrowColor, Mode.SRC_IN);
        }

    }

    private void animateArrow(boolean shouldRotateUp) {
        int start = shouldRotateUp?0:10000;
        int end = shouldRotateUp?10000:0;
        ObjectAnimator animator = ObjectAnimator.ofInt(this.arrowDrawable, "level", new int[]{start, end});
        animator.start();
    }

    public void setDropdownMaxHeight(int height) {
        this.popupWindowMaxHeight = height;
        this.popupWindow.setHeight(this.calculatePopupWindowHeight());
    }

    public void setDropdownHeight(int height) {
        this.popupWindowHeight = height;
        this.popupWindow.setHeight(this.calculatePopupWindowHeight());
    }

    public void setDropdownWidth(int width) {
        this.popupWindowHeight = width;
        this.getPopupWindow().setHeight(popupWindowWidth);
    }

    public int calculatePopupWindowHeight() {
        if(this.adapter == null) {
            return -2;
        } else {
            float listViewHeight = (float)this.adapter.getCount() * this.getResources().getDimension(dimen.ms__item_height);
            return this.popupWindowMaxHeight > 0 && listViewHeight > (float)this.popupWindowMaxHeight?this.popupWindowMaxHeight:(this.popupWindowHeight != -1 && this.popupWindowHeight != -2 && (float)this.popupWindowHeight <= listViewHeight?this.popupWindowHeight:-2);
        }
    }

    public MaterialSpinnerBaseAdapter getAdapter(){
        return adapter;
    }

    public PopupWindow getPopupWindow() {
        return this.popupWindow;
    }

    public interface OnNothingSelectedListener {
        void onNothingSelected(Spinner var1);
    }

    public interface OnItemSelectedListener<T> {
        void onItemSelected(Spinner var1, int var2, long var3, T var5);
    }
}
