// @formatter:off
/*
 * MocaMap.java - MOCA map host
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

package org.level28.android.moca.ui.map;

import org.level28.android.moca.R;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * Moca map host.
 * <p>
 * Thanks to Google Maps SDK quirks, this <em>has</em> to be a separate
 * activity.
 * 
 * @author Matteo Panella
 */
public class MocaMap extends SherlockMapActivity {

    private MapView mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map);

        // Enable the "home" function of the actionbar icon
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // Get a reference to our lovely map
        mMap = (MapView) findViewById(R.id.map_view);

        // Center and zoom on MOCA :-)
        final int[] coords = getResources().getIntArray(R.array.moca_coords);
        final int zoomLevel = getResources().getInteger(
                R.integer.map_zoom_factor);
        final GeoPoint moca = new GeoPoint(coords[0], coords[1]);

        final MapController controller = mMap.getController();
        controller.setCenter(moca);
        controller.setZoom(zoomLevel);

        // TODO: pushpin overlay for MOCA
        // TODO: current location overlay
        // TODO: action menu item to center on MOCA
        // TODO: action menu item to start the system navigator (if any)

        // Enable the builtin zoom controls
        mMap.setBuiltInZoomControls(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Kick-off tile preloading
        mMap.preLoad();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // actionbar icon was clicked, terminate this activity
            finish();
            return true;
        default:
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.google.android.maps.MapActivity#isRouteDisplayed()
     */
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
