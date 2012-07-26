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

import java.util.Map.Entry;

import org.level28.android.moca.util.LruCache;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;

/**
 * Specialized {@link LruCache} for bitmaps.
 * 
 * @author Chris Banes
 */
public class BitmapLruCache extends LruCache<String, BitmapWrapper> {

    static final float DEFAULT_CACHE_SIZE = 1f / 8f;

    private static final float MAX_CACHE_SIZE = 1f;
    private static final int MEGABYTE = 1024 * 1024;

    public BitmapLruCache(Context context) {
        this(context, DEFAULT_CACHE_SIZE);
    }

    public BitmapLruCache(Context context, float percentageOfHeap) {
        this(Math.round(MEGABYTE * getHeapSize(context)
                * Math.min(percentageOfHeap, MAX_CACHE_SIZE)));
    }

    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, BitmapWrapper value) {
        if (value.hasValidBitmap()) {
            Bitmap bitmap = value.getBitmap();
            // Bitmap.getByteCount() is API Level 12+
            return bitmap.getRowBytes() * bitmap.getHeight();
        } else {
            return 0;
        }
    }

    /**
     * Convenience wrapper around {@link LruCache#put(String, BitmapWrapper)}
     */
    public BitmapWrapper put(final BitmapWrapper newValue) {
        return put(newValue.getKey(), newValue);
    }

    @Override
    public BitmapWrapper put(String key, BitmapWrapper value) {
        // Increment the cache reference counter
        value.setCached(true);
        return super.put(key, value);
    }

    @Override
    protected void entryRemoved(boolean evicted, String key,
            BitmapWrapper oldValue, BitmapWrapper newValue) {
        // Decrement the cache reference counter
        if (oldValue != null) {
            oldValue.setCached(false);
        }
    }

    /**
     * This method iterates through the cache and removes any Bitmap entries
     * which are not currently being displayed. A good place to call this would
     * be from {@link android.app.Application#onLowMemory()
     * Application.onLowMemory()}.
     */
    public void trimMemory() {
        for (Entry<String, BitmapWrapper> entry : snapshot().entrySet()) {
            BitmapWrapper value = entry.getValue();
            if (value == null || !value.isBeingDisplayed()) {
                remove(entry.getKey());
            }
        }
    }

    private static int getHeapSize(Context context) {
        return ((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    }
}
