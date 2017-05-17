package dev.dworks.apps.acrypto.misc;

import android.content.Context;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.utils.PreferenceUtils;

/**
 * Created by HaKr on 26/07/15.
 */
public class SignInPreference {
    public static final String SKIP_SIGN_IN = "skip_sign_in";
    public static final String SIGNED_IN = "signed_in";
    public static final String AUTH_TOKEN = "auth_token";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_DOB = "user_dob";
    public static final String USER_GENDER = "user_gender";
    public static final String USER_IMAGE_URL = "user_image_url";

    public static final String CLIENT_ID = "client_id";


    public static boolean skipSignIn() {
        return PreferenceUtils.getBooleanPrefs(App.getInstance().getBaseContext(), SKIP_SIGN_IN);
    }

    public static void setSkipSignIn(boolean value) {
        PreferenceUtils.set(App.getInstance().getBaseContext(), SKIP_SIGN_IN, value);
    }

    public static boolean isSignedIn() {
        return PreferenceUtils.getBooleanPrefs(App.getInstance().getBaseContext(), SIGNED_IN);
    }

    public static void setSignedIn(boolean value) {
        PreferenceUtils.set(App.getInstance().getBaseContext(), SIGNED_IN, value);
    }

    public static String getAuthToken() {
        return PreferenceUtils.getStringPrefs(App.getInstance().getBaseContext(), AUTH_TOKEN);
    }

    public static void setAuthToken(String value) {
        PreferenceUtils.set(App.getInstance().getBaseContext(), AUTH_TOKEN, value);
    }

/*    public static void setUser(User user) {
        Context context = App.getInstance().getBaseContext();
        PreferenceUtils.set(context, USER_ID, user.id);
        PreferenceUtils.set(context, USER_NAME, user.name);
        PreferenceUtils.set(context, USER_EMAIL, user.email);
        PreferenceUtils.set(context, USER_DOB, user.date_of_birth);
        PreferenceUtils.set(context, USER_GENDER, user.gender);
        PreferenceUtils.set(context, USER_IMAGE_URL, user.image_url);
    }*/

    public static int getUserId() {
        return PreferenceUtils.getIntegerPrefs(App.getInstance().getBaseContext(), USER_ID);
    }

    public static String getUserName() {
        return PreferenceUtils.getStringPrefs(App.getInstance().getBaseContext(), USER_NAME);
    }

    public static String getUserEmail() {
        return PreferenceUtils.getStringPrefs(App.getInstance().getBaseContext(), USER_EMAIL);
    }

    public static String getUserDOB() {
        return PreferenceUtils.getStringPrefs(App.getInstance().getBaseContext(), USER_DOB);
    }

    public static String getUserGender() {
        return PreferenceUtils.getStringPrefs(App.getInstance().getBaseContext(), USER_GENDER);
    }

    public static String getUserImageUrl() {
        return PreferenceUtils.getStringPrefs(App.getInstance().getBaseContext(), USER_IMAGE_URL);
    }

    public static int getClientId() {
        return PreferenceUtils.getIntegerPrefs(App.getInstance().getBaseContext(), CLIENT_ID);
    }

    public static void setClientId(int value) {
        PreferenceUtils.set(App.getInstance().getBaseContext(), CLIENT_ID, value);
    }

    public static void clearPreferences() {
        Context context = App.getInstance().getBaseContext();
        PreferenceUtils.set(context, USER_ID, "");
        PreferenceUtils.set(context, USER_NAME, "");
        PreferenceUtils.set(context, USER_EMAIL, "");
        PreferenceUtils.set(context, USER_DOB, "");
        PreferenceUtils.set(context, USER_GENDER, "");
        PreferenceUtils.set(context, USER_IMAGE_URL, "");

        PreferenceUtils.set(context, AUTH_TOKEN, "");
        PreferenceUtils.set(context, CLIENT_ID, "");
    }
}
