package dev.dworks.apps.acrypto.entity;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

import dev.dworks.apps.acrypto.App;

import static dev.dworks.apps.acrypto.portfolio.PortfolioFragment.DEFAULT_PRICE_TYPE;
import static dev.dworks.apps.acrypto.utils.Utils.getDisplayPercentageSimple;

/**
 * Created by HaKr on 18-Jul-17.
 */

public class PortfolioCoin implements Serializable {
    public String coin;
    public String currency;
    public String exchange;
    public String priceType;
    public double amount;
    public double price;
    public double conversion;
    public String notes;
    public long boughtAt;

    public PortfolioCoin(){

    }

    public PortfolioCoin(String coin, String currency, String exchange, String priceType,
                         double amount, double price, long broughtAt, String notes, double conversion){
        this.coin = coin;
        this.currency = currency;
        this.exchange = exchange;
        this.priceType = priceType;
        this.price = price;
        this.amount = amount;
        this.boughtAt = broughtAt;
        this.notes = notes;
        this.conversion = conversion;
    }

    @Exclude
    public String getKey(){
        return coin+"~"+currency;
    }

    @Exclude
    public double getUnitPrice(){
        return priceType.equals(DEFAULT_PRICE_TYPE) ? price : price / amount;
    }

    @Exclude
    public double getTotalAmount(){
        return priceType.equals(DEFAULT_PRICE_TYPE) ? amount * price : price;
    }

    @Exclude
    public double getTotalConvertedAmount(){
        return conversion * getTotalAmount();
    }

    @Exclude
    public double getTotalHoldings(){
        CoinPairs.CoinPair  coinPair = App.getInstance().getCachedCoinPair(getKey());
        if(null == coinPair){
            return  0;
        }
        double currentPrice = coinPair.getCurrentPrice();
        double currentTotal = amount * currentPrice;
        return  currentTotal;
    }

    @Exclude
    public double getTotalConvertedHoldings(){
        CoinPairs.CoinPair  coinPair = App.getInstance().getCachedCoinPair(getKey());
        if(null == coinPair){
            return  0;
        }
        double currentPrice = coinPair.getCurrentPrice();
        double currentTotal = amount * currentPrice * conversion;
        return  currentTotal;
    }

    @Exclude
    public double getTotalProfit(){
        return getTotalHoldings() - getTotalAmount();
    }

    @Exclude
    public String getProfitChange(){
        return getDisplayPercentageSimple(getTotalAmount(), getTotalHoldings());
    }
}
