package dev.dworks.apps.acrypto.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by HaKr on 21/07/17.
 */

public class News implements Serializable {
    public ArrayList<NewsData> news = new ArrayList<>();
    public int code;

    public static class NewsData {
        @SerializedName("source_source_link")
        @Expose
        public String link = "";
        public String title = "";
        public String thumb = "";
        public String publish_time = "";
    }

    public ArrayList<NewsData> getData() {
        return news;
    }
}
