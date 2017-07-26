package dev.dworks.apps.acrypto.common;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.Button;

import dev.dworks.apps.acrypto.utils.Utils;

/**
 * Created by HaKr on 12/06/16.
 */

public class DialogFragment extends AppCompatDialogFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!getShowsDialog()){
            return;
        }
        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                tintButtons(getDialog());
            }
        });
    }

    public static void tintButtons(Dialog dialog){
        Utils.tintButton(getButton(dialog, DialogInterface.BUTTON_POSITIVE));
        Utils.tintButton(getButton(dialog, DialogInterface.BUTTON_NEGATIVE));
        Utils.tintButton(getButton(dialog, DialogInterface.BUTTON_NEUTRAL));
    }

    public static Button getButton(Dialog dialog, int which){
        return ((AlertDialog)dialog).getButton(which);
    }

    public static Dialog showThemedDialog(AlertDialog.Builder builder){
        Dialog dialog = builder.create();
        dialog.show();
        tintButtons(dialog);
        return dialog;
    }

}