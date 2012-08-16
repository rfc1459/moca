// @formatter:off
/*
 * SessionDetailActivity.java - session details fragment wrapper for single-pane mode
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

import static org.level28.android.moca.util.ActivityUtils.intentToFragmentArguments;

import org.level28.android.moca.R;

import android.os.Bundle;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * Single-pane mode wrapper for {@link SessionDetailFragment}.
 * 
 * @author Matteo Panella
 */
public class SessionDetailActivity extends SherlockFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.dualPaned)) {
            // The application entered dual-pane mode, go up the activity stack
            navigateUp();
        }

        // Setup action bar for Up navigation
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // Create the details fragment
        SessionDetailFragment detailsFragment = new SessionDetailFragment();
        // Bind it with the inbound intent
        detailsFragment.setArguments(intentToFragmentArguments(getIntent()));
        // And display it as our sole contents
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, detailsFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // Provide up navigation
            navigateUp();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Go back up the activity stack.
     */
    private void navigateUp() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
