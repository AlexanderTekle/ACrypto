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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.utils.Utils.openCustomTabUrl;
import static dev.dworks.apps.acrypto.utils.Utils.openFeedback;
import static dev.dworks.apps.acrypto.utils.Utils.openPlaystore;
import static dev.dworks.apps.acrypto.utils.Utils.showLicenseDialog;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

	public static final String TAG = "About";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
		int color = ContextCompat.getColor(this, R.color.colorPrimary);
		mToolbar.setBackgroundColor(color);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(TAG);
		initControls();
	}

	private void initControls() {

		TextView appVersion = (TextView)findViewById(R.id.app_version);
		appVersion.setText( BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ") "
				+ (BuildConfig.DEBUG ? " Debug" : ""));

		findViewById(R.id.rating).setOnClickListener(this);
		findViewById(R.id.support).setOnClickListener(this);
		findViewById(R.id.share).setOnClickListener(this);
		findViewById(R.id.feedback).setOnClickListener(this);
		findViewById(R.id.provider).setOnClickListener(this);
		findViewById(R.id.licenses).setOnClickListener(this);
		findViewById(R.id.twitter).setOnClickListener(this);
		findViewById(R.id.gplus).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsManager.setCurrentScreen(this, TAG);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void startActivity(Intent intent) {
        if(Utils.isIntentAvailable(this, intent)) {
            super.startActivity(intent);
        }
    }

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.gplus:
				startActivity(new Intent("android.intent.action.VIEW",
						Uri.parse("https://plus.google.com/communities/104409384894430471343")));
				AnalyticsManager.logEvent("open_gplus");
				break;
			case R.id.twitter:
				startActivity(new Intent("android.intent.action.VIEW",
						Uri.parse("https://twitter.com/1HaKr")));
				AnalyticsManager.logEvent("open_twitter");
				break;
			case R.id.feedback:
				openFeedback(this);
				AnalyticsManager.logEvent("open_feedback");
				break;
			case R.id.rating:
				openPlaystore(this);
				AnalyticsManager.logEvent("open_rating");
				break;
			case R.id.support:
				Utils.showReason(this);
				AnalyticsManager.logEvent("open_reason");
				break;
			case R.id.share:

				String shareText = "I found this crypto currency trends app very useful. Give it a try. "
						+ Utils.getAppShareUri().toString();
				ShareCompat.IntentBuilder
						.from(this)
						.setText(shareText)
						.setType("text/plain")
						.setChooserTitle("Share ACrypto")
						.startChooser();
				AnalyticsManager.logEvent("open_share");
				break;
			case R.id.provider:
				openCustomTabUrl(this, UrlConstant.BASE_URL);
				AnalyticsManager.logEvent("open_provider");
				break;

			case R.id.licenses:
				showLicenseDialog(this);
				AnalyticsManager.logEvent("open_licenses");
				break;

		}
	}
}