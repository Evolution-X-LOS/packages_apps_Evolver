/*
 * Copyright (C) 2019-2024 The Evolution X Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.settings.fragments.miscellaneous;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.android.SystemRestartUtils;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;

import org.evolution.settings.preferences.SystemPropertySwitchPreference;
import org.evolution.settings.utils.DeviceUtils;

@SearchIndexable
public class Spoofing extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "Spoofing";

    private static final String KEY_SYSTEM_WIDE_CATEGORY = "spoofing_system_wide_category";
    private static final String SYS_GOOGLE_SPOOF = "persist.sys.pphooks.enable";
    private static final String SYS_GAMEPROP_ENABLED = "persist.sys.gamehooks.enable";
    private static final String SYS_ENABLE_TENSOR_FEATURES = "persist.sys.features.tensor";

    private PreferenceCategory mSystemWideCategory;
    private SystemPropertySwitchPreference mGamePropsSpoof;
    private SystemPropertySwitchPreference mGoogleSpoof;
    private SystemPropertySwitchPreference mTensorFeaturesToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.spoofing);

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources resources = context.getResources();

        mSystemWideCategory = (PreferenceCategory) findPreference(KEY_SYSTEM_WIDE_CATEGORY);
        mGamePropsSpoof = (SystemPropertySwitchPreference) findPreference(SYS_GAMEPROP_ENABLED);
        mGoogleSpoof = (SystemPropertySwitchPreference) findPreference(SYS_GOOGLE_SPOOF);
        mTensorFeaturesToggle = (SystemPropertySwitchPreference) findPreference(SYS_ENABLE_TENSOR_FEATURES);

        String model = SystemProperties.get("ro.product.model");
        boolean isTensorDevice = model.matches("Pixel [6-9][a-zA-Z ]*");

        if (DeviceUtils.isCurrentlySupportedPixel()) {
            mGoogleSpoof.setDefaultValue(false);
            if (isMainlineTensorModel(model)) {
                mSystemWideCategory.removePreference(mGoogleSpoof);
            }
        }

        if (isTensorDevice) {
            mSystemWideCategory.removePreference(mTensorFeaturesToggle);
        }

        mGoogleSpoof.setOnPreferenceChangeListener(this);
        mGamePropsSpoof.setOnPreferenceChangeListener(this);
        mTensorFeaturesToggle.setOnPreferenceChangeListener(this);
    }

    private boolean isMainlineTensorModel(String model) {
        return model.matches("Pixel [8-9][a-zA-Z ]*");
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        if (preference == mGoogleSpoof
            || preference == mGamePropsSpoof) {
            SystemRestartUtils.showSystemRestartDialog(getContext());
            return true;
        }
        if (preference == mTensorFeaturesToggle) {
            boolean enabled = (Boolean) newValue;
            SystemProperties.set(SYS_ENABLE_TENSOR_FEATURES, enabled ? "true" : "false");
            SystemRestartUtils.showSystemRestartDialog(getContext());
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider(R.xml.spoofing) {

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                List<String> keys = super.getNonIndexableKeys(context);
                final Resources resources = context.getResources();

                return keys;
            }
        };
}
