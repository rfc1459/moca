// @formatter:off
/*
 * ScheduleActivity.java - display MOCA schedule
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

package org.level28.android.moca.ui.schedule;

import static com.google.common.base.Preconditions.checkElementIndex;

import org.level28.android.moca.R;
import org.level28.android.moca.provider.ScheduleContract;
import org.level28.android.moca.sync.MocaAuthenticator;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Display MOCA schedule for talks and main events.
 * 
 * @author Matteo Panella
 */
public class ScheduleActivity extends SherlockFragmentActivity implements
        ActionBar.OnNavigationListener {

    private static final String URI_KEY = "_uri";
    private static final String CURRENT_DAY_KEY = "currentDay";

    // Day 2 sunrise: 2012-08-25T00:00:00+02:00
    private static final long DAY2_SUNRISE = 1345845600000L;
    // Day 3 sunrise: 2012-08-26T00:00:00+02:00
    private static final long DAY3_SUNRISE = 1345932000000L;

    private CharSequence[] mScheduleDays;

    private int mCurrentDay;

    private SpinnerAdapter mSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();

        // Unpack day labels from resources and create a SpinnerAdapter suitable
        // for the ActionBar navigation mode
        mScheduleDays = getResources().getTextArray(R.array.scheduleDays);
        mSpinnerAdapter = new ArrayAdapter<CharSequence>(
                actionBar.getThemedContext(),
                com.actionbarsherlock.R.layout.sherlock_spinner_dropdown_item,
                mScheduleDays);

        // As usual, enable standard application icon navigation
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        // ... but hide the activity title, since it will be replaced by the
        // navigation mode spinner
        actionBar.setDisplayShowTitleEnabled(false);

        // Enable navigation mode
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);

        setContentView(R.layout.schedule);

        // Choose a sensible default day based on current date/time
        final long now = System.currentTimeMillis();
        if (now < DAY2_SUNRISE) {
            mCurrentDay = 1;
        } else if (now < DAY3_SUNRISE) {
            mCurrentDay = 2;
        } else {
            mCurrentDay = 3;
        }

        if (savedInstanceState == null) {
            // Force a refresh on startup
            triggerRefresh();
        } else {
            // Restore selected day
            final int currentDay = savedInstanceState.getInt(CURRENT_DAY_KEY, mCurrentDay);
            // Safety check
            if (currentDay > 0 && currentDay < mScheduleDays.length) {
                mCurrentDay = currentDay;
            }
        }
        // This will invoke refreshSchedule() through onNavigationItemSelected()
        actionBar.setSelectedNavigationItem(mCurrentDay - 1);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Retain the currently selected day across configuration changes
        outState.putInt(CURRENT_DAY_KEY, mCurrentDay);
    }

    @Override
    protected void onDestroy() {
        mSpinnerAdapter = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.refresh_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // Go up the stack
            finish();
            return true;
        case R.id.menu_refresh:
            triggerRefresh();
            return true;
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(final int position, final long id) {
        checkElementIndex(position, mScheduleDays.length);
        // Update the current day and refresh all fragments
        mCurrentDay = position + 1;
        refreshSchedule();
        return true;
    }

    /**
     * Convert a fragment arguments bundle into an intent.
     */
    public static Intent fragmentArgumentsToIntent(Bundle arguments) {
        Intent intent = new Intent();
        if (arguments == null) {
            return intent;
        }

        final Uri data = arguments.getParcelable(URI_KEY);
        if (data != null) {
            intent.setData(data);
        }

        intent.putExtras(arguments);
        intent.removeExtra(URI_KEY);
        return intent;
    }

    /**
     * Inverse operation of {@link #fragmentArgumentsToIntent(Bundle)}.
     */
    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable(URI_KEY, data);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(extras);
        }
        return arguments;
    }

    /**
     * Perform a manual synchronization
     */
    private void triggerRefresh() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(new Account(
                MocaAuthenticator.HARDCODED_USERNAME,
                MocaAuthenticator.ACCOUNT_TYPE),
                ScheduleContract.CONTENT_AUTHORITY, extras);
    }

    /**
     * Refresh activity UI on schedule day selection.
     */
    private void refreshSchedule() {
        final Uri dayUri = ScheduleContract.Sessions
                .buildSessionsDayDirUri(mCurrentDay);

        // Create a new SessionListFragment for the current schedule day
        final Fragment sessionsList = new SessionListFragment();
        sessionsList.setArguments(intentToFragmentArguments(new Intent(
                Intent.ACTION_VIEW, dayUri)));

        // Setup and execute the fragment transaction
        final FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.sessionListFragment, sessionsList);
        // TODO: reset detail fragment in dual-pane layout
        ft.commit();
    }
}
