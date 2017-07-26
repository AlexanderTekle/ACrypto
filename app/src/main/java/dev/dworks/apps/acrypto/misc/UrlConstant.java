package dev.dworks.apps.acrypto.misc;

import android.support.v4.util.ArrayMap;

/**
 * Created by HaKr on 15/05/17.
 */

public class UrlConstant {

    public static final String ACRYPTO_BASE_API_URL = "https://us-central1-acrypto-ef41d.cloudfunctions.net/api";
    public static final String COINS_API = ACRYPTO_BASE_API_URL + "/coins";
    public static final String CURRENCY_API = ACRYPTO_BASE_API_URL + "/currencies";
    public static final String COINS_LIST_API = ACRYPTO_BASE_API_URL + "/coins_list";
    public static final String COINS_IGNORE_API = ACRYPTO_BASE_API_URL + "/coins_ignore";
    public static final String SYMBOLS_API = ACRYPTO_BASE_API_URL + "/symbols";
    public static final String AMAZON_TOKEN_API = ACRYPTO_BASE_API_URL + "/amazontoken";
    public static final String NEWS_URL = ACRYPTO_BASE_API_URL + "/news";

    public static final String BASE_API_URL = "https://min-api.cryptocompare.com";
    public static final String BASE_URL = "https://www.cryptocompare.com";
    public static final String HISTORY_MINUTE_URL = BASE_API_URL + "/data/histominute";
    public static final String HISTORY_HOUR_URL = BASE_API_URL + "/data/histohour";
    public static final String HISTORY_DAY_URL = BASE_API_URL + "/data/histoday";
    public static final String HISTORY_PRICE_URL = BASE_API_URL + "/data/price";
    public static final String HISTORY_PRICE_HISTORICAL_URL = BASE_API_URL + "/data/pricehistorical";
    public static final String COINLIST_URL =  BASE_URL + "/api/data/toplistvolumesnapshot/";
    //public static final String COINLIST_URL =  "https://api.coinmarketcap.com/v1/ticker/";
    public static final String COINDETAILS_URL =  BASE_URL + "/api/data/coinsnapshot/";
    public static final String EXCHANGELIST_URL = BASE_API_URL + "/data/top/exchanges";
    public static final String SUBSPAIRS_URL = BASE_API_URL + "/data/subsPairs";
    public static final String CONVERSION_URL = "https://query.yahooapis.com/v1/public/yql?q=%s&format=json&env=store://datatables.org/alltableswithkeys";
    //public static final String NEWS_URL = "http://bitcoinstat.org/api_v3/news";

    public static String getArbitrageToUrl() {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("type", "arbitrage_to");
        return UrlManager.with(UrlConstant.CURRENCY_API)
                .setDefaultParams(params).getUrl();
    }

    public static String getArbitrageFromUrl() {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("type", "arbitrage_from");
        return UrlManager.with(UrlConstant.CURRENCY_API)
                .setDefaultParams(params).getUrl();
    }

    public static String getArbitrageCoinsUrl() {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("type", "arbitrage");

        return UrlManager.with(UrlConstant.COINS_API)
                .setDefaultParams(params).getUrl();
    }
}
