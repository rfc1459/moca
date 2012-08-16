// @formatter:off
/*
 * SessionListFragment.java - fragment for Sessions list
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

import static android.widget.Adapter.NO_SELECTION;
import static android.widget.AdapterView.INVALID_ROW_ID;
import static com.google.common.base.Preconditions.checkArgument;
import static org.level28.android.moca.util.ActivityUtils.fragmentArgumentsToIntent;
import static org.level28.android.moca.util.ActivityUtils.intentToFragmentArguments;

import org.level28.android.moca.R;
import org.level28.android.moca.provider.ScheduleContract.Sessions;
import org.level28.android.moca.util.ViewUtils;
import org.level28.android.moca.widget.ScheduleItemLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Fragment for session list.
 * <p>
 * Since this fragment uses the (insane) ContentProvider paradigm, it cannot
 * inherit from {@link org.level28.android.moca.ui.ItemListFragment
 * ItemListFragment}.
 * 
 * @author Matteo Panella
 */
public class SessionListFragment extends SherlockFragment implements
        LoaderCallbacks<Cursor> {

    /**
     * Listener for session selected events.
     */
    interface OnSessionSelectedListener {
        /**
         * Called whenever a session is selected.
         * 
         * @param sessionId
         *            the UUID of the session
         * @param listItemId
         *            the RowID of the session returned by SQLite
         */
        public void onSessionSelected(final String sessionId,
                final long listItemId);
    }

    private boolean isHoneycomb;

    private CursorAdapter mAdapter;

    private OnSessionSelectedListener mListener;

    // The usual suspects
    private ListView mListView;
    private TextView mEmptyView;
    private ProgressBar mProgressBar;

    private boolean mListShown;

    private long mCurrentRowId = INVALID_ROW_ID;

    private boolean mDualPane = false;
    private boolean mDataValid = false;

    /**
     * Content observer for sessions.
     * <p>
     * This little object handles the magic behind-the-scenes notifications
     * coming from the content provider, refreshing the list as soon as the
     * underlying dataset has changed.
     */
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (!isUsable()) {
                return;
            }

            final Loader<Cursor> loader = getLoaderManager().getLoader(
                    SessionsQuery._TOKEN);
            if (loader != null) {
                loader.forceLoad();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reloadFromArguments(getArguments());
    }

    /**
     * Reload contents from the given fragment arguments.
     * 
     * @param arguments
     *            a bundle containing the target URI for this fragment
     */
    private void reloadFromArguments(Bundle arguments) {
        // Release previous adapter
        setListAdapter(null);

        // Extract sessions Uri
        final Intent intent = fragmentArgumentsToIntent(arguments);
        final Uri sessionsUri = intent.getData();

        // Create and bind new list adapter
        mAdapter = new SessionsAdapter(getActivity());
        // setListAdapter(mAdapter);

        // Fire background loading of sessions
        if (sessionsUri != null) {
            getLoaderManager().restartLoader(SessionsQuery._TOKEN, arguments,
                    this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getActivity().getResources().getText(R.string.no_events));

        // If we have data waiting for us, display the list
        if (mAdapter != null && !mAdapter.isEmpty()) {
            setListShown(true, false);
        }

        // Check if we're running on Honeycomb or later
        isHoneycomb = getActivity().getResources().getBoolean(
                R.bool.isHoneycomb);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This is where the observer magic happens: register our little voyeur
        // with the activity's content resolver
        activity.getContentResolver().registerContentObserver(
                Sessions.CONTENT_URI, true, mObserver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Release the observer when we're detaching from the host activity
        getActivity().getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Resources res = view.getResources();

        // Get a hold on all view elements
        mListView = (ListView) view.findViewById(android.R.id.list);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressLoading);
        mEmptyView = (TextView) view.findViewById(android.R.id.empty);

        // Backward compatibility sucks
        if (!isHoneycomb) {
            // Force the Holo Light background color for this fragment on
            // gingerbread and lower
            view.setBackgroundColor(res.getColor(R.color.background_holo_light));

            // If we're running on Gingerbread or lower we have to override the
            // divider drawable for consistency
            mListView.setDivider(res.getDrawable(R.drawable.list_divider));
        }

        // Set divider height to 2dp
        mListView.setDividerHeight(2);

        // Set item click listener
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                onListItemClick((ListView) parent, view, position, id);
            }
        });

        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        mListShown = false;
        mEmptyView = null;
        mProgressBar = null;
        mListView = null;

        super.onDestroyView();
    }

    /**
     * Bind a new {@link CursorAdapter} to this fragment.
     * 
     * @param adapter
     *            the new adapter
     */
    public void setListAdapter(CursorAdapter adapter) {
        mAdapter = adapter;
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
    }

    /**
     * Bind a new {@link OnSessionSelectedListener} to this fragment.
     * 
     * @param listener
     *            the new listener
     */
    public void setOnSessionSelectedListener(OnSessionSelectedListener listener) {
        mListener = listener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Get the URI for a Session cursor (most likely a day-based URI)
        final Intent intent = fragmentArgumentsToIntent(args);
        final Uri sessionsUri = intent.getData();
        Loader<Cursor> loader = null;
        if (id == SessionsQuery._TOKEN) {
            loader = new CursorLoader(getActivity(), sessionsUri,
                    SessionsQuery.PROJECTION, null, null, Sessions.DEFAULT_SORT);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!isUsable()) {
            return;
        }

        // We do have valid data now
        mDataValid = true;

        // The list adapter should be (re)set here...
        setListAdapter(mAdapter);

        final int token = loader.getId();
        if (token == SessionsQuery._TOKEN) {
            mAdapter.changeCursor(cursor);
            showList();
        } else {
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // We don't have valid data anymore
        mDataValid = false;
    }

    /**
     * Callback for ListView item click events.
     */
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (!isUsable()) {
            return;
        }

        // Extract the session UUID
        final Cursor cursor = (Cursor) mAdapter.getItem(position);
        final String sessionId = cursor.getString(SessionsQuery.SESSION_ID);

        if (mListener != null) {
            // We have an event listener, send it the session UUID and RowID
            mListener.onSessionSelected(sessionId, id);
        }
    }

    /**
     * Change list choice mode.
     * 
     * @param selectable
     *            {@code true} if the parent activity is operating in dual-pane
     *            mode and the list should be selectable, {@code false}
     *            otherwise
     */
    public void setSelectable(final boolean selectable) {
        if (selectable) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mDualPane = true;
        } else {
            mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            mDualPane = false;
        }
    }

    /**
     * Change the currently checked row in the list.
     * <p>
     * This method is only meaningful in dual-pane mode and will defer its
     * execution until there is valid data.
     * 
     * @param selectedRowId
     *            the RowID of the entry that should be checked
     */
    void setSelectedId(final long selectedRowId) {
        mCurrentRowId = selectedRowId;
        // Perform the actual selection only iff we have valid data, otherwise
        // defer it after a successful load operation
        if (mDataValid) {
            setCheckedItem(selectedRowId);
            mCurrentRowId = INVALID_ROW_ID;
        }
    }

    /**
     * Carry out the actual row selection.
     * 
     * @param rowId
     *            the RowID of the entry that should be checked
     */
    private void setCheckedItem(final long rowId) {
        if (mListView != null && mDualPane) {
            int position = NO_SELECTION;
            // There is no way around this ugly loop :-(
            for (int i = 0; i < mAdapter.getCount(); i++) {
                if (mAdapter.getItemId(i) == rowId) {
                    position = i;
                    break;
                }
            }
            if (position != NO_SELECTION) {
                mListView.setItemChecked(position, true);
            }
        }
    }

    /**
     * Load all sessions scheduled for the given day.
     */
    void loadScheduleForDay(final int day) {
        // Better safe than sorry
        checkArgument(day >= 1 && day <= 3, "Day out of range: %s", day);

        if (!isUsable()) {
            return;
        }

        // Build new arguments for our loader
        final Bundle loaderArgs = intentToFragmentArguments(new Intent(
                Intent.ACTION_VIEW, Sessions.buildSessionsDayDirUri(day)));

        // Restart the loader
        reloadFromArguments(loaderArgs);
    }

    // Almost all of the following methods are direct copies of the ones in
    // ItemListFragment. Check that class for documentation.

    private SessionListFragment setEmptyText(CharSequence text) {
        if (mEmptyView != null) {
            mEmptyView.setText(text);
        }
        return this;
    }

    private void showList() {
        setListShown(true, isResumed());
    }

    private SessionListFragment fadeIn(final View view, final boolean animate) {
        ViewUtils.fadeIn(getActivity(), view, animate);
        return this;
    }

    private SessionListFragment show(final View view) {
        ViewUtils.setGone(view, false);
        return this;
    }

    private SessionListFragment hide(final View view) {
        ViewUtils.setGone(view, true);
        return this;
    }

    private SessionListFragment setListShown(final boolean shown,
            final boolean animate) {
        if (!isUsable()) {
            return this;
        }

        // Check for a pending checked item change operation
        if (mCurrentRowId != INVALID_ROW_ID) {
            setCheckedItem(mCurrentRowId);
            mCurrentRowId = INVALID_ROW_ID;
        }

        if (shown == mListShown) {
            if (shown) {
                // List has already been shown so hide/show the empty view with
                // no fade effect
                if (mAdapter == null || mAdapter.isEmpty()) {
                    hide(mListView).show(mEmptyView);
                } else {
                    hide(mEmptyView).show(mListView);
                }
            }
            return this;
        }

        mListShown = shown;

        if (shown) {
            if (mAdapter != null && !mAdapter.isEmpty()) {
                hide(mProgressBar).hide(mEmptyView).fadeIn(mListView, animate)
                        .show(mListView);
            } else {
                hide(mProgressBar).hide(mListView).fadeIn(mEmptyView, animate)
                        .show(mEmptyView);
            }
        } else {
            hide(mListView).hide(mEmptyView).fadeIn(mProgressBar, animate)
                    .show(mProgressBar);
        }

        return this;
    }

    private boolean isUsable() {
        return getActivity() != null;
    }

    /**
     * List adapter for sessions.
     */
    private class SessionsAdapter extends CursorAdapter {
        public SessionsAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = getActivity().getLayoutInflater().inflate(
                    R.layout.schedule_list_item, parent, false);
            // Use the Force, Luke... I mean, use a view holder to avoid costly
            // findViewById() calls in bindView()
            final SessionItemView tag = new SessionItemView(view);
            view.setTag(tag);
            if (!isHoneycomb) {
                // LinearLayout got dividers in Honeycomb, so we have to fake
                // them in Froyo and Gingerbread
                final View divider = view.findViewById(R.id.dividerCompat);
                ViewUtils.setGone(divider, false);
            }
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Retrieve the view holder
            final SessionItemView viewHolder = (SessionItemView) view.getTag();
            final long sessionStart = cursor.getLong(SessionsQuery.START);
            final long sessionEnd = cursor.getLong(SessionsQuery.END);
            final long now = System.currentTimeMillis();
            viewHolder.title.setText(cursor.getString(SessionsQuery.TITLE));
            viewHolder.host.setText(cursor.getString(SessionsQuery.HOSTS));
            final CharSequence startTime = DateUtils.formatDateTime(mContext,
                    sessionStart, DateUtils.FORMAT_SHOW_TIME);
            viewHolder.time.setText(startTime);

            // Set (or clear) current session
            if (view instanceof ScheduleItemLayout) {
                ((ScheduleItemLayout) view).setCurrent(now >= sessionStart
                        && now <= sessionEnd);
            }
        }
    }

    /**
     * Little neat holder for our Cursor-related constants.
     */
    private interface SessionsQuery {
        /**
         * Loader token.
         */
        int _TOKEN = 1;

        /**
         * Projection used by the ContentProvider.
         */
        String[] PROJECTION = { BaseColumns._ID, Sessions.SESSION_ID,
                Sessions.SESSION_TITLE, Sessions.SESSION_START,
                Sessions.SESSION_END, Sessions.SESSION_HOSTS, };

        // Column offsets
        // _ID has to be part of the projection, otherwise CursorAdapter will
        // crap out badly.
        @SuppressWarnings("unused")
        int _ID = 0;
        int SESSION_ID = 1;
        int TITLE = 2;
        int START = 3;
        int END = 4;
        int HOSTS = 5;
    }
}
