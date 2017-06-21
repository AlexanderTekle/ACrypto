package dev.dworks.apps.acrypto.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.text.TextUtilsCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.request.StringRequest;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.BuildConfig;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.Symbols;
import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.AppFeedback;
import dev.dworks.apps.acrypto.misc.RoundedNumberFormat;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by HaKr on 20-Sep-14.
 */
public class Utils {

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {}

    // cache
    public static final int IMAGE_SIZE_BIG = 100;
    public static final int IMAGE_SIZE = 50;
    public static final int SCALE_DOWN_SIZE = 1024;
    public static final int SCALE_DOWN_SIZE_SMALL = 512;
    public static final String IMAGE_CACHE_DIR = "thumbs";
    public static final String IMAGE_BG_CACHE_DIR = "bgs";

    static final String TAG = "Utils";

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
                    .setActionTextColor(view.getResources().getColor(R.color.colorPrimary));
        }
        snackbar.show();
    }

    public static void showRetrySnackBar(Activity activity, String text, View.OnClickListener listener){
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_INDEFINITE);
        if (null != listener) {
            snackbar.setAction("RETRY", listener)
                    .setActionTextColor(activity.getResources().getColor(R.color.colorPrimary));
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
        Snackbar.make(activity.findViewById(android.R.id.content), text, duration).setAction(action, listener).show();
    }

    public static void showNoInternetSnackBar(Activity activity, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content),
                "Can't connect to Internet", Snackbar.LENGTH_INDEFINITE);

        if (null != listener) {
            snackbar.setAction("RETRY", listener)
                    .setActionTextColor(activity.getResources().getColor(R.color.colorPrimary));
        }
        snackbar.show();
    }

    public static void showNoInternetSnackBar(View view, View.OnClickListener listener){
        Snackbar snackbar = Snackbar.make(view, "Can't connect to Internet", Snackbar.LENGTH_INDEFINITE);
        if (null != listener) {
            snackbar.setAction("RETRY", listener)
                    .setActionTextColor(view.getResources().getColor(R.color.colorPrimary));
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
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
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
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        try {
            customTabsIntent.launchUrl(activity, Uri.parse(url));
        } catch (ActivityNotFoundException ex) {
            showSnackBar(activity, "Cant Open");
        }
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

    public static void setPriceValue(MoneyTextView textView, double value, String symbol){
        textView.setDecimalFormat(getMoneyFormat(value, symbol));
        textView.setAmount((float) Math.abs(value));
        textView.setSymbol(symbol);
    }

    public static void setDateTimeValue(TextView textView, long timeInMillis){
        textView.setText(TimeUtils.getFormattedDateTime(timeInMillis));
    }

    public static DecimalFormat getMoneyFormat(boolean high){
        String precisionFormat = high ? "###,##0.###" : "###,##0.##";
        DecimalFormat decimalFormat = new DecimalFormat(precisionFormat);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        return decimalFormat;
    }

    public static DecimalFormat getMoneyFormat(double value, String symbol){
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

    public static void openFeedback(Activity activity){
        ShareCompat.IntentBuilder
                .from(activity)
                .setEmailTo(new String[]{"hakr@dworks.in"})
                .setSubject("ACrypto Feedback")
                .setType("text/email")
                .setChooserTitle("Send Feedback")
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
        if(value == 0){
            return " - ";
        }
        return String.format("%.2f", Math.abs(value)) + "% " + (value > 0 ? "↑" : "↓");
    }

    public static String getDisplayPercentageRounded(double valueOne, double valueTwo){
        valueOne = valueOne == 0 ? 1 : valueOne;
        double value = ((valueTwo - valueOne)/valueOne) * 100;
        if(value == 0){
            return " - ";
        }
        return String.valueOf(Math.round(Math.abs(value))) +  "% " + (value > 0 ? "▲" : "▼");
    }

    public static String getDisplayPercentage(double valueOne, double valueTwo){
        valueOne = valueOne == 0 ? 1 : valueOne;
        double value = ((valueTwo - valueOne)/valueOne) * 100;
        if(value == 0){
            return " - ";
        }
        return String.format("%.2f", Math.abs(value)) +  "% " + (value > 0 ? "▲" : "▼");
    }

    public static String getCurrencySymbol(String currencyTo){
        final Symbols symbols = App.getInstance().getSymbols();
        String currencyToSymbol = "";
        try {
            currencyToSymbol = symbols.currencies.get(currencyTo);
            if(TextUtils.isEmpty(currencyToSymbol)) {
                currencyToSymbol = symbols.coins.get(currencyTo);
            }
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

    public static String roundDouble(String value){
        return roundDouble(Double.valueOf(value));
    }

    public static String roundDouble(Double value){
        RoundedNumberFormat roundedNumberFormat = new RoundedNumberFormat();
        return roundedNumberFormat.format(value);
    }

    public static String getFormattedNumber(double value, String symbol){
        DecimalFormat decimalFormat = getMoneyFormat(value, symbol);
        return decimalFormat.format(value);
    }

    public static void showAppFeedback(Activity activity){
        AppFeedback.with(activity, R.id.container_rate).listener(new AppFeedback.OnShowListener() {
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
}
