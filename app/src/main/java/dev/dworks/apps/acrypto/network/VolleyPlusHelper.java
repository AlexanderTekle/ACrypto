package dev.dworks.apps.acrypto.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.TextUtils;
import android.widget.ImageView;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestTickle;
import com.android.volley.VolleyLog;
import com.android.volley.cache.DiskLruBasedCache.ImageCacheParams;
import com.android.volley.cache.SimpleImageLoader;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.VolleyTickle;

import java.util.ArrayList;
import java.util.Map;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.BuildConfig;
import dev.dworks.apps.acrypto.R;

import static com.android.volley.toolbox.HttpHeaderParser.parseDateAsEpoch;

/**
 * Created by HaKr on 13/05/17.
 */

public class VolleyPlusHelper {
    public static final String TAG = "VolleyPlusHelper";

    public static final int IMAGE_SIZE_BIG = 100;
    public static final int IMAGE_SIZE = 50;
    public static final int SCALE_DOWN_SIZE = 1024;
    public static final int SCALE_DOWN_SIZE_SMALL = 512;
    public static final String IMAGE_CACHE_DIR = "thumbs";
    public static final String IMAGE_BG_CACHE_DIR = "bgs";
    private static VolleyPlusHelper mVolleyPlusHelper;

    private final Context mContext;
    private RequestTickle mRequestTickle;
    private SimpleImageLoader mImageLoader;
    private RequestQueue mRequestQueue;
    private String mUrl;
    private int mPlaceHolder;
    private boolean mCrossfade;
    private String mCacheDir;


    public VolleyPlusHelper(Context context){
        mContext = context;
    }

    public static VolleyPlusHelper with() {
        if(null == mVolleyPlusHelper) {
            mVolleyPlusHelper = new VolleyPlusHelper(App.getInstance().getApplicationContext());
        }
        return mVolleyPlusHelper;
    }

    public static VolleyPlusHelper with(Context context) {
        if(null == mVolleyPlusHelper) {
            mVolleyPlusHelper = new VolleyPlusHelper(context);
        }
        return mVolleyPlusHelper;
    }

    public VolleyPlusHelper load(String string) {
        mUrl = string;
        return this;
    }

    public VolleyPlusHelper placeholder(int resource) {
        mPlaceHolder = resource;
        return this;
    }

    public VolleyPlusHelper cachedir(String name) {
        mCacheDir = name;
        return this;
    }

    public VolleyPlusHelper crossfade() {
        mCrossfade = true;
        return this;
    }

    public ImageLoader.ImageContainer into(ImageView view) {
        return getImageLoader().get(mUrl, view);
    }

    public ImageLoader.ImageContainer load(ImageView view, Bitmap bitmap) {
        return getImageLoader().set(mUrl, view, bitmap);
    }

    public SimpleImageLoader getImageLoader(){
        if(null == mImageLoader){
            ImageCacheParams cacheParams = new ImageCacheParams(mContext,
                    TextUtils.isEmpty(mCacheDir) ? IMAGE_CACHE_DIR :  mCacheDir);
            cacheParams.setMemCacheSizePercent(0.5f);

            ArrayList<Drawable> drawables = new ArrayList<>();
            drawables.add(AppCompatDrawableManager.get().getDrawable(mContext,
                    mPlaceHolder == 0 ? R.drawable.ic_coins : mPlaceHolder));

            mImageLoader = new SimpleImageLoader(mContext, cacheParams);
            mImageLoader.setDefaultDrawables(drawables);
            mImageLoader.setMaxImageSize(hasMoreHeap() ? SCALE_DOWN_SIZE_SMALL: IMAGE_SIZE_BIG);
            mImageLoader.setFadeInImage(mCrossfade);
            mImageLoader.setContetResolver(mContext.getContentResolver());
        }

        return mImageLoader;
    }

    public SimpleImageLoader getNewsImageLoader(){
        if(null == mImageLoader){
            ImageCacheParams cacheParams = new ImageCacheParams(mContext,
                    TextUtils.isEmpty(mCacheDir) ? IMAGE_BG_CACHE_DIR :  mCacheDir);
            cacheParams.setMemCacheSizePercent(0.5f);

            ArrayList<Drawable> drawables = new ArrayList<>();
            drawables.add(AppCompatDrawableManager.get().getDrawable(mContext,
                    mPlaceHolder == 0 ? R.drawable.ic_coins : mPlaceHolder));

            mImageLoader = new SimpleImageLoader(mContext, cacheParams);
            mImageLoader.setDefaultDrawables(drawables);
            mImageLoader.setMaxImageSize(hasMoreHeap() ? IMAGE_SIZE_BIG: IMAGE_SIZE);
            mImageLoader.setFadeInImage(mCrossfade);
            mImageLoader.setContetResolver(mContext.getContentResolver());
        }

        return mImageLoader;
    }

    public RequestTickle getRequestTickle() {
        if (mRequestTickle == null) {
            mRequestTickle = VolleyTickle.newRequestTickle(mContext);
        }

        return mRequestTickle;
    }

    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        if(BuildConfig.DEBUG) {
            VolleyLog.d("Adding request to queue: %s", req.getUrl());
        }
        getRequestQueue().add(req);
    }

    public <T> void updateToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        if(BuildConfig.DEBUG) {
            VolleyLog.d("Adding request to queue: %s", req.getUrl());
        }
        getRequestQueue().cancelAll(tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public NetworkResponse startTickle(Request request){
        getRequestTickle().add(request);
        return getRequestTickle().start();
    }
    public static boolean hasMoreHeap(){
        return Runtime.getRuntime().maxMemory() > 20971520;
    }

    public static String parseCharset(Map<String, String> headers, String defaultCharset) {
        String contentType = (String)headers.get("Content-Type");
        if(null == contentType) {
            contentType = (String) headers.get("content-type");
        }
        if(contentType != null) {
            String[] params = contentType.split(";");

            for(int i = 1; i < params.length; ++i) {
                String[] pair = params[i].trim().split("=");
                if(pair.length == 2 && pair[0].equals("charset")) {
                    return pair[1];
                }
            }
        }

        return defaultCharset;
    }

    public static String parseCharset(Map<String, String> headers) {
        return parseCharset(headers, "ISO-8859-1");
    }

    public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response, long soft_expire, long expire) {
        long now = System.currentTimeMillis();
        Map headers = response.headers;
        long serverDate = 0L;
        String serverEtag = null;
        String headerValue = (String)headers.get("Date");
        if(headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }

        serverEtag = (String)headers.get("ETag");
        long softExpire = soft_expire == 0 ? 0 : now + soft_expire;
        long ttl = expire == 0 ? 0 : now + expire;
        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;
        return entry;
    }
}
