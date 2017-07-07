package dev.dworks.apps.acrypto.entity;

/**
 * Created by HaKr on 06-Jul-17.
 */

public class PriceAlert {
    public String name;
    public String fromSymbol;
    public String toSymbol;
    public int status;
    public String condition;
    public String type;
    public double value;
    public String nameStatusIndex;

    public PriceAlert(){

    }

    public PriceAlert(String name, String fromSymbol, String toSymbol, int status,
                      String condition, String type, double value){
        this.name = name;
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.status = status;
        this.condition = condition;
        this.type= type;
        this.value = value;
    }

    public String getNameStatusIndex() {
        return name + status;
    }
}
