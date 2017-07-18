package dev.dworks.apps.acrypto.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import dev.dworks.apps.acrypto.misc.FirebaseHelper;

public class InstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String instanceId = FirebaseInstanceId.getInstance().getToken();
        FirebaseHelper.updateInstanceId(instanceId);
    }
}
