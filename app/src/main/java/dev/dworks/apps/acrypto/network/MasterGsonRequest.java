package dev.dworks.apps.acrypto.network;

import android.support.v4.util.ArrayMap;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static dev.dworks.apps.acrypto.App.APP_VERSION;
import static dev.dworks.apps.acrypto.utils.Utils.AUTH_HEADER;
import static dev.dworks.apps.acrypto.utils.Utils.CLIENT_HEADER;

/**
 * Volley adapter for JSON requests that will be parsed into Java objects by Gson.
 */
public class MasterGsonRequest<T> extends BaseRequest<T> {

    private final Gson gson = new Gson();
    private final Class<T> clazz;
    private Map<String, String> headers;
    private final Map<String, String> params;
    private final Listener<T> listener;

    public MasterGsonRequest(String url, Class<T> clazz,
                             Listener<T> listener, ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.clazz = clazz;
        this.params = null;
        this.listener = listener;
        init();
    }

    public MasterGsonRequest(int type, String url, Class<T> clazz,
                             Map<String, String> params,
                             Listener<T> listener, ErrorListener errorListener) {
        super(type, url, errorListener);
        this.clazz = clazz;
        this.params = params;
        this.listener = listener;
        init();
    }

    private void init() {
        if (headers == null) {
            headers = new ArrayMap<>();
        }
        headers.put(AUTH_HEADER, "7c8f8a609a0e98b192bffff15d35bfa6");
        headers.put(CLIENT_HEADER, APP_VERSION);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params != null ? params : super.getParams();
    }

    @Override
    protected void deliverResponse(T response) {
        if (null != listener) {
            listener.onResponse(response);
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data, VolleyPlusHelper.parseCharset(response.headers));
            Cache.Entry entry;
            if(shouldCache()){
                entry = VolleyPlusHelper.parseIgnoreCacheHeaders(response,
                        getCacheSoftMinutes() * 60 * 1000,
                        getCacheMinutes() * 60 * 1000);
            } else {
                entry = HttpHeaderParser.parseCacheHeaders(response);
            }
            return Response.success(gson.fromJson(json, clazz), entry);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

}