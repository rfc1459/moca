// @formatter:off
/*
 * SimpleBitmapLoader.java - asynchronously load and cache a local bitmap resource
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

import java.lang.ref.WeakReference;

import org.level28.android.moca.MocaApp;
import org.level28.android.moca.R;
import org.level28.android.moca.ui.CacheableImageView;
import org.level28.android.moca.util.SafeAsyncTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

/**
 * Asynchronous caching loader for local bitmap resources.
 * <p>
 * Even though it shares a lot of code with {@link AbstractBitmapLoader}, it
 * does not inherit from it because this loader doesn't need an L2 cache.
 * 
 * @author Matteo Panella
 */
public final class SimpleBitmapLoader {
    /** Reference to the global bitmap memory cache */
    private final BitmapLruCache mCache;

    /** Placeholder */
    private final Drawable mPlaceholder;

    /** Parent context */
    private final Context mContext;

    public SimpleBitmapLoader(Context context) {
        mContext = context;
        mCache = MocaApp.getApplication(mContext).getBitmapCache();
        mPlaceholder = context.getResources().getDrawable(R.drawable.empty);
    }

    /**
     * Asynchronously load a local bitmap resource into the given view.
     * 
     * @param view the view which will display the bitmap
     * @param resId resource id for the bitmap
     */
    public void load(final CacheableImageView view, final int resId) {
        if (view == null) {
            throw new IllegalArgumentException("null ImageView");
        }

        final String key = new StringBuilder("local:").append(resId).toString();

        BitmapWrapper bitmap = mCache.get(key);
        if (bitmap != null && bitmap.hasValidBitmap()) {
            setImage(bitmap, view);
        } else {
            setImage(mPlaceholder, view, key);
            BitmapLoaderTask task = new BitmapLoaderTask(mContext, view, key,
                    resId);
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
        view.setTag(R.id.iv_banner, tag);
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
        view.setTag(R.id.iv_banner, tag);
    }

    private class BitmapLoaderTask extends SafeAsyncTask<BitmapWrapper> {
        private final Context mContext;

        private final WeakReference<CacheableImageView> mView;

        private final String mKey;
        private final int mResId;

        public BitmapLoaderTask(Context context, CacheableImageView view,
                String key, int resId) {
            // Use the default multi-threaded executor
            super(DEFAULT_EXECUTOR);

            mContext = context.getApplicationContext();
            mView = new WeakReference<CacheableImageView>(view);
            mKey = key;
            mResId = resId;
        }

        @Override
        public BitmapWrapper call() throws Exception {
            // Yes, it's THIS simple. We just need to decode the bitmap, but we
            // do it off the UI thread to prevent it from stalling
            final Bitmap bitmap = BitmapFactory.decodeResource(
                    mContext.getResources(), mResId);
            if (bitmap == null) {
                return null;
            } else {
                return new BitmapWrapper(mKey, bitmap);
            }
        }

        @Override
        protected void onSuccess(BitmapWrapper result) throws Exception {
            if (result != null) {
                mCache.put(result);
                final CacheableImageView view = mView.get();
                if (view != null && mKey.equals(view.getTag(R.id.iv_banner))) {
                    setImage(result, view);
                }
            }
        }
    }
}
