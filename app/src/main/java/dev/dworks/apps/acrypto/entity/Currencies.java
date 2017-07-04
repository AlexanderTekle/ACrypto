package dev.dworks.apps.acrypto.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 28/06/17.
 */

public class Currencies implements Serializable {
    public ArrayList<Currency> currencies = new ArrayList<>();

    public static class Currency {
        public String code = "";
        public String name = "";

        public Currency(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
