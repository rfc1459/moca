// @formatter:off
/*
 * MocaApp.java - Android application module
 * Copyright (C) 2012 Matteo Panella <morpheus@level28.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
// @formatter:on

package org.level28.android.moca;

import java.util.Locale;

import org.level28.android.moca.bitmaps.BitmapLruCache;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Android application module for MOCA
 * 
 * @author Matteo Panella
 */
public class MocaApp extends Application {
    private static final String LOG_TAG = "MocaApp";

    public static final String SETTINGS_LOCALE = "locale";
    public static final String LOCALE_SYSTEM = "SYSTEM";

    private Locale mLocale = null;

    /** Global L1 bitmap cache */
    private BitmapLruCache mBitmapCache;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Reset locale if we're using a custom one
        if (mLocale != null) {
            changeLocale(newConfig);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the global bitmap cache
        mBitmapCache = new BitmapLruCache(this);

        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        final Configuration config = getBaseContext().getResources()
                .getConfiguration();

        // Check for a locale string inside the application shared preferences
        final String[] langCode = settings.getString(SETTINGS_LOCALE,
                LOCALE_SYSTEM).split("_", 2);
        final String language = langCode[0];
        final String country = langCode.length == 2 ? langCode[1] : "";

        if (shouldSwitchLocale(config.locale, language, country)) {
            mLocale = new Locale(language, country);
            changeLocale(config);
        }
    }

    @Override
    public void onLowMemory() {
        Log.w(LOG_TAG, "Running low on memory, trimming bitmap cache!");
        // Try to trim the L1 bitmap cache to reclaim some space
        mBitmapCache.trimMemory();
    }

    /** Get a reference to the global {@link BitmapLruCache}. */
    public final BitmapLruCache getBitmapCache() {
        return mBitmapCache;
    }

    /** Typesafe version of {@link #getApplicationContext()}. */
    public static final MocaApp getApplication(Context context) {
        return (MocaApp) context.getApplicationContext();
    }

    /**
     * Check if we need to switch to a custom locale during application startup
     * 
     * @param current
     *            current application locale
     * @param lang
     *            two-letter language code
     * @param country
     *            two-letter country code
     * @return true if the user requested a custom locale which doesn't match
     *         the current one, false otherwise
     */
    private static boolean shouldSwitchLocale(final Locale current,
                                              final String lang,
                                              final String country) {
        return !LOCALE_SYSTEM.equals(lang)
                && (!lang.equals(current.getLanguage()) || !country
                        .equals(current.getCountry()));
    }

    /** Perform the actual locale change */
    private void changeLocale(Configuration config) {
        config.locale = mLocale;
        Locale.setDefault(mLocale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }
}
