// @formatter:off
/*
 * SessionHelper.java - synchronization helper for sessions
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
import java.util.List;
import java.util.Map;

import org.level28.android.moca.json.JsonDeserializerException;
import org.level28.android.moca.json.ScheduleDeserializer;
import org.level28.android.moca.model.Session;
import org.level28.android.moca.provider.ScheduleContract.Sessions;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

/**
 * Synchronization helper for sessions.
 * 
 * @author Matteo Panella
 */
class SessionHelper {
    private final String mUrl;
    private final String mUserAgent;
    private final ContentResolver mContentResolver;
    private final long now;

    SessionHelper(final String url, final String userAgent,
            final ContentResolver contentResolver) {
        mUrl = url;
        mUserAgent = userAgent;
        mContentResolver = contentResolver;
        now = System.currentTimeMillis();
    }

    /**
     * Perform session synchronization between local SQLite database and TMA-1
     * sessions API.
     */
    List<ContentProviderOperation> synchronizeSessions() throws IOException,
            JsonDeserializerException {
        final ArrayList<ContentProviderOperation> sessionsBatch = Lists
                .newArrayList();

        // Get a snapshot of all sessions stored in the database
        final Map<String, Session> localSessions = getSessionsSnapshot();
        // Ask the TMA-1 server for updated session data
        final Map<String, Session> remoteSessions = getRemoteSessions();

        if (!remoteSessions.isEmpty()) {
            // Perform the update only if we got a non-empty reply from the
            // TMA-1 server
            final MapDifference<String, Session> diff = Maps.difference(
                    localSessions, remoteSessions);

            // @formatter:off
            /*
             * Now diff contains a nice "patch" that should be transformed into a
             * batch of ContentProviderOperation.
             *
             * Namely:
             *  diff.entriesDiffering()   -> entries that should be updated with new values
             *  diff.entriesOnlyOnLeft()  -> entries that should be removed
             *  diff.entriesOnlyOnRight() -> entries that should be added
             */
            // @formatter:on
            sessionsBatch.addAll(createUpdateOps(diff.entriesDiffering()));
            sessionsBatch.addAll(createDeleteOps(diff.entriesOnlyOnLeft()));
            sessionsBatch.addAll(createInsertOps(diff.entriesOnlyOnRight()));
        }

        return sessionsBatch;
    }

    /**
     * Create a batch of UPDATE requests for sessions with updated values.
     */
    private List<ContentProviderOperation> createUpdateOps(
            Map<String, ValueDifference<Session>> entriesDiffering) {
        final ArrayList<ContentProviderOperation> updateBatch = Lists
                .newArrayList();

        for (String sessionId : entriesDiffering.keySet()) {
            final ValueDifference<Session> delta = entriesDiffering
                    .get(sessionId);
            final Session newSession = delta.rightValue();

            updateBatch.add(ContentProviderOperation
                    .newUpdate(Sessions.CONTENT_URI)
                    .withSelection(Sessions.SESSION_ID + "=?",
                            new String[] { sessionId })
                    .withValue(Sessions.UPDATED, now)
                    .withValue(Sessions.SESSION_TITLE, newSession.getTitle())
                    .withValue(Sessions.SESSION_DAY, newSession.getDay())
                    .withValue(Sessions.SESSION_START,
                            newSession.getStartTime())
                    .withValue(Sessions.SESSION_END, newSession.getEndTime())
                    .withValue(Sessions.SESSION_HOSTS, newSession.getHosts())
                    .withValue(Sessions.SESSION_LANG,
                            newSession.getLang().toString())
                    .withValue(Sessions.SESSION_ABSTRACT,
                            newSession.getSessionAbstract()).build());
        }

        return updateBatch;
    }

    /**
     * Create a batch of DELETE requests for stale sessions.
     */
    private List<ContentProviderOperation> createDeleteOps(
            Map<String, Session> staleSessions) {
        final ArrayList<ContentProviderOperation> deleteBatch = Lists
                .newArrayList();

        for (String sessionId : staleSessions.keySet()) {
            deleteBatch.add(ContentProviderOperation
                    .newDelete(Sessions.CONTENT_URI)
                    .withSelection(Sessions.SESSION_ID + "=?",
                            new String[] { sessionId }).build());
        }

        return deleteBatch;
    }

