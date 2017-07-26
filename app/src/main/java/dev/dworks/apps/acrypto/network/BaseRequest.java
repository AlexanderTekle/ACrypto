package dev.dworks.apps.acrypto.network;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.error.VolleyError;

import dev.dworks.apps.acrypto.utils.LogUtils;
import dev.dworks.apps.acrypto.utils.Utils;

abstract class BaseRequest<T> extends Request<T> {

	private long cacheSoftMinutes = 5;
	private long cacheMinutes = 60;

	BaseRequest(int method, String url, ErrorListener listener) {
		super(method, url, listener);
		setShouldCache(false);
		setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
	}

	@Override
	public void deliverError(VolleyError error) {
		super.deliverError(error);
        try {
            LogUtils.sendFailureLog(error, getUrl(), getMethod(), getParams());
        } catch (Exception e) {
			LogUtils.sendFailureLog(error, getUrl(), getMethod(), null);
        }
    }

	public BaseRequest<T> setDontExpireCache() {
		this.cacheSoftMinutes = 0;
		this.cacheMinutes = 0;
		return this;
	}

	public BaseRequest<T> setMasterExpireCache() {
		this.cacheSoftMinutes = Utils.getMasterDataCacheTime();
		this.cacheMinutes = Utils.getMasterDataCacheTime();
		return this;
	}

	public BaseRequest<T> setCacheMinutes(long cacheSoftMinutes, long cacheMinutes) {
		this.cacheSoftMinutes = cacheSoftMinutes;
		this.cacheMinutes = cacheMinutes;
		return this;
	}

	public long getCacheMinutes() {
		return cacheMinutes;
	}

	public long getCacheSoftMinutes() {
		return cacheSoftMinutes;
	}
}