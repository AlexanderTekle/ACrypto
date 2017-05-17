/*
 * Copyright (C) 2014 Hari Krishna Dulipudi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.dworks.apps.acrypto;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import dev.dworks.apps.acrypto.common.DialogFragment;

/**
 * Created by HaKr on 16/05/17.
 */

public final class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_error);

        //Close/restart button logic:
        //If a class if set, use restart.
        //Else, use close and just finish the app.
        //It is recommended that you follow this logic if implementing a custom error activity.
        Button restartButton = (Button) findViewById(R.id.customactivityoncrash_error_activity_restart_button);

        final Class<? extends Activity> restartActivityClass = CustomActivityOnCrash.getRestartActivityClassFromIntent(getIntent());

        if (restartActivityClass != null) {
            restartButton.setText(R.string.customactivityoncrash_error_activity_restart_app);
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ErrorActivity.this, restartActivityClass);
                    finish();
                    startActivity(intent);
                }
            });
        } else {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        Button moreInfoButton = (Button) findViewById(R.id.customactivityoncrash_error_activity_more_info_button);

        if (CustomActivityOnCrash.isShowErrorDetailsFromIntent(getIntent())) {

            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //We retrieve all the error data and show it

                    AlertDialog.Builder builder = new AlertDialog.Builder(ErrorActivity.this)
                            .setTitle(R.string.customactivityoncrash_error_activity_error_details_title)
                            .setMessage(CustomActivityOnCrash.getAllErrorDetailsFromIntent(ErrorActivity.this, getIntent()))
                            .setPositiveButton(R.string.customactivityoncrash_error_activity_error_details_close, null);

                    Dialog dialog = DialogFragment.showThemedDialog(builder);

                    TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.customactivityoncrash_error_activity_error_details_text_size));
                }
            });
        } else {
            moreInfoButton.setVisibility(View.GONE);
        }
    }
}