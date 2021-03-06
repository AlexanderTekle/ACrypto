package dev.dworks.apps.acrypto.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 28/06/17.
 */

public class CoinsList implements Serializable {
    public ArrayList<Currency> coins_list = new ArrayList<>();

    public static class Currency implements Serializable {
        public String code = "";

        public Currency(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
