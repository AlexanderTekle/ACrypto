package dev.dworks.apps.acrypto.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import dev.dworks.apps.acrypto.R;

import static dev.dworks.apps.acrypto.MainActivity.REQUEST_INVITE;

/**
 * Created by HaKr on 07-Jul-17.
 */

public class ShareUtils {

    private void getInvite(Activity activity){
        FirebaseDynamicLinks.getInstance().getDynamicLink(activity.getIntent())
                .addOnSuccessListener(activity, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        if (data == null) {
                            return;
                        }
                        Uri deepLink = data.getLink();
                        FirebaseAppInvite invite = FirebaseAppInvite.getInvitation(data);
                        if (invite != null) {
                            String invitationId = invite.getInvitationId();
                        }

                    }
                });
    }
    private void sendInvite(Activity activity) {
        Intent intent = new AppInviteInvitation.IntentBuilder(activity.getString(R.string.invitation_title))
                .setMessage(activity.getString(R.string.invitation_message))
                //.setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                //.setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(activity.getString(R.string.invitation_cta))
                .build();
        activity.startActivityForResult(intent, REQUEST_INVITE);
    }
}
