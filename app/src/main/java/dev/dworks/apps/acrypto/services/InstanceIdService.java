package dev.dworks.apps.acrypto.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static dev.dworks.apps.acrypto.entity.User.USERS;

public class InstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String instanceId = FirebaseInstanceId.getInstance().getToken();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child(USERS)
                    .child(firebaseUser.getUid())
                    .child("instanceId")
                    .setValue(instanceId);
        }
    }
}
