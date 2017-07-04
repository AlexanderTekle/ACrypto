package dev.dworks.apps.acrypto.misc;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import dev.dworks.apps.acrypto.entity.User;

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

    public static FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
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

        FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                .child(firebaseUser.getUid()).setValue(user);

        String instanceId = FirebaseInstanceId.getInstance().getToken();
        if (instanceId != null) {
            FirebaseHelper.getFirebaseDatabaseReference().child(USERS)
                    .child(firebaseUser.getUid())
                    .child("instanceId")
                    .setValue(instanceId);
        }
    }

    public static void startMasterDataSync() {
        FirebaseHelper.syncData("master/coins", true);
        FirebaseHelper.syncData("master/currency", true);
        FirebaseHelper.syncData("master/symbols", true);
        FirebaseHelper.syncData("master/coins_list", true);
        FirebaseHelper.syncData("master/coins_ignore", true);
        FirebaseHelper.syncData("master/coin_details", true);
    }

    public static void stopMasterDataSync() {
        FirebaseHelper.syncData("master/coins", false);
        FirebaseHelper.syncData("master/currency", false);
        FirebaseHelper.syncData("master/symbols", false);
        FirebaseHelper.syncData("master/coins_list", false);
        FirebaseHelper.syncData("master/coins_ignore", false);
        FirebaseHelper.syncData("master/coin_details", false);
    }

    public static void syncData(String path, boolean sync){
        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference(path);
        scoresRef.keepSynced(sync);
    }
}
