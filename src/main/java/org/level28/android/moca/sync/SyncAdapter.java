// @formatter:off
/*
 * SyncAdapter.java - data synchronization adapter for MOCA
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

import org.level28.android.moca.BuildConfig;
import org.level28.android.moca.json.JsonDeserializerException;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Synchronization adapter for MOCA.
 * <p>
 * This class allows the system to perform data synchronization on our behalf
 * while minimizing battery and data usage. Ok, that's not entirely true since
 * we force the synchronization (sometimes), but this class will enable
 * application data to be synchronized without leaving services and the like
 * hanging around while the application is closed.
 * 
 * @author Matteo Panella
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = "SyncAdapter";

    private final Context mContext;
    private SyncHelper mSyncHelper;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;

        // Suppress uncaught exception message in production builds
        // Blatantly ripped from Google iosched
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Log.e(LOG_TAG,
                            "Uncaught sync exception, suppressing UI in release build.",
                            ex);
                }
            });
        }
    }

    @Override
    public void onPerformSync(final Account account, Bundle extras,
            String authority, final ContentProviderClient provider,
            final SyncResult syncResult) {
        // Oh boy...
        final boolean uploadOnly = extras.getBoolean(
                ContentResolver.SYNC_EXTRAS_UPLOAD, false);
        final boolean manualSync = extras.getBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, false);
        final boolean initialize = extras.getBoolean(
                ContentResolver.SYNC_EXTRAS_INITIALIZE, false);

        if (uploadOnly) {
            // We don't support uploads
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, "Starting synchronization, manualSync=" + manualSync
                    + " initialize=" + initialize);
        }

        if (initialize) {
            // Check for AccountManager/SyncProvider SNAFUs
            final boolean isHardcodedAccount = MocaAuthenticator.HARDCODED_USERNAME
                    .equals(account.name);
            ContentResolver.setIsSyncable(account, authority,
                    isHardcodedAccount ? 1 : 0);
            if (!isHardcodedAccount) {
                // OK, it was a SNAFU, abort now and prevent future
                // synchronization operations.
                ++syncResult.stats.numAuthExceptions;
                return;
            }
        }

        // Use our synchronization helper to perform all the dirty work
        if (mSyncHelper == null) {
            mSyncHelper = new SyncHelper(mContext);
        }

        try {
            mSyncHelper.performSync(syncResult);
        } catch (IOException e) {
            ++syncResult.stats.numIoExceptions;
            Log.e(LOG_TAG, "I/O error while syncing data for MOCA", e);
        } catch (JsonDeserializerException e) {
            ++syncResult.stats.numParseExceptions;
            Log.e(LOG_TAG, "Parse error while syncing data for MOCA", e);
        }
    }
}
