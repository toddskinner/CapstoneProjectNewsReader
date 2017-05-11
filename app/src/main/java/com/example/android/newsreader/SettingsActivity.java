package com.example.android.newsreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.submitButton)
    Button submitButton;

    @BindView(R.id.adView)
    AdView mAdView;

    private static int mPrefIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        submitButton.setOnClickListener(this);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        Tracker tracker = (((MyApplication) getApplication()).getTracker());
        tracker.setScreenName(getResources().getString(R.string.settings_screen));
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black, null);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    public static class ArticlePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference articleSource = findPreference(getString(R.string.settings_source_key));
            bindPreferenceSummaryToValue(articleSource);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPref = (ListPreference) preference;
                int prefIndex = listPref.findIndexOfValue(stringValue);
                mPrefIndex = prefIndex;
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPref.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference){
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }

    @Override
    public void onClick(View view) {
        if(mPrefIndex == 6){
            Intent savedArticlesIntent = new Intent(this, SavedArticlesActivity.class);
            startActivity(savedArticlesIntent);
        } else {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent backIntent = new Intent(this, MainActivity.class);
        startActivity(backIntent);
    }
}
