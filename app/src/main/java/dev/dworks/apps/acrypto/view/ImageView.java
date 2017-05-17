package dev.dworks.apps.acrypto.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.StyleableRes;
import android.util.AttributeSet;

import dev.dworks.apps.acrypto.R;

/**
 * Created by HaKr on 11/04/15.
 */

/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
public class ImageView extends com.android.volley.ui.NetworkImageView {
    private final int[] attrsArray = {
            android.R.attr.src,
            R.attr.srcCompat
    };
    @StyleableRes
    int index = 1;

    public ImageView(Context context) {
        this(context, null);
    }

    public ImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray a = context.obtainStyledAttributes(attrs, attrsArray);
        int value = a.getResourceId(0, -1);
        if (value == -1) {
            value = a.getResourceId(index, -1);
        }
        setDefaultImageResId(value);
        a.recycle();
    }
}