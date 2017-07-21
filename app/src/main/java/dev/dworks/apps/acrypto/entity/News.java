package dev.dworks.apps.acrypto.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by HaKr on 21/07/17.
 */

public class News implements Serializable {
    public ArrayList<NewsData> data = new ArrayList<>();
    public int code;

    public static class NewsData {
        public String link = "";
        public String title = "";
        public String description = "";
        public long publicated = 0;

    }

    public ArrayList<NewsData> getData() {

        Comparator<NewsData> comparator = new Comparator<News.NewsData>() {
            @Override
            public int compare(News.NewsData n1, News.NewsData n2) {
                if (n1.publicated < n2.publicated) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };

        Collections.sort(data, comparator);
        return data;
    }
}
