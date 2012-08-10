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

import org.level28.android.moca.R;
import org.level28.android.moca.provider.ScheduleContract;
import org.level28.android.moca.sync.MocaAuthenticator;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();

        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // TODO: enable navigation mode

        setContentView(R.layout.schedule);

        if (savedInstanceState == null) {
            // Force a refresh on startup
            triggerRefresh();
        }
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
    public boolean onNavigationItemSelected(int position, long id) {
        // TODO Auto-generated method stub
        return false;
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
}
