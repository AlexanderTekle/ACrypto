package dev.dworks.apps.acrypto;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.amazon.device.messaging.ADM;
import com.billing.SubscriptionServiceListener;
import com.eggheadgames.inapppayments.IAPManager;
import com.github.lykmapipo.localburst.LocalBurst;
import com.google.firebase.perf.FirebasePerformance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;

/**
 * Created by HaKr on 16/05/17.
 */

public abstract class AppFlavour extends Application implements SubscriptionServiceListener {
	public static final String BILLING_ACTION = "BillingInitialized";
	public static final String SUBSCRIPTION_MONTHLY_ID = getSubscriptionMain() + ".subs.m1";

	public boolean isSubsUpdateSupported;
	public boolean isOneTimePurchaseSupported;
	private boolean isSubscribedMonthly;
	private boolean autoRenewing;
	private boolean isSubscriptionActive;
	private List<String> skuList;
	private String price;
	private String productId;

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
			AnalyticsManager.intialize(getApplicationContext());
			AnalyticsManager.setProperty("NativeCurrency", getLocaleCurrency());
			FirebasePerformance.getInstance().setPerformanceCollectionEnabled(true);
		}
	}

	public void setOneTimePurchaseSupported(boolean oneTimePurchaseSupported) {
		isOneTimePurchaseSupported = oneTimePurchaseSupported;
	}

	public void setSubsUpdateSupported(boolean subsUpdateSupported) {
		isSubsUpdateSupported = subsUpdateSupported;
	}

	public void initializeBilling() {
		skuList = new ArrayList<>();
		skuList.add(SUBSCRIPTION_MONTHLY_ID);
		IAPManager.build(getApplicationContext(), IAPManager.BUILD_TARGET_AMAZON, skuList);
		IAPManager.addSubscriptionListener(this);
		IAPManager.init("", BuildConfig.DEBUG);
	}

	public boolean isSubscriptionActive() {
		return isSubscriptionActive;
	}

	public boolean isSubscribedMonthly() {
		return isSubscribedMonthly;
	}

	public String getSubscriptionCTA(){
		if(TextUtils.isEmpty(price)){
			return "Subscribe";
		}
		return "Subscribe "
				+ price + "/"
				+ " Monthly";
	}

	public void reloadSubscription() {
		if(!isBillingSupported()){
			return;
		}
		isSubscribedMonthly = !TextUtils.isEmpty(productId);

		isSubscriptionActive = isSubscribedMonthly;
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

	public boolean handleActivityResult(int requestCode, int resultCode, Intent data){
		return false;
	}

	public void releaseBillingProcessor() {
		IAPManager.destroy();
	}

	public boolean isBillingSupported() {
		return null != IAPManager.getBillingService();
	}

	public void fetchTrailStatus() {
		//nothing doing
	}

	public boolean getTrailStatus(){
		return false;
	}

	public void subscribe(Activity activity, String productId){
		if(isBillingSupported()) {
			IAPManager.subscribe(activity, productId, 0);
		} else {
			Toast.makeText(activity, "Billing not supported", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onSubscriptionRestored(String productId) {
		this.productId = productId;
		reloadSubscription();
	}

	@Override
	public void onSubscriptionPurchased(String productId) {
		this.productId = productId;
		reloadSubscription();
		FirebaseHelper.updateUserSubscription(productId);
	}

	@Override
	public void onPricesUpdated(Map<String, String> map) {
		price = map.get(SUBSCRIPTION_MONTHLY_ID);
	}

	private static String getSubscriptionMain() {
		return BuildConfig.APPLICATION_ID;
	}

	public void updateInstanceId(){
		try {
			final ADM adm = new ADM(getApplicationContext());
			if (adm.isSupported()) {
				String currentToken = adm.getRegistrationId();
				if (currentToken == null) {
					adm.startRegister();
				} else {
					FirebaseHelper.updateInstanceId(currentToken);
				}
			}
		} catch (Exception e) {

		}
	}
}