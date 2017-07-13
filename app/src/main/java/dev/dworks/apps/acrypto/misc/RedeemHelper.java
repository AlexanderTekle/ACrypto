package dev.dworks.apps.acrypto.misc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import java.net.URLEncoder;

import dev.dworks.apps.acrypto.R;

public class RedeemHelper {

    public static void showRedeemDialog(final Context context) {
        final EditText editText = new EditText(context);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        editText.setSingleLine(true);
        editText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.redeem)
                .setMessage(R.string.redeem_message)
                .setIcon(R.drawable.ic_bonus)
                .setView(editText)
                .setPositiveButton(R.string.redeem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            String code = editText.getText().toString();
                            String url = "https://play.google.com/redeem?code=" + URLEncoder.encode(code, "UTF-8");
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Error
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}