// @formatter:off
/*
 * ScheduleDatabase.java - SQLite database helper for MOCA schedule
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

import org.level28.android.moca.BuildConfig;
import org.level28.android.moca.provider.ScheduleContract.SessionColumns;
import org.level28.android.moca.provider.ScheduleContract.SyncColumns;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * SQLite database helper for MOCA schedule.
 * 
 * @author Matteo Panella
 */
public class ScheduleDatabase extends SQLiteOpenHelper {

    private static final String LOG_TAG = "ScheduleDatabase";

    private static final String DATABASE_NAME = "schedule.db";

    private static final int DATABASE_VERSION = 1;

    /** Table names. */
    interface Tables {
        String SESSIONS = "sessions";
    }

    /** Indices names. */
    interface Indices {
        String SESSION_DAY = Tables.SESSIONS + "_" + SessionColumns.SESSION_DAY;
        String SESSION_START = Tables.SESSIONS + "_"
                + SessionColumns.SESSION_START;
        String SESSION_END = Tables.SESSIONS + "_" + SessionColumns.SESSION_END;
    }

    /**
     * @see SQLiteOpenHelper#SQLiteOpenHelper(Context, String,
     *      SQLiteDatabase.CursorFactory, int)
     */
    public ScheduleDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "
                + Tables.SESSIONS
                + " ("
                + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," // I HATE ANDROID
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + SessionColumns.SESSION_ID + " TEXT NOT NULL,"
                + SessionColumns.SESSION_TITLE + " TEXT NOT NULL,"
                + SessionColumns.SESSION_DAY + " INTEGER NOT NULL,"
                + SessionColumns.SESSION_START + " INTEGER NOT NULL,"
                + SessionColumns.SESSION_END + " INTEGER NOT NULL,"
                + SessionColumns.SESSION_HOSTS + " TEXT,"
                + SessionColumns.SESSION_LANG + " TEXT NOT NULL,"
                + SessionColumns.SESSION_ABSTRACT + " TEXT," + "UNIQUE ("
                + SessionColumns.SESSION_ID + ") ON CONFLICT REPLACE)");

        createIndices(db);
    }

    private void createIndices(SQLiteDatabase db) {
        // Index by day
        db.execSQL("CREATE INDEX IF NOT EXISTS " + Indices.SESSION_DAY
                + " ON " + Tables.SESSIONS + " ("
                + SessionColumns.SESSION_DAY + ")");
        // Index session start times
        db.execSQL("CREATE INDEX IF NOT EXISTS " + Indices.SESSION_START
                + " ON " + Tables.SESSIONS + " ("
                + SessionColumns.SESSION_START + ")");
        // Index session end times
        db.execSQL("CREATE INDEX IF NOT EXISTS " + Indices.SESSION_END + " ON "
                + Tables.SESSIONS + " (" + SessionColumns.SESSION_END + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onUpgrade() from " + oldVersion + " to "
                    + newVersion);
        }

        // This switch will eventually be used to perform incremental updates to
        // the database schema.
        int version = oldVersion;

        switch (version) {
        default:
            break;
        }

        // Check if the incremental update brought us to the current version,
        // otherwise destroy the database and start fresh
        if (version != DATABASE_VERSION) {
            Log.w(LOG_TAG,
                    "Unable to perform incremental schema update, dropping old database");

            db.execSQL("DROP TABLE IF EXISTS " + Tables.SESSIONS);

            onCreate(db);
        }
    }

    /**
     * Purge the entire database from disk.
     */
    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
