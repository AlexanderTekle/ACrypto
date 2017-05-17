package dev.dworks.apps.acrypto.network;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.error.VolleyError;

import dev.dworks.apps.acrypto.utils.LogUtils;

abstract class BaseRequest<T> extends Request<T> {

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
}