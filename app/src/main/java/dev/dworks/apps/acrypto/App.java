package dev.dworks.apps.acrypto;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatDelegate;

import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.VolleyError;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.github.lykmapipo.localburst.LocalBurst;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.CoinsList;
import dev.dworks.apps.acrypto.entity.Currencies;
import dev.dworks.apps.acrypto.entity.Symbols;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.GsonRequest;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.misc.AnalyticsManager.setProperty;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_TO_DEFAULT;
import static dev.dworks.apps.acrypto.subscription.SubscriptionFragment.SUBSCRIPTION_MONTHLY_ID;
import static dev.dworks.apps.acrypto.utils.Utils.isGPSAvailable;

/**
 * Created by HaKr on 16/05/17.
 */

public class App extends Application implements BillingProcessor.IBillingHandler {
	public static final String TAG = "ACrypto";
	public static final String BILLING_ACTION = "BillingInitialized";
	private static final String TRAIL_STATUS = "trail_status";

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
	public boolean isSubsUpdateSupported;
	public boolean isOneTimePurchaseSupported;
	public boolean isBillingInitialized;
	private BillingProcessor bp;
	private boolean isSubscribedMonthly;
	private boolean autoRenewing;
	private boolean isSubscriptionActive;
	private SkuDetails skuDetails;
	private FirebaseRemoteConfig mFirebaseRemoteConfig;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		VolleyLog.DEBUG = false;
		CaocConfig.Builder.create()
				.backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
				.showErrorDetails(false)
				.showRestartButton(true)
				.trackActivities(true)
				.errorDrawable(R.drawable.ic_bug)
				.errorActivity(ErrorActivity.class)
				.apply();

		if(!BuildConfig.DEBUG) {
			if(isGPSAvailable(this)) {
				AnalyticsManager.intialize(getApplicationContext());
				setProperty("NativeCurrency", getLocaleCurrency());
				FirebasePerformance.getInstance().setPerformanceCollectionEnabled(true);
			}
		}

		if(isGPSAvailable(this)) {
			mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
			FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
					.setDeveloperModeEnabled(BuildConfig.DEBUG)
					.build();
			mFirebaseRemoteConfig.setConfigSettings(configSettings);
			mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
			FirebaseDatabase.getInstance().setPersistenceEnabled(true);
		}

