package dev.dworks.apps.acrypto.entity;

import android.support.v4.util.ArrayMap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by HaKr on 20-Jul-17.
 */

public class CoinPairs extends  BaseEntity {

    public ArrayMap<String, CoinPair> data = new ArrayMap<>();

    public class CoinPair {
        @SerializedName("Conversion")
        @Expose
        private String conversion;
        @SerializedName("ConversionSymbol")
        @Expose
        private String conversionSymbol;
        @SerializedName("CurrencyFrom")
        @Expose
        private String currencyFrom;
        @SerializedName("CurrencyTo")
        @Expose
        private String currencyTo;
        @SerializedName("Market")
        @Expose
        private String market;
        @SerializedName("SubBase")
        @Expose
        private String subBase;
        @SerializedName("RAW")
        @Expose
        public ArrayList<String> raw = null;

        public ArrayList<Coins.CoinDetail> data = new ArrayList<>();

        public double getCurrentPrice(){
            if(conversion.equals("multiply")){
                return Double.parseDouble(data.get(0).price) * Double.parseDouble(data.get(1).price);
            } else {
                return Double.parseDouble(data.get(0).price);
            }
        }

        public double get24HPrice(){
            if(conversion.equals("multiply")){
                return Double.parseDouble(data.get(0).open24H) * Double.parseDouble(data.get(1).open24H);
            } else {
                return Double.parseDouble(data.get(0).open24H);
            }
        }

    }
}
