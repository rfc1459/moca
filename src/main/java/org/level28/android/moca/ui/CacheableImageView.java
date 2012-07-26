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

package org.level28.android.moca.ui;

import org.level28.android.moca.bitmaps.BitmapWrapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * {@link ImageView} subclass with caching support
 * 
 * @author Chris Banes
 */
public class CacheableImageView extends ImageView {

    private BitmapWrapper mDisplayedBitmapWrapper;

    public CacheableImageView(Context context) {
        this(context, null);
    }

    public CacheableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets a BitmapWrapper as content of this ImageView.
     * 
     * @param wrapper
     *            The wrapped bitmap to set
     */
    public void setImageCachedBitmap(final BitmapWrapper wrapper) {
        if (wrapper != null && wrapper.hasValidBitmap()) {
            wrapper.setBeingUsed(true);
            setImageDrawable(new BitmapDrawable(getResources(),
                    wrapper.getBitmap()));
        } else {
            setImageDrawable(null);
        }

        mDisplayedBitmapWrapper = wrapper;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        setImageCachedBitmap(new BitmapWrapper(bm));
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        resetCachedBitmap();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        resetCachedBitmap();
    }

    /**
     * Get the current cached bitmap wrapper (if any)
     * 
     * @return the BitmapWrapper currently bound with this ImageView
     */
    public BitmapWrapper getCachedBitmapWrapper() {
        return mDisplayedBitmapWrapper;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Clear the reference to the cached bitmap so that it can be recycled
        // (if needed)
        setImageDrawable(null);
    }

    /**
     * Decrement the ImageView reference counter of the wrapped bitmap and clear
     * the reference to it.
     */
    private void resetCachedBitmap() {
        if (mDisplayedBitmapWrapper != null) {
            mDisplayedBitmapWrapper.setBeingUsed(false);
            mDisplayedBitmapWrapper = null;
        }
    }
}
