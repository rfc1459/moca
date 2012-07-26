// @formatter:off
/*
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// @formatter:on

package org.level28.android.moca.bitmaps;

import org.level28.android.moca.BuildConfig;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Reference-counted wrapper for {@link Bitmap}s loaded off the network.
 * 
 * @author Chris Banes
 */
public class BitmapWrapper {
    private static final String LOG_TAG = "BitmapWrapper";

    private final String mKey;
    private final Bitmap mBitmap;

    /** How many {@code ImageView}s are referencing us? */
    private int mImageViewRefCount;

    /**
     * How many times do we appear inside the {@code LruCache}? (usually 0 or 1)
     */
    private int mCacheRefCount;

    public BitmapWrapper(final Bitmap bitmap) {
        this(null, bitmap);
    }

    public BitmapWrapper(final String key, final Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap cannot be null");
        }

        mBitmap = bitmap;
        mKey = key;
        mImageViewRefCount = 0;
        mCacheRefCount = 0;
    }

    /**
     * Is this wrapper still referenced by a cache entry?
     * 
     * @return true if a cache entry is holding a reference to this wrapper
     */
    public boolean isReferencedByCache() {
        return mCacheRefCount > 0;
    }

    /**
     * Is this wrapper directly referenced by at least one {@code ImageView}?
     * 
     * @return true if an {@code ImageView} is holding a reference to this
     *         wrapper
     */
    public boolean isBeingDisplayed() {
        return mImageViewRefCount > 0;
    }

    /**
     * Returns the raw {@link Bitmap} wrapped by this instance.
     * 
     * @return the raw Bitmap wrapped by this instance.
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * Returns the cache key for this bitmap.
     * 
     * @return a string representing the cache key for this bitmap
     */
    public String getKey() {
        return mKey;
    }

    /**
     * Check if the wrapped bitmap has been recycled.
     * 
     * @return true if the bitmap has been recycled, false otherwise
     */
    public boolean hasValidBitmap() {
        return !mBitmap.isRecycled();
    }

    /**
     * Manipulate the cache reference counter.
     * 
     * @param cached
     *            true if the bitmap is currently being cached, false otherwise
     * @see #setBeingUsed(boolean)
     */
    public void setCached(boolean cached) {
        if (cached) {
            mCacheRefCount++;
        } else {
            mCacheRefCount--;
        }
        checkRefCount();
    }

    /**
     * Manipulate the ImageView reference counter.
     * 
     * @param used
     *            true if the bitmap is currently being used, false otherwise
     * @see #setCached(boolean)
     */
    public void setBeingUsed(boolean used) {
        if (used) {
            mImageViewRefCount++;
        } else {
            mImageViewRefCount--;
        }
        checkRefCount();
    }

    /**
     * Check reference counters and recycle the bitmap (if needed).
     */
    private void checkRefCount() {
        if (mCacheRefCount <= 0 && mImageViewRefCount <= 0 && hasValidBitmap()) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "Recycling bitmap with key: "
                        + (mKey == null ? "(null)" : mKey));
            }
            mBitmap.recycle();
        }
    }
}
