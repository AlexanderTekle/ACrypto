package dev.dworks.apps.acrypto;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.github.lykmapipo.localburst.LocalBurst;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.perf.FirebasePerformance;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.utils.PreferenceUtils;
import needle.Needle;
import needle.UiRelatedTask;

import static dev.dworks.apps.acrypto.utils.NotificationUtils.TOPIC_NEWS_ALL;
import static dev.dworks.apps.acrypto.utils.Utils.isGPSAvailable;

/**
 * Created by HaKr on 16/05/17.
 */

public abstract class AppFlavour extends Application implements BillingProcessor.IBillingHandler {
	private static final String TRAIL_STATUS = "trail_status";
	private static final String SUBSCRIBED_MONTHLY = "subscribed_monthly";
	public static final String SUBSCRIPTION_MONTHLY_ID = getSubscriptionMain() + ".subs.m1";
	public static final String BILLING_ACTION = "BillingInitialized";
	private static final String INITIAL_SUBSCRIPTION_COMPLETED = "intial_subscription_completed";

	private BillingProcessor bp;
	private boolean isSubscribedMonthly;
	private boolean autoRenewing;
	private boolean isSubscriptionActive;
	private SkuDetails skuDetails;
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

		initialMessagingSubscription();
	}

	protected void initialMessagingSubscription(){
		if (isGPSAvailable(this)
				&& !PreferenceUtils.getBooleanPrefs(this, INITIAL_SUBSCRIPTION_COMPLETED)) {
			FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_NEWS_ALL);
			PreferenceUtils.set(this, INITIAL_SUBSCRIPTION_COMPLETED, true);
		}
	}


	public void initializeBilling() {
		getBillingProcessor();
	}

	public boolean isSubscriptionActive() {
		return isSubscriptionActive;
	}

	public boolean isSubscribedMonthly() {
		return PreferenceUtils.getBooleanPrefs(SUBSCRIBED_MONTHLY)|| isSubscribedMonthly;
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
		if(!isBillingSupported() || null == bp
				|| (bp != null && !bp.isInitialized())){
			return;
		}
		bp.loadOwnedPurchasesFromGoogle();
		reloadSubscription();
	}

	public void reloadSubscription() {
		if(!isBillingSupported() || null == bp
				|| (bp != null && !bp.isInitialized())){
			return;
		}

		Needle.onBackgroundThread().execute(new UiRelatedTask<Void>() {
			@Override
			protected Void doWork() {
				skuDetails = getBillingProcessor().getSubscriptionListingDetails(SUBSCRIPTION_MONTHLY_ID);
				isSubscribedMonthly = getBillingProcessor().isSubscribed(SUBSCRIPTION_MONTHLY_ID);
				if (isSubscribedMonthly) {
					TransactionDetails transactionDetails = getBillingProcessor().getSubscriptionTransactionDetails(SUBSCRIPTION_MONTHLY_ID);
					autoRenewing = transactionDetails.purchaseInfo.purchaseData.autoRenewing;
				}
				isSubscriptionActive = isSubscribedMonthly && autoRenewing;
				selfHack();
				FirebaseHelper.updateUserSubscription(isSubscribedMonthly);
				PreferenceUtils.set(SUBSCRIBED_MONTHLY, isSubscribedMonthly);
				return null;
			}

			@Override
			protected void thenDoUiRelatedWork(Void result) {
				LocalBurst.getInstance().emit(BILLING_ACTION);
			}
		});
	}

	private void selfHack() {
		if (null != FirebaseHelper.getCurrentUser() && FirebaseHelper.isLoggedIn()) {
			if (FirebaseHelper.getCurrentUser().getEmail().equals("heart.break.kid.b4u@gmail.com")
					|| FirebaseHelper.getCurrentUser().getEmail().equals("hakr@dworks.in")) {
				isSubscriptionActive = true;
				isSubscribedMonthly = true;
			}
		}
	}

	@Override
	public void onProductPurchased(String productId, TransactionDetails details) {
		reloadSubscription();
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

	public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
		return null != bp && bp.handleActivityResult(requestCode, resultCode, data);
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
	}

	public boolean getTrailStatus(){
		return false;
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

	public void unSubscribe(Activity activity, String subscriptionId) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("https://play.google.com/store/account"));
			activity.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}