// @formatter:off
/*
 * MocaAuthenticatorService.java - authenticator service
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

package org.level28.android.moca.service;

import org.level28.android.moca.sync.MocaAuthenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Authenticator service for MOCA.
 * <p>
 * Apparently, Google thinks that everybody likes to force users to authenticate
 * themselves to synchronize data across the network...
 * 
 * @author Matteo Panella
 */
public class MocaAuthenticatorService extends Service {

    private MocaAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new MocaAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
