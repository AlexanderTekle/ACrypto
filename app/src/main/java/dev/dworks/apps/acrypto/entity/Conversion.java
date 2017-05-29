package dev.dworks.apps.acrypto.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HaKr on 29/05/17.
 */

public class Conversion {

    @SerializedName("query")
    @Expose
    public Query query;

    public class Query {

        @SerializedName("count")
        @Expose
        public Integer count;
        @SerializedName("created")
        @Expose
        public String created;
        @SerializedName("lang")
        @Expose
        public String lang;
        @SerializedName("results")
        @Expose
        public Results results;
    }

    public class Results {

        @SerializedName("rate")
        @Expose
        public Rate rate;
    }

    public class Rate {

        @SerializedName("id")
        @Expose
        public String id;
        @SerializedName("Name")
        @Expose
        public String name;
        @SerializedName("Rate")
        @Expose
        public String rate;
        @SerializedName("Date")
        @Expose
        public String date;
        @SerializedName("Time")
        @Expose
        public String time;
        @SerializedName("Ask")
        @Expose
        public String ask;
        @SerializedName("Bid")
        @Expose
        public String bid;
    }
}
