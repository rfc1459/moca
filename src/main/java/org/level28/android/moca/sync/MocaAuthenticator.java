// @formatter:off
/*
 * MocaAuthenticator.java - account manager for MOCA
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

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_ERROR_CODE;
import static android.accounts.AccountManager.KEY_ERROR_MESSAGE;

import org.level28.android.moca.provider.ScheduleContract;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

/**
 * Dear Google, not everybody requires authentication to synchronize data...
 * 
 * @author Matteo Panella
 */
public class MocaAuthenticator extends AbstractAccountAuthenticator {

    public static final String ACCOUNT_TYPE = "org.level28.android.moca";

    public static final String HARDCODED_USERNAME = "MOCA";
    private static final String HARDCODED_PASSWORD = "SonicAmicoDeiLamer";

    private Context mContext;

    public MocaAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
            String accountType, String authTokenType,
            String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        final Bundle reply = new Bundle();
        AccountManager am = AccountManager.get(mContext);
        if (dummyAccountExists(am)) {
            // No way
            reply.putString(KEY_ERROR_CODE, "403");
            // FIXME: extract string into resources
            reply.putString(KEY_ERROR_MESSAGE, "Account already exists");
        } else {
            // Create the standard MOCA dummy account
            final String accountName = addDummyAccount(am);
            reply.putString(KEY_ACCOUNT_NAME, accountName);
            reply.putString(KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        }
        return reply;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
            Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
            String accountType) {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (ACCOUNT_TYPE.equals(authTokenType)) {
            return authTokenType;
        }
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
            Account account, String[] features) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        return null;
    }

    /**
     * Check if the standard MOCA dummy account exists.
     */
    public static boolean dummyAccountExists(AccountManager am) {
        return am.getAccountsByType(ACCOUNT_TYPE).length > 0;
    }

    /**
     * Create a standard MOCA dummy account.
     * 
     * @return the dummy account name
     */
    public static String addDummyAccount(AccountManager am) {
        final Account dummyAccount = new Account(HARDCODED_USERNAME,
                ACCOUNT_TYPE);
        am.addAccountExplicitly(dummyAccount, HARDCODED_PASSWORD, null);
        // Ensure the dummy account is bound to our content provider
        ContentResolver.setIsSyncable(dummyAccount, ScheduleContract.CONTENT_AUTHORITY, 1);
        // Don't force-enable the master sync switch, respect the user's choice
        // ... nevertheless enable automatic synchronization of schedules (subject to the master sync switch)
        ContentResolver.setSyncAutomatically(dummyAccount, ScheduleContract.CONTENT_AUTHORITY, true);
        return dummyAccount.name;
    }
}
