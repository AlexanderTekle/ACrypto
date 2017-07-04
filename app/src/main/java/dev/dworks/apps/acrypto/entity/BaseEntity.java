package dev.dworks.apps.acrypto.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by HaKr on 16/05/17.
 */

public class BaseEntity implements Serializable{
    @SerializedName("Response")
    @Expose
    public String response;
    @SerializedName("Message")
    @Expose
    public String message;
    @SerializedName("Type")
    @Expose
    public int type;

    public boolean isValidResponse(){
        return response.equalsIgnoreCase("success");
    }
}
