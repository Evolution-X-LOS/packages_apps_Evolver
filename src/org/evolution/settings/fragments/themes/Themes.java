/*
 * Copyright (C) 2019-2024 The Evolution X Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.settings.fragments.themes;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.evolution.Utils;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;

import org.evolution.settings.preferences.GlobalSettingListPreference;
import org.evolution.settings.utils.DeviceUtils;
import org.evolution.settings.utils.SystemUtils;

@SearchIndexable
public class Themes extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "Themes";

    private static final String KEY_LOCK_SOUND = "lock_sound";
    private static final String KEY_UNLOCK_SOUND = "unlock_sound";
    private static final String KEY_ICONS_CATEGORY = "themes_icons_category";
    private static final String KEY_NAVBAR_ICON = "android.theme.customization.navbar";
    private static final String KEY_SIGNAL_ICON = "android.theme.customization.signal_icon";
    private static final String KEY_UDFPS_ICON = "udfps_icon";
    private static final String KEY_ANIMATIONS_CATEGORY = "themes_animations_category";
    private static final String KEY_UDFPS_ANIMATION = "udfps_animation";
    private static final String KEY_BOOT_ANIMATION = "boot_animation";
    private static final String KEY_LAUNCHER_CATEGORY = "themes_launcher_category";

    private GlobalSettingListPreference mLockSound;
    private GlobalSettingListPreference mUnlockSound;
    private PreferenceCategory mLauncherCategory;
    private PreferenceCategory mIconsCategory;
    private Preference mNavbarIcon;
    private Preference mSignalIcon;
    private Preference mUdfpsIcon;
    private PreferenceCategory mAnimationsCategory;
    private Preference mBootAnimation;
    private Preference mUdfpsAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.evolution_settings_themes);

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources resources = context.getResources();

        mLockSound = (GlobalSettingListPreference) findPreference(KEY_LOCK_SOUND);
        mLockSound.setOnPreferenceChangeListener(this);
        mUnlockSound = (GlobalSettingListPreference) findPreference(KEY_UNLOCK_SOUND);
        mUnlockSound.setOnPreferenceChangeListener(this);
        mLauncherCategory = (PreferenceCategory) findPreference(KEY_LAUNCHER_CATEGORY);
        mIconsCategory = (PreferenceCategory) findPreference(KEY_ICONS_CATEGORY);
        mNavbarIcon = (Preference) findPreference(KEY_NAVBAR_ICON);
        mSignalIcon = (Preference) findPreference(KEY_SIGNAL_ICON);
        mUdfpsIcon = (Preference) findPreference(KEY_UDFPS_ICON);
        mAnimationsCategory = (PreferenceCategory) findPreference(KEY_ANIMATIONS_CATEGORY);
        mBootAnimation = (Preference) findPreference(KEY_BOOT_ANIMATION);
        mUdfpsAnimation = (Preference) findPreference(KEY_UDFPS_ANIMATION);

        if (DeviceUtils.isCurrentlySupportedPixel()) {
            mAnimationsCategory.removePreference(mBootAnimation);
        }

        if (!DeviceUtils.deviceSupportsMobileData(context)) {
            mIconsCategory.removePreference(mSignalIcon);
        }

        if (DeviceUtils.isEdgeToEdgeEnabled(context)) {
            mIconsCategory.removePreference(mNavbarIcon);
        }

        FingerprintManager fingerprintManager = (FingerprintManager)
                getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
            mIconsCategory.removePreference(mUdfpsIcon);
            mAnimationsCategory.removePreference(mUdfpsAnimation);
        } else {
            if (!Utils.isPackageInstalled(context, "org.evolution.udfps.icons")) {
                mIconsCategory.removePreference(mUdfpsIcon);
            }
            if (!Utils.isPackageInstalled(context, "org.evolution.udfps.animations")) {
                mAnimationsCategory.removePreference(mUdfpsAnimation);
            }
        }

        if (!Utils.isPackageInstalled(context, "com.google.android.apps.nexuslauncher")) {
            prefScreen.removePreference(mLauncherCategory);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        if (preference == mLockSound || preference == mUnlockSound) {
            SystemUtils.showSystemUiRestartDialog(context);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider(R.xml.evolution_settings_themes) {

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                List<String> keys = super.getNonIndexableKeys(context);
                final Resources resources = context.getResources();

                FingerprintManager fingerprintManager = (FingerprintManager)
                        context.getSystemService(Context.FINGERPRINT_SERVICE);

                if (DeviceUtils.isCurrentlySupportedPixel()) {
                    keys.add(KEY_BOOT_ANIMATION);
                }

                if (!DeviceUtils.deviceSupportsMobileData(context)) {
                    keys.add(KEY_SIGNAL_ICON);
                }

                if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                    keys.add(KEY_UDFPS_ICON);
                    keys.add(KEY_UDFPS_ANIMATION);
                } else {
                    if (!Utils.isPackageInstalled(context, "org.evolution.udfps.icons")) {
                        keys.add(KEY_UDFPS_ICON);
                    }
                    if (!Utils.isPackageInstalled(context, "org.evolution.udfps.animations")) {
                        keys.add(KEY_UDFPS_ANIMATION);
                    }
                }
                return keys;
            }
        };
}
