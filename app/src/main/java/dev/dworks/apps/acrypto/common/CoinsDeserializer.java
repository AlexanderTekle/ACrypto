package dev.dworks.apps.acrypto.common;

import android.support.v4.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;

import dev.dworks.apps.acrypto.entity.CoinDetails;
import dev.dworks.apps.acrypto.entity.CoinPairDeserializer;
import dev.dworks.apps.acrypto.entity.CoinPairs;
import dev.dworks.apps.acrypto.entity.Coins;

/**
 * Created by HaKr on 9-Aug-17.
 */

public class CoinsDeserializer implements JsonDeserializer<Coins> {
    @Override
    public Coins deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {


        Coins coins = new Gson().fromJson(jsonElement.getAsJsonObject(), Coins.class);
        ArrayList<Coins.CoinDetail> data = new ArrayList<>();

        for ( String value:
             coins.data) {
            Coins.CoinDetail coin = Coins.getCoin(value);
            data.add(coin);
        }

        coins.list = data;
        return coins;
    }
}
