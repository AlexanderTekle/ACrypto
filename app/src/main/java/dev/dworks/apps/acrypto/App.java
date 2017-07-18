package dev.dworks.apps.acrypto;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;

import com.android.volley.VolleyLog;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.lykmapipo.localburst.LocalBurst;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.DefaultData;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
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

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		VolleyLog.DEBUG = false;

		FirebaseDatabase.getInstance().setPersistenceEnabled(true);

		LocalBurst.initialize(getApplicationContext());
		loadDefaultData();
		loadCoinSymbols();
		loadCurrencyList();
		loadCoinDetails();
		loadCoinIgnore();

		FirebaseHelper.checkInstanceIdValidity();
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
}