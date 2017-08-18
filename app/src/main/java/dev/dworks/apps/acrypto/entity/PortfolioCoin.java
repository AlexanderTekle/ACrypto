package dev.dworks.apps.acrypto.entity;

import android.text.TextUtils;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

import dev.dworks.apps.acrypto.App;

import static dev.dworks.apps.acrypto.portfolio.PortfolioCoinDetailFragment.COIN_TYPE_SELL;
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
    public double priceSold;
    public double conversion;
    public String notes;
    public long boughtAt;
    public String type;

    public PortfolioCoin(){

    }

    public PortfolioCoin(String coin, String currency, String exchange, String priceType,
                         double amount, double price, double priceSold,
                         long broughtAt, String notes, String type, double conversion){
        this.coin = coin;
        this.currency = currency;
        this.exchange = exchange;
        this.priceType = priceType;
        this.price = price;
        this.priceSold = priceSold;
        this.amount = amount;
        this.boughtAt = broughtAt;
        this.notes = notes;
        this.type = type;
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
    public double getPrice(){
        return isSellType() ? priceSold : price;
    }

    @Exclude
    public double getTotalAmount(){
        return priceType.equals(DEFAULT_PRICE_TYPE) ? amount * price : price;
    }

    @Exclude
    public double getTotalAmountSold(){
        return priceType.equals(DEFAULT_PRICE_TYPE) ? amount * priceSold : priceSold;
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
    public double getTotalConvertedHoldings24H(){
        CoinPairs.CoinPair  coinPair = App.getInstance().getCachedCoinPair(getKey());
        if(null == coinPair){
            return  0;
        }
        double currentPrice = coinPair.get24HPrice();
        double currentTotal = amount * currentPrice * conversion;
        return  currentTotal;
    }

    @Exclude
    public double getTotalProfit(){
        return isSellType() ? getTotalAmountSold() : getTotalHoldings() - getTotalAmount();
    }

    @Exclude
    public String getProfitChange(){
        return getDisplayPercentageSimple(getTotalAmount(), isSellType() ? getTotalAmountSold() : getTotalHoldings());

    }

    @Exclude
    public boolean isSellType(){
        return !TextUtils.isEmpty(type) && type.equals(COIN_TYPE_SELL);
    }

    @Exclude
    public void sell(double amount){
        this.amount -= amount;
        if(!priceType.equals(DEFAULT_PRICE_TYPE)){
            price = getUnitPrice() * amount;
        }
    }

    public String getCoinName(){
        return coin + (isSellType() ? "*" : "");
    }
}
