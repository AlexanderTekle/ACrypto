package dev.dworks.apps.acrypto.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by HaKr on 14/05/17.
 */

public class Prices extends BaseEntity {

    @SerializedName("Aggregated")
    @Expose
    public boolean aggregated;
    @Expose
    public long timeTo;
    @SerializedName("TimeFrom")
    @Expose
    public long timeFrom;
    @SerializedName("FirstValueInArray")
    @Expose
    public boolean firstValueInArray;
    @SerializedName("Data")
    @Expose
    public ArrayList<Price> price = new ArrayList<>();

    public static class Price {
        public long time;
        public double close;
        public double high;
        public double low;
        public double open;
        public double volumefrom;
        public double volumeto;
        public double conversion = 1;

        public double getClose() {
            return close * (1/conversion);
        }
    }
}
