package dev.dworks.apps.acrypto;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.VolleyError;
import com.github.lykmapipo.localburst.LocalBurst;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.CoinPairs;
import dev.dworks.apps.acrypto.entity.CoinsList;
import dev.dworks.apps.acrypto.entity.Currencies;
import dev.dworks.apps.acrypto.entity.DefaultData;
import dev.dworks.apps.acrypto.entity.Symbols;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.LruCoinPairCache;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.MasterGsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusMasterHelper;
import dev.dworks.apps.acrypto.utils.PreferenceUtils;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_TO_DEFAULT;

/**
 * Created by HaKr on 16/05/17.
 */

public class App extends AppFlavour {
	public static final String TAG = "ACrypto";

	static {
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}

	public static String APP_VERSION;
    public static int APP_VERSION_CODE;
	private static App sInstance;
	private Locale current;
	public ArrayMap<String, String> symbols;
	public ArrayList<String> coinsIgnore;
	private CoinDetailSample coinDetails;
	private ArrayList<String> currencyStrings;
	private ArrayList<CharSequence> currencyChars;
	private String defaultCurrencyCode;
	private DefaultData defaultData;
	private LruCoinPairCache coinPairCache = new LruCoinPairCache();

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		VolleyLog.DEBUG = false;

		FirebaseDatabase.getInstance().setPersistenceEnabled(true);

		LocalBurst.initialize(getApplicationContext());
		synMasterData();

		FirebaseHelper.checkInstanceIdValidity();
		checkForAppUpdates();
	}

	public void synMasterData() {
		cleanupMasterData();
		loadDefaultData();
		loadCoinSymbols();
		loadCurrencyList();
		loadCoinDetails();
		loadCoinIgnore();
	}

	private void checkForAppUpdates() {

		try {
			final PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			APP_VERSION = info.versionName;
			APP_VERSION_CODE = info.versionCode;
			String currentVersion = PreferenceUtils.getStringPrefs(this, Utils.APP_VERSION, "");
			if(TextUtils.isEmpty(currentVersion) || !currentVersion.equals(APP_VERSION)) {
				PreferenceUtils.set(this, Utils.APP_VERSION, APP_VERSION);
				FirebaseHelper.updateUserAppVersion(APP_VERSION);
			}
		} catch (PackageManager.NameNotFoundException e) {
			APP_VERSION = "Unknown";
			APP_VERSION_CODE = 0;
			e.printStackTrace();
		}
	}

	private void loadCurrencyList() {
		currencyStrings = new ArrayList<>();
		currencyChars = new ArrayList<>();

		String url = UrlManager.with(UrlConstant.CURRENCY_API).getUrl();
		MasterGsonRequest<Currencies> request = new MasterGsonRequest<>(url,
				Currencies.class,
				new Response.Listener<Currencies>() {
					@Override
					public void onResponse(Currencies list) {

						for (Currencies.Currency currency: list.currencies) {
							currencyStrings.add(currency.code);
							currencyChars.add(currency.code);
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError volleyError) {

					}
				});
		request.setMasterExpireCache();
		request.setShouldCache(true);
		VolleyPlusMasterHelper.with(getApplicationContext()).updateToRequestQueue(request, "currency");
	}

	private void loadCoinSymbols() {
		String url = UrlManager.with(UrlConstant.SYMBOLS_API).getUrl();

		MasterGsonRequest<Symbols> request = new MasterGsonRequest<>(url,
				Symbols.class,
				new Response.Listener<Symbols>() {
					@Override
					public void onResponse(Symbols list) {
						symbols = new ArrayMap<>();

						for (Symbols.Symbol sym: list.symbols) {
							symbols.put(sym.code, sym.symbol);
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError volleyError) {

					}
				});
		request.setMasterExpireCache();
		request.setShouldCache(true);
		VolleyPlusMasterHelper.with(getApplicationContext()).updateToRequestQueue(request, "symbols");
	}

	private void loadCoinDetails() {
		String symbolsString = Utils.getStringAsset(this, "coins.json");
		Gson gson = new Gson();
		coinDetails = gson.fromJson(symbolsString, CoinDetailSample.class);
	}

	private void loadDefaultData() {
		defaultData = new DefaultData();
		String data = Utils.getStringAsset(this, "coins_top.json");
		Gson gson = new Gson();
		defaultData = gson.fromJson(data, DefaultData.class);
	}

	private void loadCoinIgnore() {
		String url = UrlManager.with(UrlConstant.COINS_IGNORE_API).getUrl();

		MasterGsonRequest<CoinsList> request = new MasterGsonRequest<>(url,
				CoinsList.class,
				new Response.Listener<CoinsList>() {
					@Override
					public void onResponse(CoinsList list) {
						coinsIgnore = new ArrayList<>();

						for (CoinsList.Currency currency: list.coins_list) {
							coinsIgnore.add(currency.code);
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError volleyError) {

					}
				});
		request.setMasterExpireCache();
		request.setShouldCache(true);
		VolleyPlusMasterHelper.with(getApplicationContext()).updateToRequestQueue(request, "coins_ignore");
	}

	public ArrayMap<String, String> getSymbols(){
		if(null == symbols) {
			return new ArrayMap<>();
		}
		return symbols;
	}

	public ArrayList<String> getCoinsIgnore(){
		if(null == coinsIgnore) {
			return new ArrayList<>();
		}
		return coinsIgnore;
	}

	public DefaultData getDefaultData(){
		if(null == defaultData) {
			return new DefaultData();
		}
		return defaultData;
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
		if(null == currencyStrings) {
			return new ArrayList<>();
		}
		return currencyStrings;
	}

	public ArrayList<CharSequence> getCurrencyCharsList() {
		if(null == currencyChars) {
			return new ArrayList<>();
		}
		return currencyChars;
	}

	public void cleanupMasterData(){
		coinsIgnore = null;
		symbols = null;
		currencyStrings = null;
		currencyChars = null;
	}

	@Override
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

	public CoinPairs.CoinPair getCachedCoinPair(String key) {
		return coinPairCache.getCoinDetail(key);
	}

	public void putCoinPairCache(String key, CoinPairs.CoinPair coinPair) {
		this.coinPairCache.putCoinDetail(key, coinPair);
	}
}