package dev.dworks.apps.acrypto.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 18/06/17.
 */

public class CoinDetails extends BaseEntity {
    @SerializedName("Data")
    @Expose
    public Coin data;

    public Double getMarketCap(){
        Double mined = data.totalCoinsMined;
        Double price = Double.valueOf(data.aggregatedData.price);
        try {
            return mined * price;
        } catch (Exception e){
            return 0.0;
        }
    }

    public static class Coin implements Serializable {
        @SerializedName("code")
        @Expose
        public String code;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("Algorithm")
        @Expose
        public String algorithm;
        @SerializedName("ProofType")
        @Expose
        public String proofType;
        @SerializedName("BlockNumber")
        @Expose
        public Integer blockNumber;
        @SerializedName("NetHashesPerSecond")
        @Expose
        public Double netHashesPerSecond;
        @SerializedName("TotalCoinsMined")
        @Expose
        public Double totalCoinsMined;
        @SerializedName("BlockReward")
        @Expose
        public Double blockReward;
        @SerializedName("AggregatedData")
        @Expose
        public Coins.CoinDetail aggregatedData;
        @SerializedName("Exchanges")
        @Expose
        public ArrayList<Coins.CoinDetail> exchanges = null;

        public Coin(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
