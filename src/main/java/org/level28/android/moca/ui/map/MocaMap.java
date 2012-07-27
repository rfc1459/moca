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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

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

    private MyLocationOverlay mMyLocation;
    private boolean mMyLocationEnabled;
    private boolean mLocationWasEnabled;

    private String mMocaLatLonUrl;

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

        // Setup current location overlay
        mMyLocation = new MyLocationOverlay(this, mMap);
        mLocationWasEnabled = false;
        mMyLocationEnabled = false;

        // Store the latlon url for later use by the navigator
        mMocaLatLonUrl = Float.valueOf(coords[0] / 1e6f).toString() + ","
                + Float.valueOf(coords[1] / 1e6f);

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
    protected void onResume() {
        super.onResume();
        if (mLocationWasEnabled) {
            enableMyLocation(true);
        }
    }

    @Override
    protected void onPause() {
        // Make sure we ALWAYS disable the location overlay before pausing the
        // activity
        mLocationWasEnabled = mMyLocationEnabled;
        enableMyLocation(false);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // actionbar icon was clicked, terminate this activity
            finish();
            return true;
        case R.id.menu_locate:
            // Toggle current location visibility
            enableMyLocation(!mMyLocationEnabled);
            if (mMyLocationEnabled) {
                scheduleJumpToCurrentLocation();
            }
            return true;
        case R.id.menu_directions:
            startNavigator();
            return true;
        default:
            return false;
        }
    }

    /**
     * Animate a jump to current location as soon as we have a fix.
     */
    private void scheduleJumpToCurrentLocation() {
        mMyLocation.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                mMap.getController().animateTo(mMyLocation.getMyLocation());
            }
        });
    }

    /**
     * Toggle {@link MyLocationOverlay} functionality.
     * 
     * @param enabled
     *            {@code true} to enable the location overlay, {@code false} to
     *            disable it
     */
    private void enableMyLocation(final boolean enabled) {
        if (mMyLocationEnabled == enabled) {
            return;
        }

        mMyLocationEnabled = enabled;

        if (enabled) {
            mMap.getOverlays().add(mMyLocation);
            mMyLocation.enableMyLocation();
        } else {
            mMyLocation.disableMyLocation();
            mMap.getOverlays().remove(mMyLocation);
        }
        mMap.postInvalidate();

        // FIXME: switch location action item image?
    }

    /**
     * (Try to) start the Google Navigator to get directions for MOCA.
     */
    private void startNavigator() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + mMocaLatLonUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
