package dev.dworks.apps.acrypto.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 16/05/17.
 */

public class Coins extends BaseEntity implements Serializable{
    public static final String BASE_URL = "https://files.coinmarketcap.com/static/img/coins/64x64/"; //bitcoin.png

    @SerializedName("Data")
    @Expose
    public ArrayList<String> data = null;

    public static class Coin implements Serializable {

        // 1. TYPE : 5
        // 2. MARKET : CCCAGG
        // 3. FROMSYMBOL : BTC
        // 4. TOSYMBOL : USD
        // 5. FLAG :  4
        // 6. PRICE :  2159.75
        // 7. LASTUPDATE : 1495952799
        // 8. AVG : 0.249
        // 9. LASTVOLUME : 544.40613
        // 10. Last Trade ID :  15960455
        // 12. VOLUME24HOUR : 186244.47348771704
        // 13. VOLUME24HOURTO : 372064007.6147878
        // 14. OPENHOUR : 2073.1
        // 15. HIGHHOUR : 2231.57
        // 16. LOWHOUR : 1851.93
        // 17. LASTMARKET : Coinbase
        // 18. MaskInt :  78ce9

        public final String type;
        public final String market;
        public final String fromSym;
        public final String toSym;
        public final String flag;
        public final String price;
        public final String lastUpdate;
        public final String avg;
        public final String lastVolume;
        public final String lastTradeId;
        public final String volume24;
        public final String volume24To;
        public final String openHour;
        public final String highHour;
        public final String lowHour;
        public final String lastMarket;

        public Coin(String data){
           String[] dataSplit =  data.split("~");
           type = dataSplit[0];
           market = dataSplit[1];
           fromSym = dataSplit[2];
           toSym = dataSplit[3];
           flag = dataSplit[4];
           price = dataSplit[5];
           lastUpdate = dataSplit[6];
           avg = dataSplit[7];
           lastVolume = dataSplit[8];
           lastTradeId = dataSplit[9];
           volume24 = dataSplit[10];
           volume24To = dataSplit[11];
           openHour = dataSplit[12];
           highHour = dataSplit[13];
           lowHour = dataSplit[14];
           lastMarket = dataSplit[15];

        }
    }

    public static Coin getCoin(String data){
        return new Coin(data);
    }
}
