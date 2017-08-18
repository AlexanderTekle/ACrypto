package dev.dworks.apps.acrypto.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.request.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.fabiomsr.moneytextview.MoneyTextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.psdev.licensesdialog.LicensesDialog;
import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.BuildConfig;
import dev.dworks.apps.acrypto.LoginActivity;
import dev.dworks.apps.acrypto.MainActivity;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.AppFeedback;
import dev.dworks.apps.acrypto.misc.FirebaseHelper;
import dev.dworks.apps.acrypto.misc.RoundedNumberFormat;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.settings.SettingsActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_AUTO;
import static android.text.Html.FROM_HTML_MODE_LEGACY;
import static dev.dworks.apps.acrypto.App.SUBSCRIPTION_MONTHLY_ID;

/**
 * Created by HaKr on 20-Sep-14.
 */
public class Utils {

    public static final String AUTH_HEADER =  "X-AUTH-TOKEN";
    public static final String CLIENT_HEADER =  "X-CLIENT-VERSION";

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {}

    // delay to launch nav drawer item, to allow close animation to play
    public static final int NAVDRAWER_LAUNCH_DELAY = 250;

    // cache
    public static final int IMAGE_SIZE_BIG = 100;
    public static final int IMAGE_SIZE = 50;
    public static final int SCALE_DOWN_SIZE = 1024;
    public static final int SCALE_DOWN_SIZE_SMALL = 512;
    public static final String IMAGE_CACHE_DIR = "thumbs";
    public static final String IMAGE_BG_CACHE_DIR = "bgs";

    static final String TAG = "Utils";

    public static final String APP_VERSION = "app_version";

    public static final String REQUIRED = "Required";

    //Home
    public static final String BUNDLE_NAME = "bundle_name";

    //News
    public static final String BUNDLE_NEWS = "bundle_news";

    //Portfolio
    public static final String BUNDLE_PORTFOLIO = "bundle_portfolio";
    public static final String BUNDLE_PORTFOLIO_COIN = "bundle_portfolio_coin";
    public static final String BUNDLE_PORTFOLIO_COIN_FROM = "bundle_portfolio_coin_from";

    //Alert
    public static final String BUNDLE_ALERT_TYPE = "bundle_alert_type";
    public static final String BUNDLE_ALERT = "bundle_alert";
    public static final String BUNDLE_REF_KEY = "bundle_ref_key";
    public static final String BUNDLE_BUY_REF_KEY = "bundle_buy_ref_key";
    public static final String BUNDLE_TYPE = "bundle_type";

    //Coins
    public static final String BUNDLE_CURRENCY = "bundle_currency";
    public static final String BUNDLE_COINS = "bundle_coins";
    public static final String BUNDLE_COIN = "bundle_coin";
    public static final String BUNDLE_SCREEN_NAME = "bundle_screen_name";

    public static final String REGISTERED_VERSION_CODE = "registered_version_code";
    public static final String REQUIRED_VERSION_CODE = "required_version_code";
    public static final String APP_DEPRECATED = "app_deprecated";
    public static final String APP_UPDATE_AVAILABLE = "app_update_available";

    public static final String INDICATIVE_IMAGES_MSG_SHOWN = "indicative_images_msg_shown";

