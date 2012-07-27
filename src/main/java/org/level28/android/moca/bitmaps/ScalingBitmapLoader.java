// @formatter:off
/*
 * ScalingBitmapLoader.java - asynchronous scaling bitmap loader
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

import org.level28.android.moca.ui.CacheableImageView;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Asynchronous bitmap loader for local resources.
 * 
 * @author Matteo Panella
 */
public final class ScalingBitmapLoader extends AbstractBitmapLoader {
    public static final String LOG_TAG = "ScalingBitmapLoader";

    public static final String DISK_CACHE_SUBDIR = "bitmaps/local";

    public ScalingBitmapLoader(Context context, int placeholderResId) {
        super(context, placeholderResId);
    }

    @Override
    protected String getDiskCacheName() {
        return DISK_CACHE_SUBDIR;
    }

    /**
     * Asynchronously load a bitmap resource.
     * 
     * @param view
     *            the view to which the bitmap should be bound
     * @param resId
     *            resource id for the bitmap
     */
    public void load(final CacheableImageView view, final int resId) {
        load(view, Integer.toHexString(resId));
    }

    /**
     * Load and scale a local bitmap resource.
     */
    @Override
    protected BitmapWrapper loadMissingBitmap(Context context, String url,
            String key, int width, int height) throws Exception {
        final int bitmapResId = Integer.valueOf(url, 16);

        // Load directly from resources
        final Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromResource(
                context.getResources(), bitmapResId, width, height);

        if (bitmap == null) {
            return null;
        } else {
            return new BitmapWrapper(key, bitmap);
        }
    }
}
