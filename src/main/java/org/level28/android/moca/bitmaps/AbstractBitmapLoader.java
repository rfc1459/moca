// @formatter:off
/*
 * AbstractBitmapLoader.java - generic asynchronous loader for bitmaps
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

import static android.graphics.Bitmap.CompressFormat.PNG;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.level28.android.moca.BuildConfig;
import org.level28.android.moca.MocaApp;
import org.level28.android.moca.R;
import org.level28.android.moca.ui.CacheableImageView;
import org.level28.android.moca.util.SafeAsyncTask;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.DiskLruCache;

/**
 * Generic asynchronous loader for bitmaps.
 * 
 * @author Matteo Panella
 */
public abstract class AbstractBitmapLoader {
    private static final String LOG_TAG = "AbstractBitmapLoader";

    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10;
    private static final int DISK_CACHE_VERSION = 1;
    private static final int HASH_LENGTH = 40;

    /** L1 bitmap cache: memory */
    private final BitmapLruCache mMemoryCache;

    /** L2 bitmap cache: sdcard */
    private final DiskLruCache mDiskCache;

    /** Placeholder drawable */
    private final Drawable mPlaceHolderDrawable;

    /** Parent context */
    protected final Context mContext;

    protected boolean mReleased;

    public AbstractBitmapLoader(Context context, int placeholderResId) {
        mContext = context;

        // Get a reference for the global L1 memory cache
        mMemoryCache = MocaApp.getApplication(context).getBitmapCache();

        // Instantiate the L2 cache
        File cacheDir = getCacheDir(context, getDiskCacheName());
        if (!cacheDir.isDirectory()) {
            cacheDir.mkdirs();
        }

        DiskLruCache diskCache;
        try {
            diskCache = DiskLruCache.open(cacheDir, DISK_CACHE_VERSION, 1,
                    DISK_CACHE_SIZE);
        } catch (IOException e) {
            Log.w(LOG_TAG, "Error while creating L2 bitmap cache", e);
            diskCache = null;
        }
        mDiskCache = diskCache;

        mPlaceHolderDrawable = context.getResources().getDrawable(
                placeholderResId);
        mReleased = false;
    }

    /**
     * Get the path for the L2 cache.
     */
    protected abstract String getDiskCacheName();

    /**
     * Evict all elements from the L1 cache and close the L2 cache.
     */
    public final void release(final boolean evictAll) {
        synchronized (this) {
            checkState(!mReleased, "BitmapLoader already released");
            mReleased = true;
            // From now on, every other thread still trying to access this
            // loader will fail with an IllegalStateException.
            // The rest of this method is either thread-safe or synchronized
            // using other objects as monitors.
        }

        if (evictAll) {
            // Evict all elements from the L1 cache (thread-safe)
            mMemoryCache.evictAll();
        }

        if (mDiskCache != null) {
            try {
                // Close the L2 cache (synchronized on the cache itself)
                mDiskCache.close();
            } catch (IOException e) {
                // Swallow the exception
            }
        }
    }

    /**
     * Load an avatar for the given view.
     * <p>
     * If the avatar is available in the memory cache it will be assigned
     * immediately to the view, otherwise a placeholder will be displayed and a
     * background job will be launched to handle its retrieval.
     * 
     * @param view
     *            the {@link CacheableImageView} which will display the avatar
     * @param url
     *            the URL from which the avatar can be retrieved
     */
    public void load(final CacheableImageView view, final String url) {
        checkNotNull(view, "ImageView may not be null");

        if (url == null) {
            setImage(mPlaceHolderDrawable, view);
            return;
        }

        // Get all parameters required to build the cache key
        final int width = view.getWidth();
        final int height = view.getHeight();
        final String cacheKey = getBitmapKey(url, width, height);

        BitmapWrapper loadedImage;
        try {
            loadedImage = getBitmapFromL1Cache(cacheKey);
        } catch (IllegalStateException e) {
            // load() called after the loader has been finalized.
            // The right thing to do would be to check for finalization as a
            // precondition and propagate the IllegalStateException up the
            // stack, but this would exacerbate a rare race condition between
            // image loading and activity termination.
            setImage(mPlaceHolderDrawable, view);
            return;
        }
        if (loadedImage != null && loadedImage.hasValidBitmap()) {
            // L1 cache hit - we're done
            setImage(loadedImage, view);
        } else {
            // L1 cache miss, go in background
            setImage(mPlaceHolderDrawable, view, cacheKey);

            FetchBitmapTask task = new FetchBitmapTask(mContext, view, url,
                    cacheKey);
            task.execute();
        }
    }

