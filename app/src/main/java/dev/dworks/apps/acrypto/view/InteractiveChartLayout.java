/*
 * Copyright (C) 2017 WordPlat Open Source Project
 *
 *      https://wordplat.com/InteractiveKLineView/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.dworks.apps.acrypto.view;

import android.content.Context;
import android.graphics.RectF;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.wordplat.ikvstockchart.KLineHandler;
import com.wordplat.ikvstockchart.compat.ViewUtils;
import com.wordplat.ikvstockchart.drawing.BOLLDrawing;
import com.wordplat.ikvstockchart.drawing.HighlightDrawing;
import com.wordplat.ikvstockchart.drawing.KDJDrawing;
import com.wordplat.ikvstockchart.drawing.KLineVolumeDrawing;
import com.wordplat.ikvstockchart.drawing.KLineVolumeHighlightDrawing;
import com.wordplat.ikvstockchart.drawing.MACDDrawing;
import com.wordplat.ikvstockchart.drawing.RSIDrawing;
import com.wordplat.ikvstockchart.drawing.StockIndexYLabelDrawing;
import com.wordplat.ikvstockchart.entry.Entry;
import com.wordplat.ikvstockchart.entry.SizeColor;
import com.wordplat.ikvstockchart.entry.StockBOLLIndex;
import com.wordplat.ikvstockchart.entry.StockKDJIndex;
import com.wordplat.ikvstockchart.entry.StockKLineVolumeIndex;
import com.wordplat.ikvstockchart.entry.StockMACDIndex;
import com.wordplat.ikvstockchart.entry.StockRSIIndex;
import com.wordplat.ikvstockchart.marker.XAxisTextMarkerView;
import com.wordplat.ikvstockchart.marker.YAxisTextMarkerView;
import com.wordplat.ikvstockchart.render.KLineRender;

import dev.dworks.apps.acrypto.R;

import static dev.dworks.apps.acrypto.utils.Utils.getColor;

public class InteractiveChartLayout extends FrameLayout implements TabLayout.OnTabSelectedListener{
    private static final String TAG = "InteractiveKLineLayout";

    private Context context;

    private InteractiveKLineView kLineView;
    private KLineHandler kLineHandler;
    private KLineRender kLineRender;

    private StockKLineVolumeIndex volumeIndex;
    private StockMACDIndex macdIndex;
    private StockRSIIndex rsiIndex;
    private StockKDJIndex kdjIndex;
    private StockBOLLIndex bollIndex;

    private int stockMarkerViewHeight;
    private int stockIndexViewHeight;
    private int stockIndexTabHeight;
    private RectF currentRect;
    private TabLayout tabLayout;

    public InteractiveChartLayout(Context context) {
        this(context, null);
    }

    public InteractiveChartLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InteractiveChartLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;

        stockMarkerViewHeight = context.getResources().getDimensionPixelOffset(R.dimen.stock_marker_view_height);
        stockIndexViewHeight = context.getResources().getDimensionPixelOffset(R.dimen.stock_index_view_height);
        stockIndexTabHeight = context.getResources().getDimensionPixelOffset(R.dimen.stock_index_tab_height);

        initUI(context, attrs, defStyleAttr);
    }

    private void initUI(Context context, AttributeSet attrs, int defStyleAttr) {
        kLineView = new InteractiveKLineView(context);
        kLineRender = (KLineRender) kLineView.getRender();

        SizeColor sizeColor = ViewUtils.getSizeColor(context, attrs, defStyleAttr);
        sizeColor.setHighlightSize(4f);
        sizeColor.setHighlightColor(ContextCompat.getColor(context, R.color.colorAccent));
        kLineRender.setSizeColor(sizeColor);

        kLineView.setKLineHandler(new KLineHandler() {
            @Override
            public void onLeftRefresh() {
                if (kLineHandler != null) {
                    kLineHandler.onLeftRefresh();
                }
            }

            @Override
            public void onRightRefresh() {
                if (kLineHandler != null) {
                    kLineHandler.onRightRefresh();
                }
            }

            @Override
            public void onSingleTap(MotionEvent e, float x, float y) {
                if (kLineHandler != null) {
                    kLineHandler.onSingleTap(e, x, y);
                }

                ///onTabClick(x, y);
            }

            @Override
            public void onDoubleTap(MotionEvent e, float x, float y) {
                if (kLineHandler != null) {
                    kLineHandler.onDoubleTap(e, x, y);
                }
            }

            @Override
            public void onHighlight(Entry entry, int entryIndex, float x, float y) {
                if (kLineHandler != null) {
                    kLineHandler.onHighlight(entry, entryIndex, x, y);
                }
            }

            @Override
            public void onCancelHighlight() {
                if (kLineHandler != null) {
                    kLineHandler.onCancelHighlight();
                }
            }
        });

        // Volume

        HighlightDrawing volumeHighlightDrawing = new KLineVolumeHighlightDrawing();
        volumeHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        volumeIndex = new StockKLineVolumeIndex(stockIndexViewHeight);
        volumeIndex.addDrawing(new KLineVolumeDrawing());
        volumeIndex.addDrawing(new StockIndexYLabelDrawing());
        volumeIndex.addDrawing(volumeHighlightDrawing);
        volumeIndex.setPaddingTop(stockIndexTabHeight);
        kLineRender.addStockIndex(volumeIndex);

        // MACD
        HighlightDrawing macdHighlightDrawing = new HighlightDrawing();
        macdHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        macdIndex = new StockMACDIndex(stockIndexViewHeight);
        macdIndex.addDrawing(new MACDDrawing());
        macdIndex.addDrawing(new StockIndexYLabelDrawing());
        macdIndex.addDrawing(macdHighlightDrawing);
        macdIndex.setPaddingTop(stockIndexTabHeight);
        kLineRender.addStockIndex(macdIndex);

        // RSI
        HighlightDrawing rsiHighlightDrawing = new HighlightDrawing();
        rsiHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        rsiIndex = new StockRSIIndex(stockIndexViewHeight);
        rsiIndex.addDrawing(new RSIDrawing());
        rsiIndex.addDrawing(new StockIndexYLabelDrawing());
        rsiIndex.addDrawing(rsiHighlightDrawing);
        rsiIndex.setPaddingTop(stockIndexTabHeight);
        kLineRender.addStockIndex(rsiIndex);

        // KDJ
        HighlightDrawing kdjHighlightDrawing = new HighlightDrawing();
        kdjHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        kdjIndex = new StockKDJIndex(stockIndexViewHeight);
        kdjIndex.addDrawing(new KDJDrawing());
        kdjIndex.addDrawing(new StockIndexYLabelDrawing());
        kdjIndex.addDrawing(kdjHighlightDrawing);
        kdjIndex.setPaddingTop(stockIndexTabHeight);
        kLineRender.addStockIndex(kdjIndex);

        // BOLL
        HighlightDrawing bollHighlightDrawing = new HighlightDrawing();
        bollHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        bollIndex = new StockBOLLIndex(stockIndexViewHeight);
        bollIndex.addDrawing(new BOLLDrawing());
        bollIndex.addDrawing(new StockIndexYLabelDrawing());
        bollIndex.addDrawing(bollHighlightDrawing);
        bollIndex.setPaddingTop(stockIndexTabHeight);
        kLineRender.addStockIndex(bollIndex);

        kLineRender.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));
        kLineRender.addMarkerView(new XAxisTextMarkerView(stockMarkerViewHeight));

        addView(kLineView);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setOnTabSelectedListener(this);

        showVolume();
    }

    public InteractiveKLineView getKLineView() {
        return kLineView;
    }

    public void setKLineHandler(KLineHandler kLineHandler) {
        this.kLineHandler = kLineHandler;
    }

    private void onTabClick(float x, float y) {
        if (currentRect.contains(x, y)) {
            if (volumeIndex.isEnable()) {
                showMACD();
            } else if (macdIndex.isEnable()) {
                showRSI();
            } else if (rsiIndex.isEnable()) {
                showKDJ();
            } else if (kdjIndex.isEnable()) {
                showBOLL();
            } else {
                showVolume();
            }

            if (kLineHandler != null) {
                kLineHandler.onCancelHighlight();
            }

            kLineView.notifyDataSetChanged();
        }
    }

    public void showVolume() {
        volumeIndex.setEnable(true);
        macdIndex.setEnable(false);
        rsiIndex.setEnable(false);
        kdjIndex.setEnable(false);
        bollIndex.setEnable(false);
        currentRect = volumeIndex.getRect();
    }

    public void showMACD() {
        volumeIndex.setEnable(false);
        macdIndex.setEnable(true);
        rsiIndex.setEnable(false);
        kdjIndex.setEnable(false);
        bollIndex.setEnable(false);
        currentRect = macdIndex.getRect();
    }

    public void showRSI() {
        volumeIndex.setEnable(false);
        macdIndex.setEnable(false);
        rsiIndex.setEnable(true);
        kdjIndex.setEnable(false);
        bollIndex.setEnable(false);

        currentRect = rsiIndex.getRect();
    }

    public void showKDJ() {
        volumeIndex.setEnable(false);
        macdIndex.setEnable(false);
        rsiIndex.setEnable(false);
        kdjIndex.setEnable(true);
        bollIndex.setEnable(false);

        currentRect = kdjIndex.getRect();
    }

    public void showBOLL() {
        volumeIndex.setEnable(false);
        macdIndex.setEnable(false);
        rsiIndex.setEnable(false);
        kdjIndex.setEnable(false);
        bollIndex.setEnable(true);

        currentRect = bollIndex.getRect();
    }

    public boolean isShownVolume() {
        return volumeIndex.isEnable();
    }

    public boolean isShownMACD() {
        return macdIndex.isEnable();
    }

    public boolean isShownRSI() {
        return rsiIndex.isEnable();
    }

    public boolean isShownKDJ() {
        return kdjIndex.isEnable();
    }

    public boolean isShownBOLL() {
        return bollIndex.isEnable();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()){
            case 0:
                showVolume();
                break;
            case 1:
                showMACD();
                break;
            case 2:
                showRSI();
                break;
            case 3:
                showKDJ();
                break;
            case 4:
                showBOLL();
                break;
            default:
                showVolume();
                break;
        }
        if (kLineHandler != null) {
            kLineHandler.onCancelHighlight();
        }

        kLineView.notifyDataSetChanged();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
