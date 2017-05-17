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
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import dev.dworks.apps.acrypto.misc.AnalyticsManager;
import dev.dworks.apps.acrypto.utils.ColorUtils;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.utils.Utils.openFeedback;
import static dev.dworks.apps.acrypto.utils.Utils.openPlaystore;

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
		getSupportActionBar().setTitle(null);

		initControls();
	}

	private void initControls() {

		int accentColor = ColorUtils.getTextColorForBackground(ContextCompat.getColor(this, R.color.colorPrimary));
		TextView logo = (TextView)findViewById(R.id.logo);
		logo.setTextColor(accentColor);
		String header = logo.getText() + " v" + BuildConfig.VERSION_NAME;
		logo.setText(header);

		TextView action_rate = (TextView)findViewById(R.id.action_rate);
		TextView action_support = (TextView)findViewById(R.id.action_support);
		TextView action_share = (TextView)findViewById(R.id.action_share);
		TextView action_feedback = (TextView)findViewById(R.id.action_feedback);

		action_rate.setOnClickListener(this);
		action_support.setOnClickListener(this);
		action_share.setOnClickListener(this);
		action_feedback.setOnClickListener(this);
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
/*			case R.id.action_github:
				startActivity(new Intent("android.intent.action.VIEW",
						Uri.parse("https://github.com/DWorkS")));
				break;
			case R.id.action_gplus:
				startActivity(new Intent("android.intent.action.VIEW",
						Uri.parse("https://plus.google.com/+HariKrishnaDulipudi")));
				break;
			case R.id.action_twitter:
				startActivity(new Intent("android.intent.action.VIEW",
						Uri.parse("https://twitter.com/1HaKr")));
				break;*/
			case R.id.action_feedback:
				openFeedback(this);
				break;
			case R.id.action_rate:
				openPlaystore(this);
				AnalyticsManager.logEvent("app_rate");
				break;
			case R.id.action_support:
				Intent intentMarketAll = new Intent("android.intent.action.VIEW");
				intentMarketAll.setData(Utils.getAppStoreUri());
				startActivity(intentMarketAll);
				AnalyticsManager.logEvent("app_love");
				break;
			case R.id.action_share:

				String shareText = "I found this crypto currency trends app very useful. Give it a try. "
						+ Utils.getAppShareUri().toString();
				ShareCompat.IntentBuilder
						.from(this)
						.setText(shareText)
						.setType("text/plain")
						.setChooserTitle("Share ACrypto")
						.startChooser();
				AnalyticsManager.logEvent("app_share");
				break;
		}
	}
}