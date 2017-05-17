package dev.dworks.apps.acrypto.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
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

import org.fabiomsr.moneytextview.MoneyTextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.BuildConfig;
import dev.dworks.apps.acrypto.R;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by HaKr on 20-Sep-14.
 */
public class Utils {

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {}

    public static final int REQUEST_CHECK_SETTINGS = 0x1;

    // cache
    public static final int IMAGE_SIZE_BIG = 100;
    public static final int IMAGE_SIZE = 50;
    public static final int SCALE_DOWN_SIZE = 1024;
    public static final int SCALE_DOWN_SIZE_SMALL = 512;
    public static final String IMAGE_CACHE_DIR = "thumbs";
    public static final String IMAGE_BG_CACHE_DIR = "bgs";

    public static final String API_URL ="";// BuildConfig.BASE_API_URL;
    static final String TAG = "Utils";
    public static final String BUNDLE_PLACES = "bundle_places";
    public static final String BUNDLE_RESTAURANT = "bundle_restaurant";
    public static final String BUNDLE_MENU = "bundle_menu";
    public static final String BUNDLE_MENU_ITEMS = "bundle_menu_items";
    public static final String BUNDLE_FAVORITE_ITEMS = "bundle_favourite_items";
    public static final String BUNDLE_ITEM = "bundle_item";
    public static final String BUNDLE_LOCATION = "bundle_location";
    public static final String BUNDLE_ADDRESS = "bundle_address";

    public static final String GOOGLE_PLACES_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    public static final String GOOGLE_PHOTOS_URL = "https://maps.googleapis.com/maps/api/place/photo";
    public static final String GOOGLE_PLACES_API_KEY = "AIzaSyArBmLVB_OqHZAiQo7zoSzbnAiDjkPZ03o";

    //Restaurant
    public static final String BUNDLE_RESTAURANT_ID = "bundle_restaurant_id";
    public static final String BUNDLE_RESTAURANT_NAME = "bundle_restaurant_name";
    public static final String BUNDLE_RESTAURANT_URL = "bundle_restaurant_url";
    public static final String BUNDLE_RESTAURANT_CUISINES = "bundle_restaurant_cuisines";
    public static final String BUNDLE_SEARCH_QUERY = "bundle_search_query";
    public static final String BUNDLE_SEARCH_ITEM_NAME = "bundle_search_item_name";

    //Menut Item
    public static final String BUNDLE_ITEM_ID = "bundle_item_id";
    public static final String BUNDLE_ITEM_NAME = "bundle_item_name";
    public static final String BUNDLE_ITEM_URL = "bundle_item_url";

    public static final String BUNDLE_IMAGE_PATH = "bundle_image_path";

    public static final String PARAM_KEY = "thuglife";

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
        String topic = "SHIFOO-DROID";
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

    public static void openCustomTabUrl(Context context, String url){
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        builder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.colorAccent));
        builder.setCloseButtonIcon(getVector2Bitmap(context, R.drawable.ic_back));
        //builder.setCloseButtonIcon(BitmapFactory.decodeResource(
          //      context.getResources(), R.drawable.abc_ic_ab_back_material));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        customTabsIntent.launchUrl(context, Uri.parse(url));
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

    public static int getValueDifferenceColor(double value){
        int colorRes = R.color.accent_white;
        double absValue = Math.abs(value);
        if(absValue > 0.00){
            colorRes = R.color.accent_teal;
        }
        else if(absValue < 0){
            colorRes = R.color.accent_red;
        }
        return colorRes;
    }

    public static void setPriceValue(MoneyTextView textView, double value, String symbol){
        textView.setDecimalFormat(getMoneyFormat(Math.abs(value) < 0));
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

}
