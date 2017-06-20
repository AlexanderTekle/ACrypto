package dev.dworks.apps.acrypto;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v7.app.AppCompatDelegate;

import com.google.firebase.perf.FirebasePerformance;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.Symbols;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.misc.AnalyticsManager.setProperty;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_TO_DEFAULT;

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
	private CoinDetailSample coinDetails;
	private ArrayList<String> currencies;
	private ArrayList<String> currenciesOne;
	private ArrayList<String> currenciesTWo;
	private String defaultCurrencyCode;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;

		CaocConfig.Builder.create()
				.backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
				.showErrorDetails(false)
				.showRestartButton(true)
				.trackActivities(true)
				.errorDrawable(R.drawable.ic_bug)
				.errorActivity(ErrorActivity.class)
				.apply();

		if(!BuildConfig.DEBUG) {
			AnalyticsManager.intialize(getApplicationContext());
			setProperty("NativeCurrency", getLocaleCurrency());
			FirebasePerformance.getInstance().setPerformanceCollectionEnabled(true);
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
		loadCoinDetails();
	}

	private void loadCoinSymbols() {
		String symbolsString = Utils.getStringAsset(this, "symbols.json");
		Gson gson = new Gson();
		symbols = gson.fromJson(symbolsString, Symbols.class);
	}

	private void loadCoinDetails() {
		String symbolsString = Utils.getStringAsset(this, "coins.json");
		Gson gson = new Gson();
		coinDetails = gson.fromJson(symbolsString, CoinDetailSample.class);
	}

	public Symbols getSymbols(){
		return symbols;
	}

	public CoinDetailSample getCoinDetails(){
		return coinDetails;
	}

	public Locale getLocale() {
		if(current == null){
			current = getResources().getConfiguration().locale;
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

	public ArrayList<String> getCurrencyToList() {
		if(currencies == null){
			List<String> currencyNames = Arrays.asList(getResources().getStringArray(R.array.currency_names));
			currencies = new ArrayList<>(currencyNames);
		}

		return currencies;
	}

	public ArrayList<String> getCurrencyOneList() {
		if(currenciesOne == null){
			List<String> currencyNames = Arrays.asList(getResources().getStringArray(R.array.currency_one));
			currenciesOne = new ArrayList<>(currencyNames);
		}

		return currenciesOne;
	}

	public ArrayList<String> getCurrencyTwoList() {
		if(currenciesTWo == null){
			List<String> currencyNames = Arrays.asList(getResources().getStringArray(R.array.currency_two));
			currenciesTWo = new ArrayList<>(currencyNames);
		}

		return currenciesTWo;
	}

	public String getLocaleCurrency(){
		if(defaultCurrencyCode == null){
			defaultCurrencyCode = CURRENCY_TO_DEFAULT;
			try {
				Currency currency = Currency.getInstance(App.getInstance().getLocale());
				String defaultCode = currency.getCurrencyCode();
				if(App.getInstance().getCurrencyToList().indexOf(defaultCurrencyCode) != -1){
					defaultCurrencyCode = defaultCode;
				}

			} catch (Exception e) { }
		}
		return defaultCurrencyCode;
	}
}