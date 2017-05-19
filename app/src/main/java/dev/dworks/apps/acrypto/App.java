package dev.dworks.apps.acrypto;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v7.app.AppCompatDelegate;

import com.google.gson.Gson;

import java.util.Locale;

import dev.dworks.apps.acrypto.entity.Symbols;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.utils.Utils;

/**
 * Created by HaKr on 16/05/17.
 */

public class App extends Application {
	public static final String TAG = "ACrypto";

	static {
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}

	public static String APP_VERSION;
    public static int APP_VERSION_CODE;
	private static App sInstance;
	private Locale current;
	private Symbols symbols;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;

/*		CustomActivityOnCrash.setLaunchErrorActivityWhenInBackground(false);
		CustomActivityOnCrash.setRestartActivityClass(MainActivity.class);
		CustomActivityOnCrash.setShowErrorDetails(false);
		CustomActivityOnCrash.setErrorActivityClass(ErrorActivity.class);
		CustomActivityOnCrash.install(this);*/

		if(!BuildConfig.DEBUG) {
			AnalyticsManager.intialize(getApplicationContext());
		}

    	try {
            final PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
    		APP_VERSION = info.versionName;
    		APP_VERSION_CODE = info.versionCode;
		} catch (NameNotFoundException e) {
			APP_VERSION = "Unknown";
			APP_VERSION_CODE = 0;
			e.printStackTrace();
		}
		loadCoinSymbols();
	}

	private void loadCoinSymbols() {
		String symbolsString = Utils.getStringAsset(this, "symbols.json");
		Gson gson = new Gson();
		symbols = gson.fromJson(symbolsString, Symbols.class);
	}

	public Symbols getSymbols(){
		return symbols;
	}

	public Locale getLocale() {
		if(current == null){
			current = Locale.US;
		}
		return current;
	}
	
	public static synchronized App getInstance() {
		return sInstance;
	}
	
	@Override
	public void onLowMemory() {
		Runtime.getRuntime().gc(); 
		super.onLowMemory();
	}
}