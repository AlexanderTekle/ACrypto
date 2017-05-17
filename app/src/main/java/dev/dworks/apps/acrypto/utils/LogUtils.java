/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.dworks.apps.acrypto.utils;

import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.android.volley.toolbox.VolleyTickle;

import java.net.URLEncoder;
import java.util.Map;

import dev.dworks.apps.acrypto.BuildConfig;
import dev.dworks.apps.acrypto.misc.Config;
import dev.dworks.apps.acrypto.misc.CrashReportingManager;

public class LogUtils {
	private static final String LOG_PREFIX = "shifoodroid_";
	private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
	private static final int MAX_LOG_TAG_LENGTH = 23;

	public static String makeLogTag(String str) {
		if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
			return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
		}

		return LOG_PREFIX + str;
	}

	/**
	 * Don't use this when obfuscating class names!
	 */
	public static String makeLogTag(Class cls) {
		return makeLogTag(cls.getSimpleName());
	}

	public static void LOGD(final String tag, String message) {
		// noinspection PointlessBooleanExpression,ConstantConditions
		if (BuildConfig.DEBUG || Config.IS_BETA_BUILD || Log.isLoggable(tag, Log.DEBUG)) {
			Log.d(tag, message);
		}
	}

	public static void LOGD(final String tag, String message, Throwable cause) {
		// noinspection PointlessBooleanExpression,ConstantConditions
		if (BuildConfig.DEBUG || Config.IS_BETA_BUILD || Log.isLoggable(tag, Log.DEBUG)) {
			Log.d(tag, message, cause);
		}
	}

	public static void LOGV(final String tag, String message) {
		// noinspection PointlessBooleanExpression,ConstantConditions
		if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
			Log.v(tag, message);
		}
	}

	public static void LOGV(final String tag, String message, Throwable cause) {
		// noinspection PointlessBooleanExpression,ConstantConditions
		if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
			Log.v(tag, message, cause);
		}
	}

	public static void LOGI(final String tag, String message) {
		Log.i(tag, message);
	}

	public static void LOGI(final String tag, String message, Throwable cause) {
		Log.i(tag, message, cause);
	}

	public static void LOGW(final String tag, String message) {
		Log.w(tag, message);
	}

	public static void LOGW(final String tag, String message, Throwable cause) {
		Log.w(tag, message, cause);
	}

	public static void LOGE(final String tag, String message) {
		Log.e(tag, message);
	}

	public static void LOGE(final String tag, String message, Throwable cause) {
		Log.e(tag, message, cause);
	}

	private LogUtils() {
	}

	public static void sendFailureLog(VolleyError error, String url, int method, Map<String, String> params) {
		try {
			if (null != error && null != error.networkResponse) {
				ArrayMap<String, String> map = new ArrayMap<>();
				map.put("Request_url", url);
				if (null != params) {
					map.put("Request_params", getEncodedUrlParams(params));
					if (null != error.networkResponse.data) {
						map.put("Response", VolleyTickle.parseResponse(error.networkResponse));
					}
				}

				String message = getMethodName(method);
				Uri uri = Uri.parse(url);
				if (null != uri && uri.getPathSegments().size() >= 3) {
					message += " " + uri.getPathSegments().get(2);
				}

				message += " : " + error.networkResponse.statusCode;

/*				Sentry.SentryEventBuilder sentryEventBuilder = new Sentry.SentryEventBuilder();
				sentryEventBuilder.setMessage(message);
				sentryEventBuilder.setExtra(map);
				sentryEventBuilder.setTags(Sentry.getSystemTags());
				sentryEventBuilder.setException(error);
				Sentry.captureEvent(sentryEventBuilder);*/
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getEncodedUrlParams(Map<String, String> params) {

		StringBuilder encodedParams = new StringBuilder();
		String paramsEncoding = "UTF-8";
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				if (null == entry.getValue()) {
					continue;
				}
				encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
				encodedParams.append('=');
				encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
				encodedParams.append('&');
			}
			return encodedParams.toString();
		} catch (Exception uee) {
			return "Encoding not supported: " + uee.getMessage();
		}
	}

	public static String getMethodName(int type) {
		String name = "";
		switch (type) {
			case Request.Method.DEPRECATED_GET_OR_POST:
				name = "";
				break;
			case Request.Method.GET:
				name = "GET";
				break;
			case Request.Method.DELETE:
				name = "DELETE";
				break;
			case Request.Method.POST:
				name = "POST";
				break;
			case Request.Method.PATCH:
				name = "PATCH";
				break;
			case Request.Method.PUT:
				name = "PUT";
				break;
		}
		return name;
	}


	public static void logException(Exception e) {
		if(!BuildConfig.DEBUG) {
			CrashReportingManager.logException(e);
		}
	}
}