    /** @hide */
    static void setImage(final Drawable image, final CacheableImageView view) {
        setImage(image, view, null);
    }

    /** @hide */
    static void setImage(final Drawable image, final CacheableImageView view,
            Object tag) {
        view.setImageDrawable(image);
        view.setTag(R.id.iv_avatar_tag, tag);
    }

    /** @hide */
    static void setImage(final BitmapWrapper image,
            final CacheableImageView view) {
        setImage(image, view, null);
    }

    /** @hide */
    static void setImage(final BitmapWrapper image,
            final CacheableImageView view, Object tag) {
        view.setImageCachedBitmap(image);
        view.setTag(R.id.iv_avatar_tag, tag);
    }

    private BitmapWrapper getBitmapFromL1Cache(String key) {
        synchronized (this) {
            checkState(!mReleased, "BitmapLoader has been released");
        }
        return mMemoryCache.get(key);
    }

    /** @hide */
    final BitmapWrapper getBitmapFromL2Cache(final String key) {
        synchronized (this) {
            checkState(!mReleased, "BitmapLoader has been released");
        }

        DiskLruCache.Snapshot cacheSnapshot = null;
        try {
            // Do we have an L2 cache at all?
            if (mDiskCache != null) {
                cacheSnapshot = mDiskCache.get(key);
                // Do we have this avatar stored?
                if (cacheSnapshot != null) {
                    // Indeed we do
                    return new BitmapWrapper(key,
                            BitmapFactory.decodeStream(cacheSnapshot
                                    .getInputStream(0)));
                }
            }
        } catch (IOException e) {
            // I/O errors with L2 cache *CAN* and *WILL* occur, swallow them
            // silently and fall back to network loading
        } finally {
            if (cacheSnapshot != null) {
                cacheSnapshot.close();
            }
        }
        return null;
    }

    /** @hide */
    void addBitmapToL1Cache(BitmapWrapper bitmapWrapper) {
        synchronized (this) {
            if (mReleased) {
                // Do not log the error
                return;
            }
        }
        mMemoryCache.put(bitmapWrapper);
    }

    /** @hide */
    void addBitmapToL2Cache(BitmapWrapper bitmapWrapper) {
        synchronized (this) {
            if (mReleased) {
                // Do not log the error
                return;
            }
        }

        final String key = bitmapWrapper.getKey();

        if (mDiskCache != null) {
            try {
                DiskLruCache.Editor l2CacheEditor = mDiskCache.edit(key);
                if (l2CacheEditor != null) {
                    OutputStream cacheStream = l2CacheEditor.newOutputStream(0);
                    bitmapWrapper.getBitmap().compress(PNG, 100, cacheStream);
                    cacheStream.flush();
                    cacheStream.close();
                    l2CacheEditor.commit();
                }
            } catch (IOException e) {
                Log.w(LOG_TAG, "Error while storing bitmap to disk cache", e);
            }
        }
    }

    /** Task for fetching avatars off the network */
    private class FetchBitmapTask extends SafeAsyncTask<BitmapWrapper> {
        private final Context mContext;

        private final WeakReference<CacheableImageView> mImageView;

        private final String mUrl;
        private final String mKey;
        private final int mWidth;
        private final int mHeight;

