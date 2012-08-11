// @formatter:off
/*
 * SyncHelper.java - data synchronization helper
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

package org.level28.android.moca.sync;

import java.io.IOException;
import java.util.ArrayList;

import org.level28.android.moca.BuildConfig;
import org.level28.android.moca.json.JsonDeserializerException;
import org.level28.android.moca.provider.ScheduleContract;
import org.level28.android.moca.service.SyncService;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.util.Log;

/**
 * Data synchronization helper.
 * <p>
 * This class performs all the dirty work on behalf of {@link SyncAdapter}.
 * 
 * @author Matteo Panella
 */
class SyncHelper {
    private static final String LOG_TAG = "SyncHelper";

    // TMA-1 host
    private static final String TMA1_BASE_URL = "https://tma-1.level28.org";
    // TMA-1 schedule API endpoint
    private static final String SCHEDULE_URL = TMA1_BASE_URL + "/schedule.json";

    private final Context mContext;
    private final String mUserAgent;

    SyncHelper(final Context context) {
        mContext = context;
        mUserAgent = SyncService.buildUserAgent(context);
    }

    /**
     * Synchronize against a TMA-1 server.
     */
    public void performSync(final SyncResult syncResult) throws IOException, JsonDeserializerException {
        final ContentResolver resolver = mContext.getContentResolver();
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Perform synchronization only if we're online
        if (isOnline()) {
            if (BuildConfig.DEBUG) {
                Log.i(LOG_TAG, "We're online, performing actual synchronization");
            }

            // Synchronize sessions
            SessionHelper sessionHelper = new SessionHelper(SCHEDULE_URL, mUserAgent, resolver);
            batch.addAll(sessionHelper.synchronizeSessions());

            // Apply the batch in a single transaction
            try {
                resolver.applyBatch(ScheduleContract.CONTENT_AUTHORITY, batch);
            } catch (RemoteException e) {
                throw new RuntimeException("Problem applying batch operation", e);
            } catch (OperationApplicationException e) {
                throw new RuntimeException("Problem applying batch operation", e);
            }
            // We're done (hopefully)
            if (BuildConfig.DEBUG) {
                Log.i(LOG_TAG, "Synchronization performed successfully");
            }
        }
    }

    /**
     * Check if the system is online.
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
