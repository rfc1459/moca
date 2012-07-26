// @formatter:off
/*
 * TwitterFragment.java - UI for MOCA-related Twitter stream
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

package org.level28.android.moca.ui.twitter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import org.level28.android.moca.AsyncLoader;
import org.level28.android.moca.BuildConfig;
import org.level28.android.moca.R;
import org.level28.android.moca.json.JsonDeserializerException;
import org.level28.android.moca.json.TwitterSearchDeserializer;
import org.level28.android.moca.model.Tweet;
import org.level28.android.moca.model.TwitterSearchReply;
import org.level28.android.moca.service.SyncService;
import org.level28.android.moca.ui.ItemListAdapter;
import org.level28.android.moca.ui.ItemListFragment;
import org.level28.android.moca.ui.ItemView;
import org.level28.android.moca.ui.MainActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.github.kevinsawicki.http.HttpRequest;

/**
 * The awesome MOCA Twitter stream.
 * 
 * @author Matteo Panella
 */
public class TwitterFragment extends ItemListFragment<Tweet> {

    /** Tag used for logging */
    private static final String LOG_TAG = "TwitterFragment";

    /** Base URL for Twitter search API */
    private static final String API_BASE_URL = "http://search.twitter.com/search.json?q=";

    /** Base URL for Twitter web frontend */
    private static final String FRONTEND_BASE_URL = "https://twitter.com/";

    /**
     * Twitter search query &mdash; search for all tweets from last week
     * matching the following conditions:
     * <ol>
     * <li>tweet should contain "moca2012" or "moca_2012"
     * <li>tweet <b>MUST NOT</b> contain the exact sequence "expo moca" (some
     * latin-american event which was polluting results)
     * <li>tweet <b>MUST NOT</b> come from user @mocapress (somehow its blog url
     * matches "moca2012"...)
     * <li>tweet <b>MUST NOT</b> come from user @moca2012 (MEH.)
     * <li>retweets are not allowed
     * </ol>
     */
    private static final String TWITTER_QUERY = "(MOCA2012 OR MOCA_2012) -\"expo moca\" -from:moca2012 -from:mocapress exclude:retweets";

    /** Final Twitter search url */
    private String TWITTER_SEARCH_URL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assemble the final search url
        StringBuilder sb = new StringBuilder(API_BASE_URL);

        String encodedQuery = null;
        try {
            encodedQuery = URLEncoder.encode(TWITTER_QUERY, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // WTF?!?!?!
            Log.wtf(LOG_TAG, "Twitter search query encoding failed", e);
        }

        if (encodedQuery != null) {
            TWITTER_SEARCH_URL = sb.append(encodedQuery).toString();
        } else {
            TWITTER_SEARCH_URL = "";
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_tweets);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_menu, menu);
    }

    @Override
    public Loader<List<Tweet>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader<List<Tweet>>(getActivity()) {
            @Override
            public List<Tweet> loadInBackground() {
                if (BuildConfig.DEBUG) {
                    Log.v(LOG_TAG, "loadInBackground+");
                }

                List<Tweet> result = Collections.emptyList();

                try {
                    if ("".equals(TWITTER_SEARCH_URL)) {
                        throw new IOException(
                                "TWITTER_SEARCH_URL is empty, please nag the author at morpheus@level28.org");
                    }
                    TwitterSearchDeserializer jsonParser = new TwitterSearchDeserializer();

                    HttpRequest request = HttpRequest
                            .get(TWITTER_SEARCH_URL)
                            .userAgent(SyncService.buildUserAgent(getContext()))
                            .acceptGzipEncoding().uncompress(true);

                    if (request.ok()) {
                        TwitterSearchReply searchReply = jsonParser
                                .fromInputStream(request.stream());
                        result = searchReply.getResults();
                    }
                } catch (JsonDeserializerException e) {
                    Log.e(LOG_TAG,
                            "Internal error while parsing Twitter reply", e);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to fetch tweets", e);
                }

                if (BuildConfig.DEBUG) {
                    Log.v(LOG_TAG, "loadInBackground-");
                }
                return result;
            }
        };
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Tweet tweet = (Tweet) l.getItemAtPosition(position);
        startActivity(createTwitterIntent(tweet.getFromUser(), tweet.getId()));
    }

    private Intent createTwitterIntent(final CharSequence userName,
            final long tweetId) {
        StringBuilder sb = new StringBuilder(FRONTEND_BASE_URL);
        sb.append(userName).append("/status/").append(tweetId);
        return new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString()));
    }

    // @formatter:off
    @Override
    protected ItemListAdapter<Tweet, ? extends ItemView> createAdapter(
            List<Tweet> items) {
        final MainActivity activity = (MainActivity) getActivity();
        return new TwitterAdapter(R.layout.tweet_list_item,
                activity.getLayoutInflater(), items.toArray(new Tweet[items.size()]),
                activity.avatarLoader());
    }
    // @formatter:on
}
