package dev.dworks.apps.acrypto.misc;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by HaKr on 16/05/17.
 */

public class AuthenticationHelper {

    public static void signInAnonymously(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(null == currentUser) {
            mAuth.signInAnonymously();
        }
    }
}
