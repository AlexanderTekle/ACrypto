package dev.dworks.apps.acrypto.misc;


import android.support.v4.util.ArrayMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by HaKr on 14/05/17.
 */

public class UrlManager {
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    private final String mBaseUrl;
    private String mUrl;
    private ArrayMap<String, String> mParams = new ArrayMap<>();

    public UrlManager(String url) {
        this.mBaseUrl = url;
        this.mUrl = mBaseUrl;
    }

    public static UrlManager with(String url) {
        UrlManager urlHelper = new UrlManager(url);
        return urlHelper;
    }

    public UrlManager setDefaultParams(ArrayMap<String, String> params){
        this.mParams = params;
        return this;
    }

    public ArrayMap<String, String> getParams(){
        return mParams;
    }

    public UrlManager setParam(String key, String value){
        getParams().put(key, value);
        return this;
    }

    public UrlManager removeParam(String key){
        getParams().remove(key);
        return this;
    }

    public String getEncodedUrlParams(){

        StringBuilder encodedParams = new StringBuilder();
        String paramsEncoding = DEFAULT_PARAMS_ENCODING;
        Map<String, String> params = getParams();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if(null == entry.getValue()){
                    continue;
                }
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    /**
     * Returns the URL with params.
     */
    public String getUrl() {
        if(getParams() != null && getParams().size() != 0){
            String encodedParams = getEncodedUrlParams();
            String extra = "";
            if (encodedParams != null && encodedParams.length() > 0) {
                if (!mUrl.endsWith("?")) {
                    extra += "?";
                }
                extra += encodedParams;
            }
            return mUrl + extra;
        }
        return mUrl;
    }
}
