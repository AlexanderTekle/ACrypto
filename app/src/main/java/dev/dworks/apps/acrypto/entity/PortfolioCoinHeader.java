package dev.dworks.apps.acrypto.entity;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewStructure;
import android.widget.TextView;

import com.android.volley.Cache;

import java.util.Map;

import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.TimeUtils;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.utils.Utils.getDisplayPercentageSimple;
import static dev.dworks.apps.acrypto.utils.Utils.getPercentDifferenceColor;

/**
 * Created by HaKr on 09-Aug-17.
 */

public class PortfolioCoinHeader {
    public String mCurrency;
    private ArrayMap<String, PortfolioCoin> mCoins;
    private double totalCost;
    private double totalHoldings;
    private double totalHoldings24H;
    private double totalRealizedProfit;

    public PortfolioCoinHeader(String currency, ArrayMap<String, PortfolioCoin> coins){
        mCurrency = currency;
        mCoins = coins;
    }

    public void calculate(){
        totalCost = 0;
        totalHoldings = 0;
        totalHoldings24H = 0;
        totalRealizedProfit = 0;
        if (null != mCoins && !mCoins.isEmpty()) {
            for (Map.Entry<String, PortfolioCoin> entry : mCoins.entrySet()) {
                PortfolioCoin coin = entry.getValue();
                if (coin.isSellType()) {
                    totalRealizedProfit += coin.getTotalProfit();
                    continue;
                }
                totalCost += coin.getTotalConvertedAmount();
                totalHoldings += coin.getTotalConvertedHoldings();
                totalHoldings24H += coin.getTotalConvertedHoldings24H();
            }
        }
    }

    public String getCurrencySymbol() {
        return Utils.getCurrencySymbol(mCurrency);
    }

    public double getTotalCost() {
        return totalCost;
    }

    public double getTotalHoldings() {
        return totalHoldings;
    }

    public boolean isPortfolioEmpty(){
        return totalCost == 0 && totalHoldings == 0;
    }

    public double getTotalRealizedProfit(){
        return totalRealizedProfit;
    }

    public double getTotalProfit(){
        return totalHoldings - totalCost;
    }

    public String getTotalProfitPercentage(){
        return isPortfolioEmpty() ? "-" : getDisplayPercentageSimple(totalCost, totalHoldings);
    }

    public int getProfitPercentageColor(Context context) {
        return ContextCompat.getColor(context, getPercentDifferenceColor(getTotalProfit()));
    }

    public double getTotalProfit24H(){
        return totalHoldings - totalHoldings24H;
    }

    public String getTotalProfitPercentage24H(){
        return isPortfolioEmpty() ? "-" : getDisplayPercentageSimple(totalHoldings24H, totalHoldings);
    }

    public int getProfitPercentage24HColor(Context context) {
        return ContextCompat.getColor(context, getPercentDifferenceColor(getTotalProfit24H()));
    }

    public static void showLastUpdated(Context context, TextView lastUpdated, String cachedUrl){
        Cache cache = VolleyPlusHelper.with(context).getRequestQueue().getCache();
        Cache.Entry entry = cache.get(cachedUrl);
        if (null != entry) {
            long lastUpdatedTime = entry.serverDate;
            lastUpdated.setVisibility(0 == lastUpdatedTime ? View.INVISIBLE : View.VISIBLE);
            lastUpdated.setText(TimeUtils.getTimeAgo(lastUpdatedTime));
        } else {
            lastUpdated.setVisibility(View.INVISIBLE);
            lastUpdated.setText("");
        }
    }

    public String getCurrency() {
        return mCurrency;
    }
}
