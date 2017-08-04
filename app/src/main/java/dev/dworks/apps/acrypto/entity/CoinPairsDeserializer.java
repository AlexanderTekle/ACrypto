package dev.dworks.apps.acrypto.entity;

import android.support.v4.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by HaKr on 20-Jul-17.
 */

public class CoinPairsDeserializer implements JsonDeserializer<CoinPairs> {
    @Override
    public CoinPairs deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        CoinPairs coinPairs = new CoinPairs();
        ArrayMap<String, CoinPairs.CoinPair> data = new ArrayMap<>();
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonDeserializer<CoinPairs.CoinPair> deserializer = new CoinPairDeserializer();
        gsonBuilder.registerTypeAdapter(CoinPairs.CoinPair.class, deserializer);

        Gson customGson = gsonBuilder.create();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            try {
                String key = entry.getKey();
                CoinPairs.CoinPair coinPair = customGson.fromJson(entry.getValue(), CoinPairs.CoinPair.class);
                data.put(key, coinPair);
            }
            catch(NoSuchElementException e) {
                e.printStackTrace();
            }
        }

        coinPairs.data = data;
        return coinPairs;
    }
}
