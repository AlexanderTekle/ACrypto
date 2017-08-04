package dev.dworks.apps.acrypto.view;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.components.YAxis;

import dev.dworks.apps.acrypto.utils.Utils;

/**
 * Created by HaKr on 04-Aug-17.
 */

public class LineChart extends com.github.mikephil.charting.charts.LineChart {
    public LineChart(Context context) {
        super(context);
    }

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public YAxis getAxisLeft() {
        return Utils.isRTL() ? super.getAxisRight() : super.getAxisLeft();
    }

    @Override
    public YAxis getAxisRight() {
        return Utils.isRTL() ? super.getAxisLeft() : super.getAxisRight();
    }
}
