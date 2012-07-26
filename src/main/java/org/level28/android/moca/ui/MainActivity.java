// @formatter:off
/*
 * MainActivity.java - main UI for MOCA
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

package org.level28.android.moca.ui;

import org.level28.android.moca.BuildConfig;
import org.level28.android.moca.R;
import org.level28.android.moca.bitmaps.NetworkAvatarLoader;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.bugsense.trace.BugSenseHandler;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Main Activity for MOCA app.
 * 
 * @author Matteo Panella
 */
public class MainActivity extends SherlockFragmentActivity {
    private static final String LOG_TAG = "MainActivity";

    private boolean mDualPane;

    private NetworkAvatarLoader mAvatarLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.v(LOG_TAG, "MainActivity awakening");
            // Debug build - enable StrictMode assertions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                setupStrictMode();
            }
        } else {
            // Production/testing build - enable BugSense
            setupBugSense();
        }

        setContentView(R.layout.main);

        mAvatarLoader = new NetworkAvatarLoader(this);

        // Check if we're running in dual-pane mode
        mDualPane = getResources().getBoolean(R.bool.dualPaned);

        if (mDualPane) {
            // We do
            // TODO!
        } else {
            // We don't :-)
            final ViewPager pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(new MocaFragmentAdapter(
                    getSupportFragmentManager(), getResources()));

            final TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
            indicator.setViewPager(pager);
        }

        if (BuildConfig.DEBUG) {
            Log.v(LOG_TAG, "MainActivity awakened, have fun ;-)");
        }
    }

    @Override
    protected void onDestroy() {
        mAvatarLoader.release(true);
        super.onDestroy();
    }

    public NetworkAvatarLoader avatarLoader() {
        return mAvatarLoader;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Setup {@link StrictMode} assertions in debug builds.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void setupStrictMode() {
        final ThreadPolicy.Builder threadPolicyBuilder = new ThreadPolicy.Builder();

        // StrictMode thread policy:
        threadPolicyBuilder.detectNetwork() // No network activity in UI thread
                .penaltyLog() // Log violations
                .penaltyDeath(); // Kill the process on violation
        StrictMode.setThreadPolicy(threadPolicyBuilder.build());

        /*
         * NOTE: we don't define a StrictMode VmPolicy because the Google Maps
         * API misbehaves *BADLY* and it will pollute the logs with heaps of
         * messages we can't do anything about. -morph
         */
        Log.i(LOG_TAG,
                "StrictMode enabled - Everyone, please observe that the \"fasten your seatbelt\" and \"no smoking\" signs have been turned on. Sit back and enjoy your flight.");
    }

    /**
     * Setup integration with BugSense (only for production builds).
     */
    private void setupBugSense() {
        final String key = getResources().getString(R.string.bugsense_key);
        if ("invalid-key".equals(key)) {
            Log.i(LOG_TAG, "Skipping BugSense setup");
        }
        BugSenseHandler.setup(this, key);
        Log.i(LOG_TAG, "BugSense handler installed and ready");
    }
}
