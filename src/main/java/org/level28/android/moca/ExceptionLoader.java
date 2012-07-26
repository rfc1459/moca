// @formatter:off
/*
 * ExceptionLoader.java - loader capable of exception reporting
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

package org.level28.android.moca;

import android.content.Context;
import android.util.Log;

/**
 * Loader capable of asynchronous exception reporting.
 * 
 * @param <D>
 *            type of data returned by this loader
 * @author Matteo Panella
 */
public abstract class ExceptionLoader<D> extends AsyncLoader<D> {

    private static final String LOG_TAG = "ExceptionLoader";

    private final D mDefaultData;

    private Exception mException;

    /**
     * Create a loader seeded with default data.
     * <p>
     * Default data will be returned on exception.
     * 
     * @param context
     *            the {@link Context} for this loader
     * @param mDefaultData
     *            data to return in case of exception
     */
    public ExceptionLoader(Context context, D defaultData) {
        super(context);
        this.mDefaultData = defaultData;
    }

    @Override
    public D loadInBackground() {
        mException = null;
        try {
            return performLoad();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "Exception in performLoad()", e);
            }
            mException = e;
            return mDefaultData;
        }
    }

    /**
     * Get the last stored exception (if any).
     */
    public Exception getException() {
        return mException;
    }

    /**
     * Clear the last stored exception and return it.
     */
    public Exception clearException() {
        final Exception e = mException;
        mException = null;
        return e;
    }

    /**
     * Perform the actual background data loading.
     */
    protected abstract D performLoad() throws Exception;
}
