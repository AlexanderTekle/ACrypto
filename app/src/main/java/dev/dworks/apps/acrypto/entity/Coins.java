package dev.dworks.apps.acrypto.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 16/05/17.
 */

public class Coins extends BaseEntity {
    public static final String BASE_URL = "https://files.coinmarketcap.com/static/img/coins/64x64/"; //bitcoin.png

    @SerializedName("Data")
    @Expose
    public ArrayList<String> data = null;
    public ArrayList<CoinDetail> list = null;
    public ArrayList<CoinDetails.Coin> coins = new ArrayList<>();

    public static class CoinDetail implements Serializable {

        @SerializedName("TYPE")
        @Expose
        public String type;
        @SerializedName("MARKET")
        @Expose
        public String market;
        @SerializedName("FROMSYMBOL")
        @Expose
        public String fromSym;
        @SerializedName("TOSYMBOL")
        @Expose
        public String toSym;
        @SerializedName("FLAGS")
        @Expose
        public String flags;
        @SerializedName("PRICE")
        @Expose
        public String price;
        @SerializedName("LASTUPDATE")
        @Expose
        public String lastUpdate;
        @SerializedName("LASTVOLUME")
        @Expose
        public String lastVolume;
        @SerializedName("LASTVOLUMETO")
        @Expose
        public String lastVolumeTo;
        @SerializedName("LASTTRADEID")
        @Expose
        public String lastTradeId;
        @SerializedName("VOLUME24HOUR")
        @Expose
        public String volume24H;
        @SerializedName("VOLUME24HOURTO")
        @Expose
        public String volume24HTo;
        @SerializedName("OPEN24HOUR")
        @Expose
        public String open24H;
        @SerializedName("HIGH24HOUR")
        @Expose
        public String high24H;
        @SerializedName("LOW24HOUR")
        @Expose
        public String low24H;
        @SerializedName("LASTMARKET")
        @Expose
        public String lastMarket;

        public CoinDetail() {

        }

        public CoinDetail(String data) {
            String[] dataSplit = data.split("~");
            type = dataSplit[0];
            market = dataSplit[1];
            fromSym = dataSplit[2];
            toSym = dataSplit[3];
            flags = dataSplit[4];
            price = dataSplit[5];
            lastUpdate = dataSplit[6];
            lastVolume = dataSplit[7];
            lastVolumeTo = dataSplit[8];
            lastTradeId = dataSplit[9];
            volume24H = dataSplit[10];
            volume24HTo = dataSplit[11];
            open24H = dataSplit[12];
            high24H = dataSplit[13];
            low24H = dataSplit[14];
            lastMarket = dataSplit[15];
        }

        public Double differnce(){
            double currentPrice = Double.parseDouble(price);
            double prevPrice = Double.parseDouble(open24H);
            Double difference = ((currentPrice - prevPrice)/prevPrice) * 100;
            return difference;
        }
    }

    public static CoinDetail getCoin(String data){
        return new CoinDetail(data);
    }
}
