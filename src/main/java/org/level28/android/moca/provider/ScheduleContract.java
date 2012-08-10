// @formatter:off
/*
 * ScheduleContract.java - contract class for schedule content provider
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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for the schedule content provider.
 * <p>
 * Heavily inspired by the Google iosched app.
 * 
 * @author Matteo Panella
 */
public class ScheduleContract {

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that an entry
     * has never been updated, or doesn't exist yet.
     */
    public static final long UPDATED_NEVER = -2;

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that the last
     * update time is unknown, usually when inserted from a local file source.
     */
    public static final long UPDATED_UNKNOWN = -1;

    public interface SyncColumns {
        /** Last time this entry was updated or synchronized. */
        String UPDATED = "updated";
    }

    interface SessionColumns {
        /** Unique string identifying this session. */
        String SESSION_ID = "session_id";
        /** Title describing this session. */
        String SESSION_TITLE = "session_title";
        /** Session day */
        String SESSION_DAY = "session_day";
        /** Time when this session starts. */
        String SESSION_START = "session_start";
        /** Time when this session ends. */
        String SESSION_END = "session_end";
        /** Who's hosting this session. */
        String SESSION_HOSTS = "session_hosts";
        /** Session language. */
        String SESSION_LANG = "session_lang";
        /** Body of text explaining this session in detail. */
        String SESSION_ABSTRACT = "session_abstract";
    }

    public static final String CONTENT_AUTHORITY = "org.level28.android.moca";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
            + CONTENT_AUTHORITY);

    private static final String PATH_SESSIONS = "sessions";
    private static final String PATH_AT = "at";
    private static final String PATH_DAY = "day";

    /**
     * A session.
     */
    public static class Sessions implements SessionColumns, SyncColumns,
            BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SESSIONS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.moca.session";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.moca.session";

        /**
         * Default ORDER BY clause
         */
        public static final String DEFAULT_SORT = SESSION_START + " ASC,"
                + SESSION_TITLE + " COLLATE NOCASE ASC";

        public static final String AT_TIME_SELECTION = SESSION_START
                + " <= ? AND " + SESSION_END + " >= ?";

        public static String[] buildAtTimeSelectionArgs(long time) {
            final String timeString = String.valueOf(time);
            return new String[] { timeString, timeString };
        }

        public static final String ON_DAY_SELECTION = SESSION_DAY
                + " = ?";

        public static Uri buildSessionUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).build();
        }

        public static Uri buildSessionsAtDirUri(long time) {
            return CONTENT_URI.buildUpon().appendPath(PATH_AT)
                    .appendPath(String.valueOf(time)).build();
        }

        public static Uri buildSessionsDayDirUri(int day) {
            return CONTENT_URI.buildUpon().appendPath(PATH_DAY)
                    .appendPath(String.valueOf(day)).build();
        }

        public static String getSessionId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getSearchQuery(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    private ScheduleContract() {
        // Don't new me
    }
}
