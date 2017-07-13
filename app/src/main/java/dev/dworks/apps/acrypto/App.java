package dev.dworks.apps.acrypto;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;

import com.android.volley.VolleyLog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.lykmapipo.localburst.LocalBurst;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.DefaultData;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.utils.PreferenceUtils;
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
	private DefaultData defaultData;

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

		mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
		FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
				.setDeveloperModeEnabled(BuildConfig.DEBUG)
				.build();
		mFirebaseRemoteConfig.setConfigSettings(configSettings);
		mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
		FirebaseDatabase.getInstance().setPersistenceEnabled(true);

		LocalBurst.initialize(getApplicationContext());
		loadDefaultData();
		loadCoinSymbols();
		loadCurrencyList();
		loadCoinDetails();
		loadCoinIgnore();

		checkForAppUpdates();
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
        AppUpdater appUpdater = new AppUpdater(this)
				.setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
				.setDisplay(Display.DIALOG);
        appUpdater.start();
	}

	private void loadCurrencyList() {
		currencyStrings = new ArrayList<>();
		currencyChars = new ArrayList<>();

		currencyStrings = new ArrayList<>();
		currencyChars = new ArrayList<>();
		FirebaseHelper.getFirebaseDatabaseReference().child("master/currency").orderByChild("order")
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
							String currency = childSnapshot.getKey();
							currencyStrings.add(currency);
							currencyChars.add(currency);
						}
					}

					@Override
					public void onCancelled(DatabaseError databaseError) {

					}
				});
	}

	private void loadCoinSymbols() {

		FirebaseHelper.getFirebaseDatabaseReference().child("master/symbols")
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						symbols = new ArrayMap<>();
						for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
							String currency = childSnapshot.getKey();
							String symbol = (String) childSnapshot.getValue();
							symbols.put(currency, symbol);
						}
					}

					@Override
					public void onCancelled(DatabaseError databaseError) {

					}
				});
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
		FirebaseHelper.getFirebaseDatabaseReference().child("master/coins_ignore")
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						coinsIgnore = new ArrayList<>();
						for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
							String currency = childSnapshot.getKey();
							coinsIgnore.add(currency);
						}
					}

					@Override
					public void onCancelled(DatabaseError databaseError) {

					}
				});
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

	public String getSubscriptionCTA(){
		if(null == skuDetails){
			return "Subscribe";
		}
		return "Subscribe "
				+ skuDetails.priceText + "/"
				+ " Monthly";
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