// @formatter:off
/*
 * ScheduleProvider.java - content provider for MOCA schedule
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

package org.level28.android.moca.provider;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.level28.android.moca.provider.ScheduleContract.Sessions;
import org.level28.android.moca.provider.ScheduleDatabase.Tables;
import org.level28.android.moca.util.SelectionBuilder;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * Content provider for MOCA schedule.
 * <p>
 * Heavily inspired by the Google iosched app.
 * 
 * @author Matteo Panella
 */
public class ScheduleProvider extends ContentProvider {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = "ScheduleProvider";

    private ScheduleDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int SESSIONS = 100;
    private static final int SESSIONS_AT = 101;
    private static final int SESSIONS_DAY = 102;
    private static final int SESSIONS_ID = 103;

    /** Build all URIs supported by this content provider. */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ScheduleContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "sessions", SESSIONS);
        matcher.addURI(authority, "sessions/at/*", SESSIONS_AT);
        matcher.addURI(authority, "sessions/day/*", SESSIONS_DAY);
        matcher.addURI(authority, "sessions/*", SESSIONS_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ScheduleDatabase(getContext());
        return true;
    }

    private void deleteDatabase() {
        mOpenHelper.close();
        final Context context = getContext();
        ScheduleDatabase.deleteDatabase(context);
        mOpenHelper = new ScheduleDatabase(context);
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
        case SESSIONS:
        case SESSIONS_AT:
        case SESSIONS_DAY:
            return Sessions.CONTENT_TYPE;
        case SESSIONS_ID:
            return Sessions.CONTENT_ITEM_TYPE;
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        final int match = sUriMatcher.match(uri);
        final SelectionBuilder builder = buildSelection(uri, match);
        return builder.where(selection, selectionArgs).query(db, projection,
                sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
        case SESSIONS:
            db.insertOrThrow(Tables.SESSIONS, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Sessions.buildSessionUri(values
                    .getAsString(Sessions.SESSION_ID));
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        final SelectionBuilder builder = buildSelection(uri, match);
        int updated = builder.where(selection, selectionArgs)
                .update(db, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return updated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uri == ScheduleContract.BASE_CONTENT_URI) {
            // Wipe the entire database
            deleteDatabase();
            getContext().getContentResolver().notifyChange(uri, null);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        final SelectionBuilder builder = buildSelection(uri, match);
        int deleted = builder.where(selection, selectionArgs).delete(db);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleted;
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Build a selection specification for the given URI match.
     */
    private SelectionBuilder buildSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();

        switch (match) {
        case SESSIONS:
            return builder.table(Tables.SESSIONS);
        case SESSIONS_AT: {
            final String time = Sessions.getSearchQuery(uri);
            return builder.table(Tables.SESSIONS)
                    .where(Sessions.SESSION_START + " <= ?", time)
                    .where(Sessions.SESSION_END + " >= ?", time);
        }
        case SESSIONS_DAY: {
            final String day = Sessions.getSearchQuery(uri);
            return builder.table(Tables.SESSIONS)
                    .where(Sessions.SESSION_DAY + " = ?", day);
        }
        case SESSIONS_ID: {
            final String id = Sessions.getSessionId(uri);
            return builder.table(Tables.SESSIONS).where(
                    Sessions.SESSION_ID + " = ?", id);
        }
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
}
