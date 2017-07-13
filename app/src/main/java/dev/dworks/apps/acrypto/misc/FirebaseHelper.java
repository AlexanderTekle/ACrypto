package dev.dworks.apps.acrypto.misc;

import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import dev.dworks.apps.acrypto.entity.User;

import static dev.dworks.apps.acrypto.App.APP_VERSION;
import static dev.dworks.apps.acrypto.entity.User.USERS;

/**
 * Created by HaKr on 16/05/17.
 */

public class FirebaseHelper {

    public static void signInAnonymously(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(null == currentUser) {
            mAuth.signInAnonymously();
        }
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

    public static String getCurrentUserId(){
        if(null != getCurrentUser()) {
            return getCurrentUser().getUid();
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
                firebaseUser.getUid(),
                firebaseUser.getPhotoUrl() == null ? "" : firebaseUser.getPhotoUrl().toString()
        );

        String photoUrl = firebaseUser.getPhotoUrl() == null ? "" : firebaseUser.getPhotoUrl().toString();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("displayName", firebaseUser.getDisplayName());
        childUpdates.put("email", firebaseUser.getEmail());
        childUpdates.put("uid", firebaseUser.getUid());
        childUpdates.put("photoUrl", photoUrl);
        childUpdates.put("appVersion", APP_VERSION);

        String instanceId = FirebaseInstanceId.getInstance().getToken();
        if (instanceId != null) {
            childUpdates.put("instanceId", instanceId);
        }

        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(firebaseUser.getUid()).updateChildren(childUpdates);

        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(firebaseUser.getUid())
                .child("createdAt").setValue(ServerValue.TIMESTAMP);



    }

    public static void updateUserSubscription(String productId, TransactionDetails details) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(!isLoggedIn()){
            return;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("subscriptionStatus", 1);
        childUpdates.put("subscriptionId", productId);
        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(firebaseUser.getUid())
                .updateChildren(childUpdates);
    }

    public static void updateUserAppVersion(String version) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(!isLoggedIn()){
            return;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("appVersion", version);
        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(firebaseUser.getUid())
                .updateChildren(childUpdates);
    }

    public static void updateUserSubscription(boolean active) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(!isLoggedIn()){
            return;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("subscriptionStatus", active ? 1 : 0);
        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(firebaseUser.getUid())
                .updateChildren(childUpdates);
    }
}