    /**
     * Create a batch of INSERT requests for new sessions.
     */
    private List<ContentProviderOperation> createInsertOps(
            Map<String, Session> newSessions) {
        final ArrayList<ContentProviderOperation> insertBatch = Lists
                .newArrayList();

        for (Session session : newSessions.values()) {
            insertBatch.add(ContentProviderOperation
                    .newInsert(Sessions.CONTENT_URI)
                    .withValue(Sessions.SESSION_ID, session.getId())
                    .withValue(Sessions.UPDATED, now)
                    .withValue(Sessions.SESSION_TITLE, session.getTitle())
                    .withValue(Sessions.SESSION_DAY, session.getDay())
                    .withValue(Sessions.SESSION_START, session.getStartTime())
                    .withValue(Sessions.SESSION_END, session.getEndTime())
                    .withValue(Sessions.SESSION_HOSTS, session.getHosts())
                    .withValue(Sessions.SESSION_LANG,
                            session.getLang().toString())
                    .withValue(Sessions.SESSION_ABSTRACT,
                            session.getSessionAbstract()).build());
        }

        return insertBatch;
    }

    /**
     * Fetch current list of sessions off the network.
     */
    private ImmutableMap<String, Session> getRemoteSessions()
            throws IOException, JsonDeserializerException {
        final ImmutableMap.Builder<String, Session> mapBuilder = ImmutableMap
                .builder();

        ScheduleDeserializer jsonDeserializer = new ScheduleDeserializer();
        HttpRequest request = HttpRequest.get(mUrl).userAgent(mUserAgent)
                .acceptJson().acceptGzipEncoding().uncompress(true);

        if (request.ok()) {
            mapBuilder
                    .putAll(jsonDeserializer.fromInputStream(request.stream()));
        } else if (!request.notModified()) {
            // Anything that's not a 200 or a 304 should cause the
            // synchronization code to fail fast
            throw new IOException("Request failed: " + request.code() + " - "
                    + request.message());
        }

        return mapBuilder.build();
    }

    /**
     * Get a snapshot of all sessions currently stored inside the local
     * database.
     */
    private ImmutableMap<String, Session> getSessionsSnapshot() {
        final Cursor cursor = mContentResolver.query(Sessions.CONTENT_URI,
                LocalSessionsQuery.PROJECTION, null, null,
                Sessions.DEFAULT_SORT);
        final ImmutableMap.Builder<String, Session> mapBuilder = ImmutableMap
                .builder();

        // Do we have a valid cursor at all?
        if (cursor != null) {
            // Is the cursor empty?
            if (cursor.moveToFirst()) {
                Session session;
                String sessionId;
                do {
                    // Build a new session object and store it inside the map
                    session = new Session();
                    sessionId = cursor.getString(LocalSessionsQuery.ID);
                    session.setId(sessionId);
                    session.setTitle(cursor.getString(LocalSessionsQuery.TITLE));
                    session.setDay(cursor.getInt(LocalSessionsQuery.DAY));
                    session.setStartTime(cursor
                            .getLong(LocalSessionsQuery.START));
                    session.setEndTime(cursor.getLong(LocalSessionsQuery.END));
                    session.setHosts(cursor.getString(LocalSessionsQuery.HOSTS));
                    session.setLang(cursor.getString(LocalSessionsQuery.LANG));
                    session.setSessionAbstract(cursor
                            .getString(LocalSessionsQuery.ABSTRACT));
                    mapBuilder.put(sessionId, session);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return mapBuilder.build();
    }

    /**
     * Query parameters for local sessions.
     */
    private interface LocalSessionsQuery {
        /**
         * Attribute projection.
         */
        String[] PROJECTION = { Sessions.SESSION_ID, Sessions.SESSION_TITLE,
                Sessions.SESSION_DAY, Sessions.SESSION_START,
                Sessions.SESSION_END, Sessions.SESSION_HOSTS,
                Sessions.SESSION_LANG, Sessions.SESSION_ABSTRACT, };

        // Cursor column offsets
        int ID = 0;
        int TITLE = 1;
        int DAY = 2;
        int START = 3;
        int END = 4;
        int HOSTS = 5;
        int LANG = 6;
        int ABSTRACT = 7;
    }
}
