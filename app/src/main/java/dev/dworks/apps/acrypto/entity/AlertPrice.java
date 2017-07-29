package dev.dworks.apps.acrypto.entity;

import java.io.Serializable;

/**
 * Created by HaKr on 06-Jul-17.
 */

public class AlertPrice implements Serializable{
    public String name;
    public String fromSymbol;
    public String toSymbol;
    public int status;
    public String condition;
    public String frequency;
    public String type;
    public double value;
    public String nameStatusIndex;
    public String notes;

    public AlertPrice(){

    }

    public AlertPrice(String name, String fromSymbol, String toSymbol, int status,
                      String condition, String frequency, String type, double value){
        this.name = name;
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.status = status;
        this.condition = condition;
        this.frequency = frequency;
        this.type = type;
        this.value = value;
        this.nameStatusIndex = getNameStatusIndex();
    }

    public String getNameStatusIndex() {
        return name + status;
    }
}
