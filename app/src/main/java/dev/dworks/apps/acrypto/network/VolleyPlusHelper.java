package dev.dworks.apps.acrypto.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

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

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;

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
        VolleyPlusHelper volleyPlusHelper = new VolleyPlusHelper(App.getInstance().getApplicationContext());
        return volleyPlusHelper;
    }

    public static VolleyPlusHelper with(Context context) {
        VolleyPlusHelper volleyPlusHelper = new VolleyPlusHelper(context);
        return volleyPlusHelper;
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

            mImageLoader = new SimpleImageLoader(mContext, cacheParams);
            mImageLoader.setDefaultDrawable(mPlaceHolder == 0 ? R.drawable.ic_coins : mPlaceHolder);
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
        VolleyLog.d("Adding request to queue: %s", req.getUrl());
        getRequestQueue().add(req);
    }

    public <T> void updateToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        VolleyLog.d("Adding request to queue: %s", req.getUrl());
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
}