        private FetchBitmapTask(Context context, CacheableImageView view,
                String url, String key) {
            // Use the default single-threaded executor so that multiple fetches
            // are properly serialized
            super(SINGLE_THREAD_EXECUTOR);

            // Use the application context so we don't crap out if the activity
            // is disposed
            mContext = context.getApplicationContext();

            // Use a weak reference so that we don't keep ghosts lying around
            mImageView = new WeakReference<CacheableImageView>(view);

            mUrl = url;
            mKey = key;
            mWidth = view.getWidth();
            mHeight = view.getHeight();
        }

        @Override
        public BitmapWrapper call() throws Exception {
            if (TextUtils.isEmpty(mUrl) || TextUtils.isEmpty(mKey)) {
                return null;
            }

            // Try L2 cache first
            BitmapWrapper cachedBitmap;
            try {
                cachedBitmap = getBitmapFromL2Cache(mKey);
            } catch (IllegalStateException e) {
                // Prevent a stupid race condition between application
                // termination and background loading from polluting the log
                return null;
            }
            if (cachedBitmap != null) {
                return cachedBitmap;
            }

            // Tough luck, retrieve it
            cachedBitmap = loadMissingBitmap(mContext, mUrl, mKey, mWidth,
                    mHeight);
            if (cachedBitmap != null) {
                addBitmapToL2Cache(cachedBitmap);
            }
            return cachedBitmap;
        }

        @Override
        protected void onSuccess(final BitmapWrapper result) throws Exception {
            if (result != null) {
                // Promote the bitmap to L1 cache
                addBitmapToL1Cache(result);
                // Check if the view has been recycled
                final CacheableImageView view = mImageView.get();
                if (view != null
                        && mKey.equals(view.getTag(R.id.iv_avatar_tag))) {
                    setImage(result, view);
                }
            }
        }

        @Override
        protected void onInterrupted(Exception e) {
            // Swallow silently any interruption
        }

        @Override
        protected void onException(Exception e) throws RuntimeException {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "Avatar load failed", e);
            }
        }
    }

    /**
     * Load asynchronously a missing image.
     * <p>
     * <b>BE EXTRA CAREFUL</b>: this method runs inside an {@code AsyncTask},
     * don't do anything stupid like accessing the UI or referring to non-final
     * fields.
     * 
     * @param context
     *            context used by the {@code AsyncTask}
     * @param url
     *            url of the image that should be loaded
     * @param key
     *            cache key
     * @param width
     *            requested image width
     * @param height
     *            requested image height
     */
    protected abstract BitmapWrapper loadMissingBitmap(final Context context,
            final String url, final String key, final int width,
            final int height) throws Exception;

    private static File getCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use
        // external cache dir otherwise use internal cache dir
        boolean externalStorageAvailable = Environment.MEDIA_MOUNTED
                .equals(Environment.getExternalStorageState());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            // Froyo does not support isExternalStorageRemovable
            externalStorageAvailable |= !isExternalStorageRemovable();
        }
        final String cachePath = externalStorageAvailable ? context
                .getExternalCacheDir().getPath() : context.getCacheDir()
                .getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    // Can you spell "fragmentation"?
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static boolean isExternalStorageRemovable() {
        return Environment.isExternalStorageRemovable();
    }

    /**
     * Obtain the cache key for a given avatar - defined as
     * {@code SHA1(URL::WxH)}
     */
    private static String getBitmapKey(final String url, final int width,
            final int height) {
        final String finalKey = new StringBuilder(url).append("::")
                .append(width).append('x').append(height).toString();
        byte[] digested;

        try {
            digested = MessageDigest.getInstance("SHA-1").digest(
                    finalKey.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.wtf(LOG_TAG, "The runtime environment does not support UTF-8", e);
            return null;
        } catch (NoSuchAlgorithmException e) {
            Log.wtf(LOG_TAG, "The runtime environment does not support SHA-1", e);
            return null;
        }

        String hashed = new BigInteger(1, digested).toString(16);
        final int padding = HASH_LENGTH - hashed.length();
        if (padding == 0) {
            return hashed;
        }

        char[] zeros = new char[padding];
        Arrays.fill(zeros, '0');
        return new StringBuilder(HASH_LENGTH).append(zeros).append(hashed)
                .toString();
    }
}