    public static final String INTERSTITIAL_APP_UNIT_ID = "ca-app-pub-6407484780907805/5183261278";
    public static final String NATIVE_APP_UNIT_ID = "ca-app-pub-6407484780907805/1075754433";

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int type, Bundle bundle);
    }

    public static void log(String t, String s) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        String topic = "ACRYPTO-DROID";
        if (t != null) {
            topic = topic + " " + t;
        }
        if (!TextUtils.isEmpty(s)) {
            android.util.Log.w(topic, s);
        }
    }

    public static void log(String s) {
        log(null, s);
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean hasO() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean hasMoreHeap(){
        return Runtime.getRuntime().maxMemory() > 20971520;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isLowRamDevice(Context context) {
        if(Utils.hasKitKat()){
            final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            return am.isLowRamDevice();
        }
        return !hasMoreHeap();
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static boolean isNetConnected(Context context) {
        if(null == context){
            return false;
        }
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String capitalizeFirstLetter(String original) {
        if (original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase(App.getInstance().getLocale()) + original.substring(1);
    }


    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isActivityAlive(Activity activity) {
        return !(null == activity || (Utils.hasJellyBeanMR1() ? activity.isDestroyed() : activity.isFinishing()));
    }

    public static void showSnackBar(Activity activity, String text){
        if(null == activity){
            return;
        }
        showSnackBar(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT);
    }

    public static void showSnackBar(View view, String text){
        showSnackBar(view, text, Snackbar.LENGTH_SHORT);
    }

    public static void showSnackBar(View view, String text, String action, View.OnClickListener listener){
        showSnackBar(view, text, Snackbar.LENGTH_SHORT, action, listener);
    }

    public static void showRetrySnackBar(View view, String text, View.OnClickListener listener){
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
        if (null != listener) {
            snackbar.setAction("RETRY", listener)
                    .setActionTextColor(view.getResources().getColor(R.color.colorAccent));
        }
        snackbar.show();
    }

    public static void showRetrySnackBar(Activity activity, String text, View.OnClickListener listener){
        if(null == activity){
            return;
        }
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_INDEFINITE);
        if (null != listener) {
            snackbar.setAction("RETRY", listener)
                    .setActionTextColor(activity.getResources().getColor(R.color.colorAccent));
        }
        snackbar.show();
    }

    public static void showSnackBar(View view, String text, int duration){
        Snackbar.make(view, text, duration).show();
    }

    public static void showSnackBar(View view, String text, int duration, String action, View.OnClickListener listener){
        Snackbar.make(view, text, duration).setAction(action, listener).show();
    }

    public static void showSnackBar(Activity activity, String text, int duration, String action, View.OnClickListener listener){
        if(null == activity){
            return;
        }
        Snackbar.make(activity.findViewById(android.R.id.content), text, duration).setAction(action, listener).show();
    }

    public static void showNoInternetSnackBar(Activity activity, View.OnClickListener listener) {
        if(null == activity){
            return;
        }
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content),
                "Can't connect to Internet", Snackbar.LENGTH_INDEFINITE);

        if (null != listener) {
            snackbar.setAction("RETRY", listener)
                    .setActionTextColor(activity.getResources().getColor(R.color.colorAccent));
        }
        snackbar.show();
    }

    public static void showNoInternetSnackBar(View view, View.OnClickListener listener){
        Snackbar snackbar = Snackbar.make(view, "Can't connect to Internet", Snackbar.LENGTH_INDEFINITE);
        if (null != listener) {
            snackbar.setAction("RETRY", listener)
                    .setActionTextColor(view.getResources().getColor(R.color.colorAccent));
        }
        snackbar.show();
    }

    public static void setClipView(View view, boolean clip) {
        if (view != null) {
            ViewParent parent = view.getParent();
            if(parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view.getParent();
                viewGroup.setClipChildren(clip);
                setClipView(viewGroup, clip);
            }
        }
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static String getPrice(int price){
        return price == 0 ? ""
                : String.format("%1$s %2$s", "\u20B9", Integer.toString(price));
    }

    @Visibility
    public static int getVisibility(boolean show) {
        return show ? View.VISIBLE : View.GONE;
    }

    public static String firstTimeHash(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(s.getBytes());
            String hash = bytesToHexString(digest.digest());
            return hash.substring(20) + hash.substring(0, 20);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

/*    public static void showIndicativeMessage(Activity activity, boolean force){
        boolean messageShown = PreferenceUtils.getBooleanPrefs(activity, Utils.INDICATIVE_IMAGES_MSG_SHOWN);
        if(force || !messageShown){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dialog)
                    .setTitle("Images in Shifoo")
                    .setMessage(R.string.message_indicative_images)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            DialogFragment.showThemedDialog(builder);
            PreferenceUtils.set(activity, Utils.INDICATIVE_IMAGES_MSG_SHOWN, true);
        }
    }*/

    public static int getAccentColor(Context context){
        return ContextCompat.getColor(context, R.color.colorAccent);
    }

    public static void tintWidget(View view) {
        tintWidget(view, getAccentColor(view.getContext()));
    }

    public static void tintButton(Button view) {
        view.setTextColor(getAccentColor(view.getContext()));
    }

    public static void tintWidget(View view, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTint(wrappedDrawable.mutate(), color);
        view.setBackgroundDrawable(wrappedDrawable);
    }

    public static String getStringAsset(Context context, String assetName) {
        String text = "";
        // Programmatically load text from an asset and place it into the
        // text view. Note that the text we are loading is ASCII, so we
        // need to convert it to UTF-16.
        try {
            InputStream is = context.getAssets().open(assetName);

            // We guarantee that the available method returns the total
            // size of the asset... of course, this does mean that a single
            // asset can't be more than 2 gigs.
            int size = is.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Convert the buffer into a string.
            text = new String(buffer);

        } catch (IOException e) {
            // Should never happen!
        }
        return text;
    }

    public static void openCustomTabUrl(Activity activity, String url){
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        builder.setSecondaryToolbarColor(ContextCompat.getColor(activity, R.color.colorAccent));
        builder.setCloseButtonIcon(getVector2Bitmap(activity, R.drawable.ic_back));
        builder.setStartAnimations(activity, 0, 0);
        builder.setExitAnimations(activity, 0, 0);
        builder.addDefaultShareMenuItem();
        builder.enableUrlBarHiding();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        try {
            customTabsIntent.launchUrl(activity, Uri.parse(url));
        } catch (ActivityNotFoundException ex) {
            showSnackBar(activity, "Cant Open");
        }
    }

    public static void openCustomTabUrlDelayed(final Activity activity, final String url){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openCustomTabUrl(activity, url);
            }
        }, NAVDRAWER_LAUNCH_DELAY);
    }

    public static void openUrl(Context context, String url){
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static long getCurrentTimeInMillis() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static long getTimestamp(int days) {
        return getTimestamp(days, 0, 0);
    }

    public static long getTimestamp(int days, int hours, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -days);
        calendar.add(Calendar.HOUR, -hours);
        calendar.add(Calendar.MINUTE, -minutes);
        long currTimeInUtc = calendar.getTimeInMillis();
        return (currTimeInUtc)/1000;
    }

    public static String getFormattedTime(long timestampInMilliSeconds, String format) {
        Date date = new Date();
        date.setTime(timestampInMilliSeconds);
        String formattedDate;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        formattedDate = simpleDateFormat.format(date);
        return formattedDate;
    }

    public static int getColor(Fragment fragment, int resId){
        if(null != fragment && fragment.isAdded()){
            return ContextCompat.getColor(fragment.getActivity(), resId);
        }
        return 0;
    }

    public static String getString(Fragment fragment, int resId, Object... formatArgs){
        if(null != fragment && fragment.isAdded()){
            return fragment.getResources().getString(resId, formatArgs);
        }
        return "";
    }

    public static int getValueDifferenceColor(double value){
        int colorRes = R.color.accent_white;
        if(value > 0){
            colorRes = R.color.accent_teal;
        }
        else if(value < 0){
            colorRes = R.color.accent_red;
        }
        return colorRes;
    }

    public static int getPercentDifferenceColor(double value){
        int colorRes = R.color.accent_black;
        if(value > 0){
            colorRes = R.color.accent_teal;
        }
        else if(value < 0){
            colorRes = R.color.accent_red;
        }
        return colorRes;
    }

    public static void setNumberValue(MoneyTextView textView, double value, String symbol){
        textView.setDecimalFormat(new RoundedNumberFormat());
        textView.setAmount((float) Math.abs(value));
        textView.setSymbol(symbol);
    }

    public static void setTotalPriceValue(MoneyTextView textView, double value, String symbol){
        String valueText = String.valueOf((int) Math.abs(value));
        if(valueText.length() >= 6){
            textView.setDecimalFormat(new RoundedNumberFormat());
        } else {
            textView.setDecimalFormat(getMoneyTotalFormat(symbol));
        }
        textView.setAmount((float) value);
        textView.setSymbol(symbol);
    }

    public static void setPriceValue(MoneyTextView textView, double value, String symbol){
        textView.setDecimalFormat(getMoneyFormat(value, symbol));
        textView.setAmount((float) Math.abs(value));
        textView.setSymbol(symbol);
    }

    public static void setDecimalValue(TextView textView, double value, String symbol){
        textView.setText(getDecimalFormat(symbol).format(Math.abs(value)));
    }

    public static void setDateTimeValue(TextView textView, long timeInMillis){
        textView.setText(TimeUtils.getFormattedDateTime(timeInMillis));
    }

    public static DecimalFormat getDecimalFormat(String symbol){
        String precisionFormat = "#####0.###";

        if("Ƀ".compareTo(symbol) == 0){
            precisionFormat = "#####0.00000000";
        } else if("Ξ".compareTo(symbol) == 0){
            precisionFormat = "#####0.00000000";
        }
        DecimalFormat decimalFormat = new DecimalFormat(precisionFormat);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        return decimalFormat;
    }

    public static DecimalFormat getMoneyFormat(boolean high){
        String precisionFormat = high ? "###,##0.###" : "###,##0.##";
        DecimalFormat decimalFormat = new DecimalFormat(precisionFormat);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        return decimalFormat;
    }

    public static DecimalFormat getIntegerFormat(double value, String symbol){
        String precisionFormat = "###,##0";
        if(Math.abs(value) < 1){
            precisionFormat = "###,##0.###";
        }

        if("Ƀ".compareTo(symbol) == 0){
            precisionFormat = "###,##0.00000000";
        } else if("Ξ".compareTo(symbol) == 0){
            precisionFormat = "###,##0.00000000";
        }
        DecimalFormat decimalFormat = new DecimalFormat(precisionFormat);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        return decimalFormat;
    }

    public static DecimalFormat getMoneyFormat(double value, String symbol){
        String precisionFormat = "###,##0.###";
        if(Math.abs(value) < 1){
            precisionFormat = "###,##0.######";
        }
        if("Ƀ".compareTo(symbol) == 0){
            precisionFormat = "###,##0.00000000";
        } else if("Ξ".compareTo(symbol) == 0){
            precisionFormat = "###,##0.00000000";
        }
        DecimalFormat decimalFormat = new DecimalFormat(precisionFormat);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        return decimalFormat;
    }

    public static DecimalFormat getMoneyTotalFormat(String symbol){
        String precisionFormat = "###,##0.###";

        if("Ƀ".compareTo(symbol) == 0){
            precisionFormat = "###,##0.00000000";
        } else if("Ξ".compareTo(symbol) == 0){
            precisionFormat = "###,##0.00000000";
        }
        DecimalFormat decimalFormat = new DecimalFormat(precisionFormat);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        return decimalFormat;
    }

    public static boolean isRTL() {
        return TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())
                == android.support.v4.view.ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public static Uri getAppUri(){
        return Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID);
    }

    public static Uri getAppShareUri(){
        return Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
    }

    public static Uri getAppStoreUri(){
        return Uri.parse("https://play.google.com/store/apps/dev?id=8683545855643814241");
    }

    public static void openSupport(Activity activity){
        ShareCompat.IntentBuilder
                .from(activity)
                .setEmailTo(new String[]{"hakr@dworks.in"})
                .setSubject("ACrypto Support")
                .setType("text/email")
                .setChooserTitle("Contact Support")
                .setText("ACrypto app version v"+ App.APP_VERSION)
                .startChooser();
    }

    public static void openFeedback(Activity activity){
        ShareCompat.IntentBuilder
                .from(activity)
                .setEmailTo(new String[]{"hakr@dworks.in"})
                .setSubject("ACrypto Feedback")
                .setType("text/email")
                .setChooserTitle("Send Feedback")
                .setText("ACrypto app version v"+ App.APP_VERSION)
                .startChooser();
    }

    public static void openPlaystore(Context çontext){
        Intent intent = new Intent(Intent.ACTION_VIEW, Utils.getAppUri());
        if(Utils.isIntentAvailable(çontext, intent)) {
            çontext.startActivity(intent);
        }
    }

    public static Bitmap getVector2Bitmap(Context context, int id) {
        VectorDrawableCompat vectorDrawable = VectorDrawableCompat.create(context.getResources(), id, context.getTheme());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static void generateJson(final Context context) {

        StringRequest request2 = new StringRequest(
                "https://api.coinmarketcap.com/v1/ticker/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Type coinType = new TypeToken<ArrayList<CoinDetailSample.CoinDetail>>() {}.getType();
                        ArrayList<CoinDetailSample.CoinDetail> array = new Gson().fromJson(s, coinType);
                        CoinDetailSample sample = new CoinDetailSample();
                        for (CoinDetailSample.CoinDetail coin : array){
                            sample.coins.put(coin.symbol, new CoinDetailSample.CoinDetail(coin.id, coin.name));
                        }
                        String values = new Gson().toJson(sample);
                        try {
                            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
                            if (!root.exists()) {
                                root.mkdirs();
                            }
                            File gpxfile = new File(root, "coins.txt");
                            FileWriter writer = new FileWriter(gpxfile);
                            writer.append(values);
                            writer.flush();
                            writer.close();
                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                null);

        VolleyPlusHelper.with(context).addToRequestQueue(request2);
    }

    public static String getDisplayPercentageSimple(double valueOne, double valueTwo){
        valueOne = valueOne == 0 ? 1 : valueOne;
        double value = ((valueTwo - valueOne)/valueOne) * 100;
        if(value == 0 || (valueOne == 0 && valueTwo ==0)){
            return " - ";
        }
        boolean roundoff = false;
        if(value >  500){
            roundoff = true;
            value = 500;
        }
        return String.format("%.2f", Math.abs(value))
                + (roundoff ? "+" : "" )
                + "% " + (value > 0 ? "↑" : "↓");
    }

    public static String getDisplayPercentageRounded(double valueOne, double valueTwo){
        valueOne = valueOne == 0 ? 1 : valueOne;
        double value = ((valueTwo - valueOne)/valueOne) * 100;
        if(value == 0 || (valueOne == 0 && valueTwo ==0)){
            return " - ";
        }
        return String.valueOf(Math.round(Math.abs(value))) +  "% " + (value > 0 ? "▲" : "▼");
    }

    public static String getDisplayPercentage(double valueOne, double valueTwo){
        valueOne = valueOne == 0 ? 1 : valueOne;
        double value = ((valueTwo - valueOne)/valueOne) * 100;
        if(value == 0 || (valueOne == 0 && valueTwo ==0)){
            return " - ";
        }
        return String.format("%.2f", Math.abs(value)) +  "% " + (value > 0 ? "▲" : "▼");
    }

    public static String getDisplayShortPercentage(double valueOne, double valueTwo){
        valueOne = valueOne == 0 ? 1 : valueOne;
        double value = ((valueTwo - valueOne)/valueOne) * 100;
        if(value == 0 || (valueOne == 0 && valueTwo ==0)){
            return " - ";
        }
        return String.format("%.2f", Math.abs(value)) +  "%";
    }

    public static String getCurrencySymbol(String currencyTo){
        String currencyToSymbol = "";
        try {
            currencyToSymbol = App.getInstance().getSymbols().get(currencyTo);
        } catch (Exception e){
            Currency currency = Currency.getInstance(currencyTo);
            currencyToSymbol = currency.getSymbol();
        } finally {
            if(TextUtils.isEmpty(currencyToSymbol)){
                currencyToSymbol = currencyTo;
            }
        }
        return currencyToSymbol;
    }

    public static String getDisplayCurrency(Double value){
        return String.format("%.2f", Math.abs(value));
    }

    public static String roundDouble(String value){
        return roundDouble(Double.valueOf(value));
    }

    public static String roundDouble(Double value){
        RoundedNumberFormat roundedNumberFormat = new RoundedNumberFormat();
        return roundedNumberFormat.format(value);
    }

    public static String getFormattedInteger(double value, String symbol){
        DecimalFormat decimalFormat = getIntegerFormat(value, symbol);
        return decimalFormat.format(value);
    }

    public static String getFormattedNumber(double value, String symbol){
        DecimalFormat decimalFormat = getMoneyFormat(value, symbol);
        return decimalFormat.format(value);
    }

    public static String formatDoubleValue(String value) {
        return formatDoubleValue(Double.valueOf(value));
    }

    public static String formatDoubleValue(double value) {
        int power;
        String suffix = " KMBT";
        String formattedNumber = "";

        if(value == 0){
            return "-";
        }
        NumberFormat formatter = new DecimalFormat("#,###.#");
        power = (int)StrictMath.log10(value);
        value = value/(Math.pow(10,(power/3)*3));
        formattedNumber=formatter.format(value);
        formattedNumber = formattedNumber + suffix.charAt(power/3);
        return formattedNumber.length()>4 ?  formattedNumber.replaceAll("\\.[0-9]+", "") : formattedNumber;
    }

    public static void showAppFeedback(Activity activity){
        showAppFeedback(activity, false);
    }

    public static void showAppFeedback(Activity activity, boolean isPro){
        AppFeedback appFeedback = AppFeedback.with(activity, R.id.container_rate);
        if(isPro){
            appFeedback.hide();
            return;
        }
        appFeedback.listener(new AppFeedback.OnShowListener() {
            @Override
            public void onRateAppShowing() {
                AnalyticsManager.logEvent("feedback_shown");
            }

            @Override
            public void onRateAppDismissed() {
                AnalyticsManager.logEvent("feedback_dismissed");
            }

            @Override
            public void onRateAppClicked() {
                AnalyticsManager.logEvent("feedback_given");
            }
        }).checkAndShow();
    }

    public static int darker(int color, float factor) {
        return Color.argb(Color.alpha(color), Math.max((int)((float)Color.red(color) * factor), 0), Math.max((int)((float)Color.green(color) * factor), 0), Math.max((int)((float)Color.blue(color) * factor), 0));
    }

    public static int lighter(int color, float factor) {
        int red = (int)(((float)Color.red(color) * (1.0F - factor) / 255.0F + factor) * 255.0F);
        int green = (int)(((float)Color.green(color) * (1.0F - factor) / 255.0F + factor) * 255.0F);
        int blue = (int)(((float)Color.blue(color) * (1.0F - factor) / 255.0F + factor) * 255.0F);
        return Color.argb(Color.alpha(color), red, green, blue);
    }

    public static boolean isRtl(Context context) {
        return hasJellyBeanMR1() && context.getResources().getConfiguration().getLayoutDirection() == 1;
    }

    public static boolean isGPSAvailable(Context context){
        return ConnectionResult.SUCCESS == GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }

    public static long getMasterDataCacheTime() {
        return 1440*30;
    }

    public static String toTitleCase(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }

    public static Spanned getFromHtml(String text){
        if(Utils.hasNougat()){
            return Html.fromHtml(text, FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }

    public static void showLicenseDialog(Context context) {
        new LicensesDialog.Builder(context)
                .setNotices(R.raw.notices)
                .setTitle(R.string.licenses)
                .setIncludeOwnLicense(true)
                .build()
                .showAppCompat();
    }

    public static void showReason(final Activity activity){
        new AlertDialog.Builder(activity,
                R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.paid_reason)
                .setMessage(R.string.paid_reason_description)
                .setPositiveButton("I'll Subscribe", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle bundle = new Bundle();
                        bundle.putString("source", "reason");
                        bundle.putString("type", "monthly");
                        AnalyticsManager.logEvent("subscribe", bundle);
                        if (FirebaseHelper.isLoggedIn()) {
                            App.getInstance().subscribe(activity,
                                    SUBSCRIPTION_MONTHLY_ID);
                        } else {
                            openLoginActivity(activity);
                        }
                    }
                })
                .setNegativeButton("Got It", null)
                .show();
    }

    public static void openLoginActivity(Activity activity){
        if(!FirebaseHelper.isLoggedIn()) {
            Intent login = new Intent(activity, LoginActivity.class);
            activity.startActivityForResult(login, MainActivity.LOGIN);
        }
    }

    public static String getDomainName(String url) {
        String hostname = null;
        try {
            URI uri = new URI(url);
            hostname = uri.getHost();
            if (hostname != null) {
                return hostname.startsWith("www.") ? hostname.substring(4) : hostname;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return hostname;
    }

    public static final Spannable getColoredString(Context context, CharSequence text, int color) {
        Spannable spannable = new SpannableString(text);
        spannable.setSpan(new ForegroundColorSpan(color), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static int themeAttributeToColor(Context context, int themeAttributeId) {
        TypedValue outValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        boolean wasResolved =
                theme.resolveAttribute(
                        themeAttributeId, outValue, true);
        if (wasResolved) {
            return outValue.resourceId == 0
                    ? outValue.data
                    : ContextCompat.getColor(
                    context, outValue.resourceId);
        } else {
            // fallback colour handling
            return ContextCompat.getColor(context, android.R.color.white);
        }
    }

    public static void changeThemeStyle(AppCompatDelegate delegate) {
        int nightMode = Integer.valueOf(SettingsActivity.getThemeStyle());
        delegate.setLocalNightMode(nightMode);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    public static void setActivityThemeStyle(AppCompatDelegate delegate) {
        int nightMode = Integer.valueOf(SettingsActivity.getThemeStyle());
        delegate.setLocalNightMode(nightMode);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    public static String cleanedCoinSymbol(String symbol){
        if(TextUtils.isEmpty(symbol)){
            return symbol;
        }
        return symbol.replace("*", "");
    }
}
