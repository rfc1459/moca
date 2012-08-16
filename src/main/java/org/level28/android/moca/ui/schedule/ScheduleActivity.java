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

import static android.widget.AdapterView.INVALID_ROW_ID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

import org.level28.android.moca.R;
import org.level28.android.moca.provider.ScheduleContract;
import org.level28.android.moca.provider.ScheduleContract.Sessions;
import org.level28.android.moca.sync.MocaAuthenticator;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
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
        ActionBar.OnNavigationListener,
        SessionListFragment.OnSessionSelectedListener {

    private static final String CURRENT_DAY_KEY = "currentDay";
    private static final String CURRENT_SESSION_KEY = "currentSession";
    private static final String SESSION_LIST_ID_KEY = "sessionListId";

    static final String DETAILS_FRAGMENT_TAG = "sessionDetails";

    // Used in dual-pane mode only
    private String mCurrentSessionUUID;
    private long mCurrentSessionRowId = INVALID_ROW_ID;

    // Day 2 sunrise: 2012-08-25T00:00:00+02:00
    private static final long DAY2_SUNRISE = 1345845600000L;
    // Day 3 sunrise: 2012-08-26T00:00:00+02:00
    private static final long DAY3_SUNRISE = 1345932000000L;

    private CharSequence[] mScheduleDays;

    private int mCurrentDay;

    private SessionListFragment mSessionListFragment;
    private SessionDetailFragment mSessionDetailsFragment;

    private SpinnerAdapter mSpinnerAdapter;

    private boolean mDualPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule);

        // Check if we're running in dual-pane mode
        mDualPane = getResources().getBoolean(R.bool.dualPaned);

        // Lookup our fragments
        final FragmentManager fm = getSupportFragmentManager();
        mSessionListFragment = (SessionListFragment) fm
                .findFragmentById(R.id.sessionListFragment);
        mSessionDetailsFragment = (SessionDetailFragment) fm
                .findFragmentById(R.id.sessionDetailsFragment);

        // Session list should be selectable only in dual-pane mode
        mSessionListFragment.setSelectable(mDualPane);

        // Unpack day labels from resources and create a SpinnerAdapter suitable
        // for the ActionBar navigation mode
        mScheduleDays = getResources().getTextArray(R.array.scheduleDays);
        mSpinnerAdapter = new ArrayAdapter<CharSequence>(getSupportActionBar()
                .getThemedContext(),
                com.actionbarsherlock.R.layout.sherlock_spinner_dropdown_item,
                mScheduleDays);

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
            restoreState(savedInstanceState);
        }

        // Perform ActionBar setup
        setupActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSessionListFragment.setOnSessionSelectedListener(this);
    }

    @Override
    protected void onStop() {
        mSessionListFragment.setOnSessionSelectedListener(null);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Retain the currently selected day and session id (if any)
        outState.putInt(CURRENT_DAY_KEY, mCurrentDay);
        outState.putString(CURRENT_SESSION_KEY, mCurrentSessionUUID);
        outState.putLong(SESSION_LIST_ID_KEY, mCurrentSessionRowId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreState(savedInstanceState);
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
            NavUtils.navigateUpFromSameTask(this);
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
        setScheduleDay(position + 1);
        return true;
    }

    @Override
    public void onSessionSelected(final String sessionId, final long listItemId) {
        checkNotNull(sessionId);
        // Store the rowId for dual-pane mode lifecycle
        mCurrentSessionRowId = listItemId;
        displaySessionDetails(sessionId);
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
     * Restore activity state during some of the more involved lifecycle events.
     * 
     * @param savedInstanceState
     *            the previously saved state of this activity
     */
    private void restoreState(Bundle savedInstanceState) {
        // Restore selected day
        final int currentDay = savedInstanceState.getInt(CURRENT_DAY_KEY,
                mCurrentDay);
        // Safety check
        if (currentDay > 0 && currentDay < mScheduleDays.length) {
            mCurrentDay = currentDay;
        }
        // Restore selected session id
        mCurrentSessionUUID = savedInstanceState.getString(CURRENT_SESSION_KEY);

        // Now update the UI
        mSessionListFragment.loadScheduleForDay(mCurrentDay);
        if (mDualPane) {
            // Restore session list id
            mCurrentSessionRowId = savedInstanceState.getLong(
                    SESSION_LIST_ID_KEY, INVALID_ROW_ID);
            mSessionListFragment.setSelectedId(mCurrentSessionRowId);
            displaySessionDetails(mCurrentSessionUUID);
        }
    }

    /**
     * Setup the action bar for list navigation mode.
     */
    private void setupActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        // As usual, enable standard application icon navigation
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        // ... but hide the activity title, since it will be replaced by the
        // navigation mode spinner
        actionBar.setDisplayShowTitleEnabled(false);

        // Enable navigation mode
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);

        // This will invoke refreshSchedule() through onNavigationItemSelected()
        actionBar.setSelectedNavigationItem(mCurrentDay - 1);
    }

    /**
     * Notify the {@link SessionListFragment} of a day change.
     * 
     * @param day
     *            the day that should be displayed
     */
    private void setScheduleDay(final int day) {
        mCurrentDay = day;

        // Update the schedule fragment
        mSessionListFragment.loadScheduleForDay(mCurrentDay);

        if (mDualPane) {
            // Clear the details fragment
            displaySessionDetails(null);
        }
    }

    /**
     * Display details for the given Session.
     * <p>
     * Depending on the UI mode, the details will be shown inline or a new
     * activity will be launched.
     * 
     * @param sessionId
     *            UUID of the session
     */
    private void displaySessionDetails(final String sessionId) {
        mCurrentSessionUUID = sessionId;
        if (mDualPane) {
            mSessionDetailsFragment.loadSessionDetails(sessionId);
        } else {
            // In single-pane mode we can't have a NULL sessionId
            checkNotNull(sessionId);
            checkArgument(!TextUtils.isEmpty(sessionId),
                    "Session ID may not be empty");
            // Spawn the details activity
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Sessions.buildSessionUri(sessionId)));
        }
    }
}
