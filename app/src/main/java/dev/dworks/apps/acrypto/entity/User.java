package dev.dworks.apps.acrypto.entity;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

public class User {

    public static final String USERS = "users";

    public String displayName;
    public String email;
    public String uid;
    public String photoUrl;
    public String instanceId;
    public int subscriptionStatus;
    public String subscriptionId;
    public String orderId;
    public String appVersion;
    public Object createdAt;
    public long orderedAt;
    public String nativeCurrency;
    public int newsAlertStatus = -1;

    public User() {
    }

    public User(String displayName, String email, String uid, String photoUrl) {
        this.displayName = displayName;
        this.email = email;
        this.uid = uid;
        this.photoUrl = photoUrl;
        this.createdAt = ServerValue.TIMESTAMP;
    }

    @Exclude
    public long getCreatedAt() {
        return (long)createdAt;
    }
}
