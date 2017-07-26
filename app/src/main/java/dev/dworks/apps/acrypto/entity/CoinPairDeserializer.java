package dev.dworks.apps.acrypto.entity;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class CoinPairDeserializer implements JsonDeserializer<CoinPairs.CoinPair> {

    @Override
    public CoinPairs.CoinPair deserialize(JsonElement paramJsonElement, Type paramType,
                              JsonDeserializationContext paramJsonDeserializationContext) throws JsonParseException {

        CoinPairs.CoinPair coinPair = new Gson().fromJson(paramJsonElement.getAsJsonObject(),
                CoinPairs.CoinPair.class);

        try {
            coinPair.data = new ArrayList<>();
            for (String value: coinPair.raw) {
                Coins.CoinDetail coinDetail = new Coins.CoinDetail(value);
                coinPair.data.add(coinDetail);
            }
        } catch (IllegalArgumentException ie) {
        }

        return coinPair;
    }

}