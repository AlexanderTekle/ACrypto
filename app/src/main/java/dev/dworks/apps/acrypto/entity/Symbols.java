package dev.dworks.apps.acrypto.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 28/06/17.
 */

public class Symbols implements Serializable {
    public ArrayList<Symbol> symbols = new ArrayList<>();

    public static class Symbol {
        public String code = "";
        public String symbol = "";

        public Symbol(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
