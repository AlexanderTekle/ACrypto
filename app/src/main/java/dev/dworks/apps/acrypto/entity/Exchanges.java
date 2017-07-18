package dev.dworks.apps.acrypto.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by HaKr on 19/05/17.
 */

public class Exchanges extends BaseEntity {
    public static final String ALL_EXCHANGES = "All Exchanges";
    public static final String NO_EXCHANGES = "No Exchange";

    @SerializedName("Data")
    @Expose
    public ArrayList<Exchange> data = new ArrayList<>();

    public static class Exchange {
        public String exchange;
        public String fromSymbol;
        public String toSymbol;
        public double volume24h;
        public double volume24hTo;

        public Exchange(String name){
            exchange = name;
        }

        @Override
        public String toString() {
            return exchange;
        }
    }

    public ArrayList<Exchange> getAllData(){
        if(data.size() == 0){
            Exchange allExchange = new Exchange(NO_EXCHANGES);
            ArrayList<Exchange> finalList = new ArrayList<>();
            finalList.add(allExchange);
            return finalList;
        } else {
            Exchange allExchange = new Exchange(ALL_EXCHANGES);
            ArrayList<Exchange> finalList = new ArrayList<>();
            finalList.add(allExchange);
            finalList.addAll(data);
            return finalList;
        }
    }
}
