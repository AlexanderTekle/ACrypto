package dev.dworks.apps.acrypto.entity;

import android.support.v4.util.ArrayMap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 28/05/17.
 */

public class CoinDetails {
    public ArrayMap<String, CoinDetail> coins =  new ArrayMap<>();

    public ArrayList<CoinDetail> data;

    public static class CoinDetail implements Serializable {

        @SerializedName("id")
        @Expose
        public String id;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("symbol")
        @Expose
        public String symbol;
        @SerializedName("rank")
        @Expose
        public String rank;
        @SerializedName("price_usd")
        @Expose
        public String priceUsd;
        @SerializedName("price_btc")
        @Expose
        public String priceBtc;
        @SerializedName("24h_volume_usd")
        @Expose
        public String volumeUsd_24h;
        @SerializedName("market_cap_usd")
        @Expose
        public String marketCapUsd;
        @SerializedName("available_supply")
        @Expose
        public String availableSupply;
        @SerializedName("total_supply")
        @Expose
        public String totalSupply;
        @SerializedName("percent_change_1h")
        @Expose
        public String percentChange1h;
        @SerializedName("percent_change_24h")
        @Expose
        public String percentChange24h;
        @SerializedName("percent_change_7d")
        @Expose
        public String percentChange7d;
        @SerializedName("last_updated")
        @Expose
        public String lastUpdated;

        public CoinDetail(String id, String name){
            this.id = id;
            this.name = name;
        }
    }
}
