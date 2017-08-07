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

package dev.dworks.apps.acrypto

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView

import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import cat.ereza.customactivityoncrash.config.CaocConfig
import dev.dworks.apps.acrypto.common.DialogFragment

/**
 * Created by HaKr on 16/05/17.
 */

class ErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_error)

        //Close/restart button logic:
        //If a class if set, use restart.
        //Else, use close and just finish the app.
        //It is recommended that you follow this logic if implementing a custom error activity.
        val restartButton = findViewById<View>(R.id.customactivityoncrash_error_activity_restart_button) as Button

        val config = CustomActivityOnCrash.getConfigFromIntent(intent)

        restartButton.setText(R.string.customactivityoncrash_error_activity_restart_app)
        restartButton.setOnClickListener { CustomActivityOnCrash.restartApplication(this@ErrorActivity, config) }

        if (config.isShowRestartButton && config.restartActivityClass != null) {
            restartButton.setText(R.string.customactivityoncrash_error_activity_restart_app)
            restartButton.setOnClickListener { CustomActivityOnCrash.restartApplication(this@ErrorActivity, config) }
        } else {
            restartButton.setOnClickListener { CustomActivityOnCrash.closeApplication(this@ErrorActivity, config) }
        }

        val moreInfoButton = findViewById<View>(R.id.customactivityoncrash_error_activity_more_info_button) as Button

        if (config.isShowErrorDetails) {

            moreInfoButton.setOnClickListener {
                //We retrieve all the error data and show it

                val builder = AlertDialog.Builder(this@ErrorActivity)
                        .setTitle(R.string.customactivityoncrash_error_activity_error_details_title)
                        .setMessage(CustomActivityOnCrash.getAllErrorDetailsFromIntent(this@ErrorActivity, intent))
                        .setPositiveButton(R.string.customactivityoncrash_error_activity_error_details_close, null)

                val dialog = DialogFragment.showThemedDialog(builder)

                val textView = dialog.findViewById<View>(android.R.id.message) as TextView
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.customactivityoncrash_error_activity_error_details_text_size))
            }
        } else {
            moreInfoButton.visibility = View.GONE
        }
    }
}