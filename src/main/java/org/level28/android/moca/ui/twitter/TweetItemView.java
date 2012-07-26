// @formatter:off
/*
 * TweetItemView.java - view holder for tweets
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

import org.level28.android.moca.R;
import org.level28.android.moca.ui.CacheableImageView;
import org.level28.android.moca.ui.ItemView;

import android.view.View;
import android.widget.TextView;

/**
 * View holder for tweets.
 * 
 * @author Matteo Panella
 */
public class TweetItemView extends ItemView {

    /**
     * Sender profile image (cacheable)
     */
    public final CacheableImageView avatar;

    /**
     * Sender public name
     */
    public final TextView userName;

    /**
     * Sender handle
     */
    public final TextView userHandle;

    /**
     * Message timestamp (relative)
     */
    public final TextView timestamp;

    /**
     * Message contents
     */
    public final TextView contents;

    public TweetItemView(View view) {
        super(view);

        avatar = (CacheableImageView) view.findViewById(R.id.tweetProfileImage);
        userName = textView(view, R.id.tweetUserName);
        userHandle = textView(view, R.id.tweetUserHandle);
        timestamp = textView(view, R.id.tweetTimestamp);
        contents = textView(view, R.id.tweetContents);
    }
}
