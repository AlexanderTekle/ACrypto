package dev.dworks.apps.acrypto.misc;

import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import junit.framework.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.entity.User;
import dev.dworks.apps.acrypto.settings.SettingsActivity;
import dev.dworks.apps.acrypto.utils.PreferenceUtils;
import dev.dworks.apps.acrypto.utils.TimeUtils;

import static dev.dworks.apps.acrypto.App.APP_VERSION;
import static dev.dworks.apps.acrypto.entity.User.USERS;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getNewsAlertStatus;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.getUserCurrencyFrom;
import static dev.dworks.apps.acrypto.utils.TimeUtils.MILLIS_IN_A_DAY;

/**
 * Created by HaKr on 16/05/17.
 */

public class FirebaseHelper {
    private static final String USER_LAST_UPDATED = "user_last_updated";

    public static FirebaseUser signInAnonymously(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(null == currentUser) {
            mAuth.signInAnonymously();
        }
        return currentUser;
    }

    public static FirebaseAuth getFirebaseAuth(){
        return FirebaseAuth.getInstance();
    }

    public static DatabaseReference getFirebaseDatabaseReference(){
       return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getFirebaseDatabaseReference(String path){
        return FirebaseDatabase.getInstance().getReference(path);
    }

    public static FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getCurrentUid(){
        if(null != getCurrentUser()) {
            return getCurrentUser().getUid().replace(".","-");
        } else {
            return "";
        }
    }

    public static boolean isLoggedIn(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return null != user && !user.isAnonymous();
    }

    public static boolean isAnonymous(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return null != user && user.isAnonymous();
    }

    public static void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    public static void updateUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(!isLoggedIn()){
            return;
        }
        User user = new User(
                firebaseUser.getDisplayName(),
                firebaseUser.getEmail(),
                FirebaseHelper.getCurrentUid(),
                firebaseUser.getPhotoUrl() == null ? "" : firebaseUser.getPhotoUrl().toString()
        );

        String uid = FirebaseHelper.getCurrentUid();
        String photoUrl = firebaseUser.getPhotoUrl() == null ? "" : firebaseUser.getPhotoUrl().toString();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("displayName", firebaseUser.getDisplayName());
        childUpdates.put("email", firebaseUser.getEmail());
        childUpdates.put("uid", uid);
        childUpdates.put("photoUrl", photoUrl);
        childUpdates.put("appVersion", APP_VERSION);

        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(uid).updateChildren(childUpdates);

        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(uid)
                .child("createdAt").setValue(ServerValue.TIMESTAMP);

        App.getInstance().updateInstanceId();

        updateUserSettings();
    }

    private static void updateUserSettings() {
        FirebaseHelper.getFirebaseDatabaseReference("users/"+FirebaseHelper.getCurrentUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> childUpdates = new HashMap<>();
                        User user = dataSnapshot.getValue(User.class);
                        if(TextUtils.isEmpty(user.nativeCurrency)){
                            childUpdates.put("nativeCurrency", getUserCurrencyFrom());
                        } else {
                            SettingsActivity.setUserCurrencyFrom(user.nativeCurrency);
                        }

                        if(user.newsAlertStatus == -1){
                            childUpdates.put("newsAlertStatus", getNewsAlertStatus() ? 1 : 0);
                        } else {
                            boolean status = user.newsAlertStatus == 1;
                            SettingsActivity.setNewsAlertStatus(status);
                            App.getInstance().updateNewsSubscription(status);
                        }

                        if(childUpdates.size() != 0){
                            FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                                    .child(getCurrentUid()).updateChildren(childUpdates);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static void updateUserSubscription(String productId, String orderId, long orderTime) {
        if(!isLoggedIn()){
            return;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("subscriptionStatus", 1);
        childUpdates.put("subscriptionId", productId);
        childUpdates.put("orderId", orderId);
        childUpdates.put("orderedAt", orderTime);
        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(FirebaseHelper.getCurrentUid())
                .updateChildren(childUpdates);
    }

    public static void updateUserAppVersion(String version) {
        if(!isLoggedIn()){
            return;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("appVersion", version);
        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(FirebaseHelper.getCurrentUid())
                .updateChildren(childUpdates);
    }

    public static void updateNewsAlertStatus(boolean enable) {
        if(!isLoggedIn()){
            return;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("newsAlertStatus", enable ? 1 : 0);
        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(FirebaseHelper.getCurrentUid())
                .updateChildren(childUpdates);
    }

    public static void updateUserSubscriptionStatus(boolean active) {
        if(!isLoggedIn()){
            return;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("subscriptionStatus", active ? 1 : 0);
        AnalyticsManager.setProperty("IsSubscribed", String.valueOf(active));
        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(FirebaseHelper.getCurrentUid())
                .updateChildren(childUpdates);
    }

    public static void updateNativeCurrency(String currency) {
        if(!isLoggedIn()){
            return;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("nativeCurrency", currency);
        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(FirebaseHelper.getCurrentUid())
                .updateChildren(childUpdates);
    }

    public static void updateInstanceId(String registrationId) {
        if(!isLoggedIn()){
            return;
        }
        FirebaseDatabase.getInstance().getReference()
                .child(USERS)
                .child(FirebaseHelper.getCurrentUid())
                .child("instanceId")
                .setValue(registrationId);
    }

    public static void checkInstanceIdValidity(){

        long userLastUpdated = PreferenceUtils.getLongPrefs(USER_LAST_UPDATED, -1);
        if(userLastUpdated != -1){
            long now = System.currentTimeMillis();
            long dayOrd = userLastUpdated / MILLIS_IN_A_DAY;
            long nowOrd = now / MILLIS_IN_A_DAY;
            if (dayOrd == nowOrd) {
                return;
            }
        }

        if(!isLoggedIn()){
            return;
        }
        FirebaseHelper.getFirebaseDatabaseReference("users/"+FirebaseHelper.getCurrentUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(TextUtils.isEmpty(user.instanceId)){
                    App.getInstance().updateInstanceId();
                }
                PreferenceUtils.set(USER_LAST_UPDATED, System.currentTimeMillis());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