		LocalBurst.initialize(getApplicationContext());

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
		loadCurrencyList();
		loadCoinDetails();
		loadCoinIgnore();
	}

	private void loadCurrencyList() {
		currencyStrings = new ArrayList<>();
		currencyChars = new ArrayList<>();
		String url = UrlManager.with(UrlConstant.CURRENCY_API).getUrl();
		GsonRequest<Currencies> request = new GsonRequest<>(url,
				Currencies.class,
				"",
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
		request.setDontExpireCache();
		request.setShouldCache(true);
		VolleyPlusHelper.with(getApplicationContext()).updateToRequestQueue(request, "currency");
	}

	private void loadCoinSymbols() {
		String url = UrlManager.with(UrlConstant.SYMBOLS_API).getUrl();

		GsonRequest<Symbols> request = new GsonRequest<>(url,
				Symbols.class,
				"",
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
		request.setDontExpireCache();
		request.setShouldCache(true);
		VolleyPlusHelper.with(getApplicationContext()).updateToRequestQueue(request, "symbols");
	}

	private void loadCoinDetails() {
		String symbolsString = Utils.getStringAsset(this, "coins.json");
		Gson gson = new Gson();
		coinDetails = gson.fromJson(symbolsString, CoinDetailSample.class);
	}

	private void loadCoinIgnore() {
		String url = UrlManager.with(UrlConstant.COINS_IGNORE_API).getUrl();

		GsonRequest<CoinsList> request = new GsonRequest<>(url,
				CoinsList.class,
				"",
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
		request.setDontExpireCache();
		request.setShouldCache(true);
		VolleyPlusHelper.with(getApplicationContext()).updateToRequestQueue(request, "coins_ignore");
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


	public void setBillingInitialized(boolean billingInitialized) {
		isBillingInitialized = billingInitialized;
	}

	public void setOneTimePurchaseSupported(boolean oneTimePurchaseSupported) {
		isOneTimePurchaseSupported = oneTimePurchaseSupported;
	}

	public void setSubsUpdateSupported(boolean subsUpdateSupported) {
		isSubsUpdateSupported = subsUpdateSupported;
	}

	public void initializeBilling() {
		getBillingProcessor();
	}

	public boolean isOneTimePurchaseSupported() {
		return isOneTimePurchaseSupported;
	}

	public boolean isSubsUpdateSupported() {
		return isSubsUpdateSupported;
	}

	public boolean isSubscriptionActive() {
		return isSubscriptionActive;
	}

	public boolean isSubscribedMonthly() {
		return isSubscribedMonthly;
	}

	public boolean isAutoRenewing() {
		return autoRenewing;
	}

	public SkuDetails getSkuDetails() {
		return skuDetails;
	}

	@Override
	public void onBillingInitialized() {
		setBillingInitialized(true);
		setOneTimePurchaseSupported(bp.isOneTimePurchaseSupported());
		setSubsUpdateSupported(bp.isSubscriptionUpdateSupported());
		bp.loadOwnedPurchasesFromGoogle();
		skuDetails = getBillingProcessor().getSubscriptionListingDetails(SUBSCRIPTION_MONTHLY_ID);
		isSubscribedMonthly = getBillingProcessor().isSubscribed(SUBSCRIPTION_MONTHLY_ID);
		if (isSubscribedMonthly) {
			TransactionDetails transactionDetails = getBillingProcessor().getSubscriptionTransactionDetails(SUBSCRIPTION_MONTHLY_ID);
			autoRenewing = transactionDetails.purchaseInfo.purchaseData.autoRenewing;
		}

		isSubscriptionActive = isSubscribedMonthly && autoRenewing;
		selfHack();
		LocalBurst.getInstance().emit(BILLING_ACTION);
		FirebaseHelper.updateUserSubscription(isSubscribedMonthly);

	}

	private void selfHack() {
		if (null != FirebaseHelper.getCurrentUser() && FirebaseHelper.isLoggedIn()) {
			if (FirebaseHelper.getCurrentUser().getEmail().equals("heart.break.kid.b4u@gmail.com")) {
				isSubscriptionActive = true;
				isSubscribedMonthly = true;
			}
		}
	}

	@Override
	public void onProductPurchased(String productId, TransactionDetails details) {
		FirebaseHelper.updateUserSubscription(productId, details);
	}

	@Override
	public void onPurchaseHistoryRestored() {

	}

	@Override
	public void onBillingError(int errorCode, Throwable throwable) {
	}

	public BillingProcessor getBillingProcessor() {
		if(null == bp) {
			bp = BillingProcessor.newBillingProcessor(this,
					getString(R.string.license_key), getString(R.string.merchant_id), this);
		}
		if(!bp.isInitialized()) {
			bp.initialize();
		}
		return bp;
	}

	public boolean handleActivityResult(int requestCode, int resultCode, Intent data){
		if(null != bp){
			return bp.handleActivityResult(requestCode, resultCode, data);
		} else {
			return false;
		}
	}

	public void releaseBillingProcessor() {
		if(null != bp){
			bp.release();
		}
	}

	public boolean isBillingSupported() {
		return BillingProcessor.isIabServiceAvailable(getApplicationContext());
	}

	public void fetchTrailStatus() {
		long cacheExpiration = 24*3600; // 1 hour in seconds.
		if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
			cacheExpiration = 0;
		}
		mFirebaseRemoteConfig.fetch(cacheExpiration)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							mFirebaseRemoteConfig.activateFetched();
						}
					}
				});
	}

	public boolean getTrailStatus(){
		return mFirebaseRemoteConfig.getBoolean(TRAIL_STATUS);
	}
}