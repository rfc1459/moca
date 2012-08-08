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
import org.level28.android.moca.bitmaps.SimpleBitmapLoader;
import org.level28.android.moca.ui.map.MocaMap;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Main Activity for MOCA app.
 * 
 * @author Matteo Panella
 */
public class MainActivity extends SherlockFragmentActivity implements
        OnItemClickListener {
    private static final String STATE_ACTIVE_POSITION = "active_position";

    private static final String LOG_TAG = "MainActivity";

    private boolean mDualPane;

    private NetworkAvatarLoader mAvatarLoader;

    private SimpleBitmapLoader mBannerLoader;

    private MocaFragmentAdapter mFragmentAdapter;

    private FragmentListAdapter mFragmentListAdapter;

    private ListView mFragmentList;

    private ViewPager mPager;

    private int mActivePosition = ListView.INVALID_POSITION;

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

        // Create a new loader for avatars
        mAvatarLoader = new NetworkAvatarLoader(this);
        // and a new loader for banners
        mBannerLoader = new SimpleBitmapLoader(this);

        // Instantiate a new FragmentPagerAdapter
        mFragmentAdapter = new MocaFragmentAdapter(getSupportFragmentManager(),
                getResources());

        /*
         * Both layouts use a ViewPager to host fragments (it's less hacky). The
         * difference between the two is an implementation detail we're not
         * interested in here.
         */
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mFragmentAdapter);

        // Check if we're running in dual-pane mode
        mDualPane = getResources().getBoolean(R.bool.dualPaned);

        if (mDualPane) {
            // We do - use a list to select the currently active fragment
            mFragmentList = (ListView) findViewById(R.id.fragmentList);

            // Setup the list adapter
            mFragmentListAdapter = new FragmentListAdapter(this, mPager);
            mFragmentList.setAdapter(mFragmentListAdapter);
            mFragmentList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            if (savedInstanceState != null
                    && savedInstanceState.containsKey(STATE_ACTIVE_POSITION)) {
                // Restore active position
                int position = savedInstanceState.getInt(STATE_ACTIVE_POSITION);
                if (position == ListView.INVALID_POSITION) {
                    // Reset position to ground state
                    mFragmentList.setItemChecked(mActivePosition, false);
                    position = 0;
                }
                mFragmentList.setItemChecked(position, true);
                mPager.setCurrentItem(position, false);
                mActivePosition = position;
            } else {
                // Start from position 0
                mActivePosition = 0;
                mFragmentList.setItemChecked(mActivePosition, true);
            }
        } else {
            // We don't, fall back to the less-challenging phone UI :-)
            final TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
            indicator.setViewPager(mPager);

            // Synchronize current page with the dual-pane layout
            if (savedInstanceState != null
                    && savedInstanceState.containsKey(STATE_ACTIVE_POSITION)) {
                final int position = savedInstanceState
                        .getInt(STATE_ACTIVE_POSITION);
                if (position != ListView.INVALID_POSITION) {
                    // Position updates *MUST* go through the ViewPagerIndicator
                    indicator.setCurrentItem(position);
                }
            }
        }

        if (BuildConfig.DEBUG) {
            Log.v(LOG_TAG, "MainActivity awakened, have fun ;-)");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mDualPane) {
            mFragmentList.setOnItemClickListener(this);
        }
    }

    @Override
    protected void onStop() {
        if (mDualPane) {
            mFragmentList.setOnItemClickListener(null);
        }
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDualPane) {
            if (mActivePosition != ListView.INVALID_POSITION) {
                outState.putInt(STATE_ACTIVE_POSITION, mActivePosition);
            }
        } else {
            outState.putInt(STATE_ACTIVE_POSITION, mPager.getCurrentItem());
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

    public SimpleBitmapLoader bannerLoader() {
        return mBannerLoader;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_map:
            startActivity(new Intent(this, MocaMap.class));
            return true;
        case R.id.menu_support_mx: {
            final Uri mxUri = Uri.parse(getResources().getString(
                    R.string.support_mx_url));
            startActivity(createBrowserIntent(mxUri));
            return true;
        }
        case R.id.menu_license:
            displayLicenseDialog();
            return true;
        case R.id.menu_about:
            displayAboutDialog();
            return true;
        default:
            return false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int position,
            long id) {
        mActivePosition = position;
        // Since this method is used only in two-pane mode, we have to update
        // the ViewPager directly
        mPager.setCurrentItem(position, true);
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

    /**
     * Create an intent suitable for opening a web page.
     */
    private static Intent createBrowserIntent(final Uri uri) {
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    /**
     * Instantiate and display the license dialog.
     */
    private void displayLicenseDialog() {
        LicenseDialogFragment frag = new LicenseDialogFragment();
        frag.show(getSupportFragmentManager(), "dialog");
    }

    /**
     * Instantiate and display the about dialog.
     */
    private void displayAboutDialog() {
        AboutDialogFragment frag = new AboutDialogFragment();
        frag.show(getSupportFragmentManager(), "dialog");
    }
}
