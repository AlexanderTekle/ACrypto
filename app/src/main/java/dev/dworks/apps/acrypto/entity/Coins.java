package dev.dworks.apps.acrypto.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by HaKr on 16/05/17.
 */

public class Coins extends BaseEntity implements Serializable{
    @SerializedName("BaseImageUrl")
    @Expose
    public String baseImageUrl;
    @SerializedName("BaseLinkUrl")
    @Expose
    public String baseLinkUrl;

    @SerializedName("Data")
    @Expose
    public HashMap<String, Coin> data;

    public static class Coin implements Serializable {
        @SerializedName("Id")
        @Expose
        public String id;
        @SerializedName("Url")
        @Expose
        public String url;
        @SerializedName("ImageUrl")
        @Expose
        public String imageUrl;
        @SerializedName("Name")
        @Expose
        public String name;
        @SerializedName("CoinName")
        @Expose
        public String coinName;
        @SerializedName("FullName")
        @Expose
        public String fullName;
        @SerializedName("Algorithm")
        @Expose
        public String algorithm;
        @SerializedName("ProofType")
        @Expose
        public String proofType;
        @SerializedName("SortOrder")
        @Expose
        public String sortOrder;
    }
}
