package dev.dworks.apps.acrypto.misc;

import android.support.v4.util.LruCache;

import dev.dworks.apps.acrypto.entity.CoinPairs;

public class LruCoinPairCache {
    private static final int DEFAULT_SIZE = 20;
    private LruCache<String, CoinPairs.CoinPair> mLruCache;

    public LruCoinPairCache() {
        this.mLruCache = new LruCache<>(DEFAULT_SIZE);
    }

    public CoinPairs.CoinPair getCoinDetail(String key) {
        return (CoinPairs.CoinPair) this.mLruCache.get(key);
    }

    public void putCoinDetail(String key, CoinPairs.CoinPair coinDetail) {
        this.mLruCache.put(key, coinDetail);
    }

    public void removeCoinDetail(String key) {
        this.mLruCache.remove(key);
    }

    public void clear() {
        this.mLruCache.evictAll();
    }
}