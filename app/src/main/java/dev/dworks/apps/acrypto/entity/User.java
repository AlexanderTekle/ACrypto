package dev.dworks.apps.acrypto.entity;

public class User {

    public static final String USERS = "users";

    public String displayName;
    public String email;
    public String uid;
    public String photoUrl;
    public String instanceId;
    public boolean subscriptionStatus;
    public String subscriptionId;

    public User() {
    }

    public User(String displayName, String email, String uid, String photoUrl) {
        this.displayName = displayName;
        this.email = email;
        this.uid = uid;
        this.photoUrl = photoUrl;
    }

}
