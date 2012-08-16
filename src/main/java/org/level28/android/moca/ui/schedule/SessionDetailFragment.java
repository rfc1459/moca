// @formatter:off
/*
 * SessionDetailFragment.java - fragment for session details
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

package org.level28.android.moca.ui.schedule;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.level28.android.moca.util.ActivityUtils.fragmentArgumentsToIntent;
import static org.level28.android.moca.util.ActivityUtils.intentToFragmentArguments;

import java.util.Locale;

import org.level28.android.moca.R;
import org.level28.android.moca.model.Session.Language;
import org.level28.android.moca.provider.ScheduleContract.Sessions;
import org.level28.android.moca.util.ViewUtils;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Fragment for session details.
 * 
 * @author Matteo Panella
 */
public class SessionDetailFragment extends SherlockFragment implements
        LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = "SessionDetailFragment";

    private View mScheduleContainer;
    private View mEmptyText;

    private TextView mTime;
    private TextView mTitle;
    private TextView mHosts;
    private TextView mAbstract;
    private ScrollView mAbstractContainer;

    private Cursor mCursor = null;

    // Prevent the empty view from "flickering" when refreshing the dual-pane
    // layout
    private boolean mScheduleVisible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reloadFromArguments(getArguments());
    }

    /**
     * Reload contents from the given fragment arguments.
     * 
     * @param arguments
     *            a bundle containing the target session URI for this fragment
     */
    private void reloadFromArguments(Bundle arguments) {
        // Reset cursor
        resetCursor();

        // Extract the session URI (if any)
        final Uri sessionUri;
        if (arguments != null) {
            final Intent intent = fragmentArgumentsToIntent(arguments);
            sessionUri = intent.getData();
        } else {
            sessionUri = null;
        }

        // Do we have a valid session URI?
        if (sessionUri == null) {
            return;
        }

        // We do, start the loader
        getLoaderManager().restartLoader(SessionDetailQuery._TOKEN, arguments,
                this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Try to update the UI as soon as possible
        updateUi(false);
    }

    @Override
    public void onDestroy() {
        // Prevent cursors from leaking
        resetCursor();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.session_detail, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View containers
        mScheduleContainer = view.findViewById(R.id.sessionDetailsContainer);
        mEmptyText = view.findViewById(android.R.id.empty);

        // Perform view binding
        mTime = (TextView) view.findViewById(R.id.sessionTime);
        mTitle = (TextView) view.findViewById(R.id.sessionTitle);
        mHosts = (TextView) view.findViewById(R.id.sessionHosts);
        mAbstract = (TextView) view.findViewById(R.id.sessionAbstract);
        mAbstractContainer = (ScrollView) view
                .findViewById(R.id.sessionAbstractContainer);
    }

    @Override
    public void onDestroyView() {
        mTitle = null;
        mTime = null;
        mHosts = null;
        mAbstract = null;
        mAbstractContainer = null;

        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Extract session uri
        final Intent intent = fragmentArgumentsToIntent(args);
        final Uri sessionUri = intent.getData();
        Loader<Cursor> loader = null;
        if (id == SessionDetailQuery._TOKEN) {
            loader = new CursorLoader(getActivity(), sessionUri,
                    SessionDetailQuery.PROJECTION, null, null, null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!isUsable()) {
            return;
        }

        final int token = loader.getId();
        if (token == SessionDetailQuery._TOKEN) {
            resetCursor();
            mCursor = cursor;
            updateUi(isResumed());
        } else {
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This space intentionally left blank
    }

    /**
     * Update the fragment with a new session.
     * 
     * @param sessionId
     *            the UUID of the new session for which details should be
     *            displayed
     */
    void loadSessionDetails(final String sessionId) {
        if (!isUsable()) {
            return;
        }

        if (sessionId == null) {
            // We were asked to clear our contents, do it
            resetCursor();
            updateUi(isResumed());
        } else {
            // Rebuild new fragment arguments
            final Bundle loaderArgs = intentToFragmentArguments(new Intent(
                    Intent.ACTION_VIEW, Sessions.buildSessionUri(sessionId)));

            // Perform the actual reload
            reloadFromArguments(loaderArgs);
        }
    }

    /**
     * Check whether this fragment instance has a valid UI or not.
     * 
     * @return {@code true} if the fragment is currently attached to an
     *         activity, {@code false} otherwise
     */
    private boolean isUsable() {
        return getActivity() != null;
    }

    /**
     * Release the cursor currently held by this fragment.
     */
    private void resetCursor() {
        if (mCursor != null) {
            if (!mCursor.isClosed()) {
                mCursor.close();
            }
            mCursor = null;
        }
    }

    /**
     * Update the fragment UI whenever a cursor change happens
     */
    private void updateUi(final boolean animate) {
        if (!isUsable()) {
            return;
        }

        // Sanity check
        if (mCursor != null && !mCursor.isClosed() && mCursor.moveToFirst()) {
            final CharSequence startTime = DateUtils.formatDateTime(
                    getActivity(), mCursor.getLong(SessionDetailQuery.START),
                    DateUtils.FORMAT_SHOW_TIME);
            final CharSequence endTime = DateUtils.formatDateTime(
                    getActivity(), mCursor.getLong(SessionDetailQuery.END),
                    DateUtils.FORMAT_SHOW_TIME);

            final String timeSpec = new StringBuilder(startTime).append(" - ")
                    .append(endTime).toString();

            mTime.setText(timeSpec);
            // Set the language flag as a compound drawable of mTime.
            // This is a very common layout optimization which shaves a bit of
            // memory and *a lot* of CPU time required to compute the final
            // layout
            final int flagResId = getFlagResId(mCursor
                    .getString(SessionDetailQuery.LANG));
            mTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, flagResId);

            mTitle.setText(mCursor.getString(SessionDetailQuery.TITLE));
            mHosts.setText(mCursor.getString(SessionDetailQuery.HOSTS));

            // Check for abstract presence
            if (mCursor.isNull(SessionDetailQuery.ABSTRACT)) {
                // No abstract, hide it
                ViewUtils.setGone(mAbstractContainer, true);
            } else {
                // We have an abstract, display it!
                mAbstract.setText(mCursor
                        .getString(SessionDetailQuery.ABSTRACT));
                mAbstractContainer.scrollTo(0, 0);
                ViewUtils.setGone(mAbstractContainer, false);
            }
            // Display session details
            ViewUtils.setGone(mEmptyText, true);
            ViewUtils.fadeIn(getActivity(), mScheduleContainer, animate);
            ViewUtils.setGone(mScheduleContainer, false);
            mScheduleVisible = true;
        } else if (mScheduleVisible) {
            // Display the "select session" message
            ViewUtils.setGone(mScheduleContainer, true);
            ViewUtils.fadeIn(getActivity(), mEmptyText, animate);
            ViewUtils.setGone(mEmptyText, false);
            mScheduleVisible = false;
        }
    }

    /**
     * Extract the resource id for a language icon given the two-letter language
     * id.
     */
    private int getFlagResId(final String language) {
        // Precondition
        checkNotNull(language);
        Language lang = Language.IT;
        try {
            lang = Language.valueOf(language.toUpperCase(Locale.US));
        } catch (IllegalArgumentException e) {
            // Fall back to italian
            Log.e(LOG_TAG, "Invalid value for session language", e);
        }

        final int flagResId;
        switch (lang) {
        case EN:
            // Distinctions between en_US and en_UK are pointless, so stick with
            // the Union Jack.
            flagResId = R.drawable.ukflag;
            break;
        case IT:
        default:
            flagResId = R.drawable.itflag;
            break;
        }
        return flagResId;
    }

    /**
     * Constant holder for session detail query.
     */
    private interface SessionDetailQuery {
        /** Loader token. */
        int _TOKEN = 1;

        /** Attributes projection. */
        String[] PROJECTION = { Sessions.SESSION_START, Sessions.SESSION_END,
                Sessions.SESSION_TITLE, Sessions.SESSION_HOSTS,
                Sessions.SESSION_LANG, Sessions.SESSION_ABSTRACT, };

        // Column offsets
        int START = 0;
        int END = 1;
        int TITLE = 2;
        int HOSTS = 3;
        int LANG = 4;
        int ABSTRACT = 5;
    }
}
