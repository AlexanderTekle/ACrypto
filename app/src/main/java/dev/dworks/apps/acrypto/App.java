package dev.dworks.apps.acrypto;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatDelegate;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.FirebasePerformance;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.misc.AnalyticsManager.setProperty;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.CURRENCY_TO_DEFAULT;
import static dev.dworks.apps.acrypto.utils.Utils.isGPSAvailable;

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
	public ArrayMap<String, String> symbols;
	public ArrayList<String> coinsIgnore;
	private CoinDetailSample coinDetails;
	private ArrayList<String> currencyStrings;
	private ArrayList<CharSequence> currencyChars;
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
			if(isGPSAvailable(this)) {
				AnalyticsManager.intialize(getApplicationContext());
				setProperty("NativeCurrency", getLocaleCurrency());
				FirebasePerformance.getInstance().setPerformanceCollectionEnabled(true);
			}
		}

		if(isGPSAvailable(this)) {
			FirebaseDatabase.getInstance().setPersistenceEnabled(true);
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
		loadCurrencyList();
		loadCoinSymbols();
		loadCoinDetails();
		loadCoinIgnore();

		FirebaseHelper.startMasterDataSync();
	}

	private void loadCurrencyList() {
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
		symbols = new ArrayMap<>();
		FirebaseHelper.getFirebaseDatabaseReference().child("master/symbols")
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
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

	private void loadCoinIgnore() {
		coinsIgnore = new ArrayList<>();
		FirebaseHelper.getFirebaseDatabaseReference().child("master/coins_ignore")
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
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
		return symbols;
	}

	public ArrayList<String> getCoinsIgnore(){
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
		return currencyStrings;
	}


	public ArrayList<CharSequence> getCurrencyCharsList() {
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
}