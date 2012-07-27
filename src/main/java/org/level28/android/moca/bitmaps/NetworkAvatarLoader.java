// @formatter:off
/*
 * NetworkBitmapLoader.java - Load bitmaps off the network
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

package org.level28.android.moca.bitmaps;

import org.level28.android.moca.BuildConfig;
import org.level28.android.moca.R;
import org.level28.android.moca.service.SyncService;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

/**
 * Asynchronous loader for Twitter avatars
 * 
 * @author Matteo Panella
 */
public final class NetworkAvatarLoader extends AbstractBitmapLoader {
    private static final String LOG_TAG = "NetworkBitmapLoader";

    private static final String DISK_CACHE_SUBDIR = "bitmaps/twitter";

    /** Avatar corner radius in density-independent pixels */
    private static final float CORNER_RADIUS_IN_DIP = 3;

    private final float mCornerRadius;

    public NetworkAvatarLoader(Context context) {
        super(context, R.drawable.avatar_placeholder);
        float displayDensity = context.getResources().getDisplayMetrics().density;
        mCornerRadius = CORNER_RADIUS_IN_DIP * displayDensity;
    }

    @Override
    protected String getDiskCacheName() {
        return DISK_CACHE_SUBDIR;
    }

    /**
     * Load a missing avatar off the network, scale it down and return it with
     * its corners rounded.
     */
    @Override
    protected BitmapWrapper loadMissingBitmap(final Context context,
            final String url, final String key, final int width,
            final int height) throws Exception {
        HttpRequest request = HttpRequest.get(url)
                .userAgent(SyncService.buildUserAgent(context))
                .acceptGzipEncoding().uncompress(true);

        if (!request.ok() || request.contentLength() == 0) {
            // TODO: log the error
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "Error while fetching "
                        + url
                        + " - "
                        + (!request.ok() ? "Request failed"
                                : "Content-Length is zero"));
            }
            return null;
        }
        final byte[] respBytes = request.bytes();

        final Bitmap result = BitmapUtils.decodeSampledBitmapFromByteArray(
                respBytes, 0, respBytes.length, width, height);
        // Original bitmap will be recycled by the call to roundCorners
        return new BitmapWrapper(key, BitmapUtils.roundCorners(result,
                mCornerRadius));
    }
}
