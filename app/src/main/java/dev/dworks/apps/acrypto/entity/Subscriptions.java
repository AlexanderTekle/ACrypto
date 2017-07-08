package dev.dworks.apps.acrypto.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 28/06/17.
 */

public class Subscriptions implements Serializable {
    public ArrayList<Subscription> subscriptions = new ArrayList<>();

    public static class Subscription {
        public String title = "";
        public String description = "";
        public int type = 1;
    }
}
