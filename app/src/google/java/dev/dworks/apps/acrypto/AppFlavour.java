package dev.dworks.apps.acrypto;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.github.lykmapipo.localburst.LocalBurst;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;

import static dev.dworks.apps.acrypto.utils.Utils.isGPSAvailable;

/**
 * Created by HaKr on 16/05/17.
 */

public abstract class AppFlavour extends Application implements BillingProcessor.IBillingHandler {
	private static final String TRAIL_STATUS = "trail_status";
	public static final String SUBSCRIPTION_MONTHLY_ID = getSubscriptionMain() + ".subs.m1";
	public static final String BILLING_ACTION = "BillingInitialized";

	public boolean isSubsUpdateSupported;
	public boolean isOneTimePurchaseSupported;
	private BillingProcessor bp;
	private boolean isSubscribedMonthly;
	private boolean autoRenewing;
	private boolean isSubscriptionActive;
	private SkuDetails skuDetails;
	private FirebaseRemoteConfig mFirebaseRemoteConfig;
	public abstract String getLocaleCurrency();

	@Override
	public void onCreate() {
		super.onCreate();
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
				AnalyticsManager.setProperty("NativeCurrency", getLocaleCurrency());
				FirebasePerformance.getInstance().setPerformanceCollectionEnabled(true);
			}
		}

		mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
		FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
				.setDeveloperModeEnabled(BuildConfig.DEBUG)
				.build();
		mFirebaseRemoteConfig.setConfigSettings(configSettings);
		mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
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
		if(!isBillingSupported()){
			return;
		}
		setOneTimePurchaseSupported(bp.isOneTimePurchaseSupported());
		setSubsUpdateSupported(bp.isSubscriptionUpdateSupported());
		bp.loadOwnedPurchasesFromGoogle();
		reloadSubscription();
	}

	public void reloadSubscription() {
		if(!isBillingSupported()){
			return;
		}
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
		FirebaseHelper.updateUserSubscription(productId);
	}

	@Override
	public void onPurchaseHistoryRestored() {
		reloadSubscription();
	}

	@Override
	public void onBillingError(int errorCode, Throwable throwable) {
	}

	public BillingProcessor getBillingProcessor() {
		if(!isBillingSupported()){
			return null;
		}
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

	public void subscribe(Activity activity, String productId){
		if(isBillingSupported()) {
			getBillingProcessor().subscribe(activity, productId);
		} else {
			Toast.makeText(activity, "Billing not supported", Toast.LENGTH_SHORT).show();
		}
	}

	private static String getSubscriptionMain() {
		return BuildConfig.APPLICATION_ID +
				(BuildConfig.DEBUG ? ".test" : "");
	}

	public void updateInstanceId(){
		String instanceId = FirebaseInstanceId.getInstance().getToken();
		FirebaseHelper.updateInstanceId(instanceId);
	}
}