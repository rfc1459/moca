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

import org.level28.android.moca.R;
import org.level28.android.moca.provider.ScheduleContract.Sessions;
import org.level28.android.moca.util.ViewUtils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
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
import android.view.animation.AnimationUtils;
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

    private boolean isHoneycomb;

    private CursorAdapter mAdapter;

    // The usual suspects
    private ListView mListView;
    private TextView mEmptyView;
    private ProgressBar mProgressBar;

    private boolean mListShown;

    /**
     * Content observer for sessions.
     * <p>
     * This little object handles the magic behind-the-scenes notifications
     * coming from the content provider, refreshing the list as soon as the
     * underlying dataset has changed.
     */
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
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

        reloadFromArguments(savedInstanceState);
    }

    private void reloadFromArguments(Bundle savedInstanceState) {
        // Release previous adapter
        setListAdapter(null);

        // TODO: honor URI in fragment arguments

        // Create and bind new list adapter
        mAdapter = new SessionsAdapter(getActivity());

        // Fire background loading of sessions
        getLoaderManager().restartLoader(SessionsQuery._TOKEN,
                savedInstanceState, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // FIXME: localize this string
        setEmptyText("No events?!?!");

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

    public void setListAdapter(CursorAdapter adapter) {
        mAdapter = adapter;
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO: extract content uri from arguments
        // TODO: perform day-based selection
        Loader<Cursor> loader = null;
        if (id == SessionsQuery._TOKEN) {
            loader = new CursorLoader(getActivity(), Sessions.CONTENT_URI,
                    SessionsQuery.PROJECTION, null, null, Sessions.DEFAULT_SORT);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!isUsable()) {
            return;
        }

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
        // This space intentionally left blank
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO
    }

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
        if (view != null)
            if (animate)
                view.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                        android.R.anim.fade_in));
            else
                view.clearAnimation();
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
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Retrieve the view holder
            final SessionItemView viewHolder = (SessionItemView) view.getTag();
            viewHolder.title.setText(cursor.getString(SessionsQuery.TITLE));
            viewHolder.host.setText(cursor.getString(SessionsQuery.HOSTS));
            final CharSequence startTime = DateUtils.formatDateTime(mContext,
                    cursor.getLong(SessionsQuery.START),
                    DateUtils.FORMAT_SHOW_TIME);
            viewHolder.time.setText(startTime);
        }
    }

    /**
     * Little neat holder for our Cursor-related constants.
     */
    @SuppressWarnings("unused")
    private interface SessionsQuery {
        /**
         * Loader token.
         */
        int _TOKEN = 1;

        /**
         * Projection used by the ContentProvider.
         */
        String[] PROJECTION = { BaseColumns._ID, Sessions.SESSION_ID,
                Sessions.SESSION_TITLE, Sessions.SESSION_DAY,
                Sessions.SESSION_START, Sessions.SESSION_HOSTS,
                Sessions.SESSION_LANG, };

        // Column offsets
        int _ID = 0;
        int SESSION_ID = 1;
        int TITLE = 2;
        int DAY = 3;
        int START = 4;
        int HOSTS = 5;
        int LANG = 6;
    }
}
