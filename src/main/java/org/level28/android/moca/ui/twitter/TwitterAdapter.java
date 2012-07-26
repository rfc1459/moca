// @formatter:off
/*
 * TwitterAdapter.java - list adapter for tweets
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

import org.level28.android.moca.bitmaps.NetworkAvatarLoader;
import org.level28.android.moca.model.Tweet;
import org.level28.android.moca.ui.ItemListAdapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;

/**
 * List adapter for tweets.
 * 
 * @author Matteo Panella
 */
public class TwitterAdapter extends ItemListAdapter<Tweet, TweetItemView> {

    private final NetworkAvatarLoader avatars;

    public TwitterAdapter(int viewId, LayoutInflater inflater,
            NetworkAvatarLoader avatars) {
        this(viewId, inflater, null, avatars);
    }

    public TwitterAdapter(int viewId, LayoutInflater inflater,
            Tweet[] elements, NetworkAvatarLoader avatars) {
        super(viewId, inflater, elements);
        this.avatars = avatars;
    }

    @Override
    protected void update(int position, TweetItemView view, Tweet item) {
        final long now = System.currentTimeMillis();
        final long ts = item.getCreatedAt().getTime();
        final CharSequence elapsed = DateUtils.getRelativeTimeSpanString(ts,
                now, DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE);

        view.userName.setText(item.getFromUserName());
        view.userHandle.setText("@" + item.getFromUser());
        view.timestamp.setText(elapsed);
        // Don't ask me why, I've seen it in the wild and it's utterly
        // undocumented...
        view.contents.setText(item.getText().replace("&lt;", "<")
                .replace("&gt;", ">"));
        avatars.load(view.avatar, item.getProfileImageUrl());
    }

    @Override
    protected TweetItemView createView(View view) {
        return new TweetItemView(view);
    }
}
