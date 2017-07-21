//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package dev.dworks.apps.acrypto.network;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

public class StringRequest extends BaseRequest<String> {
    private final Listener<String> mListener;

    public StringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
    }

    public StringRequest(String url, Listener<String> listener, ErrorListener errorListener) {
        this(0, url, listener, errorListener);
    }

    protected void deliverResponse(String response) {
        if(null != this.mListener) {
            this.mListener.onResponse(response);
        }

    }

    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        Cache.Entry entry;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException var4) {
            parsed = new String(response.data);
        }

        if(shouldCache()){
            entry = VolleyPlusHelper.parseIgnoreCacheHeaders(response,
                    getCacheSoftMinutes() * 60 * 1000,
                    getCacheMinutes() * 60 * 1000);
        } else {
            entry = HttpHeaderParser.parseCacheHeaders(response);
        }

        return Response.success(parsed, entry);
    }
}
