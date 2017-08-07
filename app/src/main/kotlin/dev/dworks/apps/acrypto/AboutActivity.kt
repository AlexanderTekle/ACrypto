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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView

import dev.dworks.apps.acrypto.misc.AnalyticsManager
import dev.dworks.apps.acrypto.misc.UrlConstant
import dev.dworks.apps.acrypto.utils.Utils

import dev.dworks.apps.acrypto.utils.Utils.openCustomTabUrl
import dev.dworks.apps.acrypto.utils.Utils.openFeedback
import dev.dworks.apps.acrypto.utils.Utils.openPlaystore
import dev.dworks.apps.acrypto.utils.Utils.showLicenseDialog
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.layout_about_app.*
import kotlinx.android.synthetic.main.layout_about_credits.*
import kotlinx.android.synthetic.main.layout_about_support.*

class AboutActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        toolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title)
        val color = ContextCompat.getColor(this, R.color.colorPrimary)
        toolbar.setBackgroundColor(color)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = TAG
        initControls()
    }

    private fun initControls() {

        app_version.text = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ") " +
                if (BuildConfig.DEBUG) " Debug" else ""

        rating.setOnClickListener(this)
        support.setOnClickListener(this)
        share.setOnClickListener(this)
        feedback.setOnClickListener(this)
        provider.setOnClickListener(this)
        licenses.setOnClickListener(this)
        twitter.setOnClickListener(this)
        gplus.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsManager.setCurrentScreen(this, TAG)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun startActivity(intent: Intent) {
        if (Utils.isIntentAvailable(this, intent)) {
            super.startActivity(intent)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.gplus -> {
                startActivity(Intent("android.intent.action.VIEW",
                        Uri.parse("https://plus.google.com/communities/104409384894430471343")))
                AnalyticsManager.logEvent("open_gplus")
            }
            R.id.twitter -> {
                startActivity(Intent("android.intent.action.VIEW",
                        Uri.parse("https://twitter.com/1HaKr")))
                AnalyticsManager.logEvent("open_twitter")
            }
            R.id.feedback -> {
                openFeedback(this)
                AnalyticsManager.logEvent("open_feedback")
            }
            R.id.rating -> {
                openPlaystore(this)
                AnalyticsManager.logEvent("open_rating")
            }
            R.id.support -> {
                Utils.showReason(this)
                AnalyticsManager.logEvent("open_reason")
            }
            R.id.share -> {

                val shareText = "I found this crypto currency trends app very useful. Give it a try. " + Utils.getAppShareUri().toString()
                ShareCompat.IntentBuilder
                        .from(this)
                        .setText(shareText)
                        .setType("text/plain")
                        .setChooserTitle("Share ACrypto")
                        .startChooser()
                AnalyticsManager.logEvent("open_share")
            }
            R.id.provider -> {
                openCustomTabUrl(this, UrlConstant.BASE_URL)
                AnalyticsManager.logEvent("open_provider")
            }

            R.id.licenses -> {
                showLicenseDialog(this)
                AnalyticsManager.logEvent("open_licenses")
            }
        }
    }

    companion object {

        val TAG = "About"
    }
}