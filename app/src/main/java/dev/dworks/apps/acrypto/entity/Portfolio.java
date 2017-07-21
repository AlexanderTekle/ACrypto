package dev.dworks.apps.acrypto.entity;

import java.io.Serializable;

/**
 * Created by HaKr on 18-Jul-17.
 */

public class Portfolio implements Serializable {
    public String id;
    public String name;
    public String currency;
    public String description;
    public boolean isPrivate;
    public double cost;

    public Portfolio(){

    }

    public Portfolio(String name, String currency, String description){
        this.name = name;
        this.currency = currency;
        this.description = description;
        this.isPrivate = true;
    }
}
