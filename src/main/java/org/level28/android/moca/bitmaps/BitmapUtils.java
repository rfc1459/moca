// @formatter:off
/*
 * BitmapUtils.java - Bitmap utilities
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

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.WHITE;
import static android.graphics.PorterDuff.Mode.SRC_IN;

import java.io.File;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Helper class for resampling images from the web.
 * 
 * @author Matteo Panella
 */
public final class BitmapUtils {

    /**
     * Calculate the appropriate sample size to downsize a {@code Bitmap}.
     * 
     * @param options
     *            a pre-initialized {@link BitmapFactory.Options} object
     * @param reqWidth
     *            target width
     * @param reqHeight
     *            target height
     * @return a value for {@link BitmapFactory.Options#inSampleSize} to perform
     *         the resize operation
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Downsample the image
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // Aggressively clamp down the number of samples
            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * Decode a {@link Bitmap} from the application resources scaling it to a
     * desired size.
     * <p>
     * Note: this function does <b>NOT</b> cache decoded bitmaps, the caller is
     * responsible for all cache operations.
     * </p>
     * 
     * @param res
     *            The resource object containing the image data
     * @param resId
     *            The resource id of the image data
     * @param reqWidth
     *            The required final width of the image
     * @param reqHeight
     *            The required final height of the image
     * @return The decoded and scaled bitmap, or {@code null} if the image data
     *         could not be decoded
     * @see BitmapUtils#decodeSampledBitmapFromByteArray(byte[], int, int, int,
     *      int)
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res,
            int resId, int reqWidth, int reqHeight) {
        // Decode with inJustDecodeBounds=true to get raw bitmap size (skips
        // memory allocation)
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = ARGB_8888;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate a suitable inSampleSize value
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Now decode and downsample the bitmap in one pass
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode a {@link Bitmap} from raw bytes of compressed image data, scaling
     * it to a desired size.
     * <p>
     * Note: this function does <b>NOT</b> cache decoded bitmaps, the caller is
     * responsible for all cache operations.
     * </p>
     * 
     * @param data
     *            byte array of compressed image data
     * @param offset
     *            offset into data from where the decoder should begin parsing
     * @param length
     *            number of bytes to parse starting at offset
     * @param reqWidth
     *            required final width of the image
     * @param reqHeight
     *            required final height of the image
     * @return The decoded and scaled bitmap, or {@code null} if the image data
     *         could not be decoded
     * @see BitmapUtils#decodeSampledBitmapFromResource(Resources, int, int,
     *      int)
     */
    public static Bitmap decodeSampledBitmapFromByteArray(byte[] data,
            int offset, int length, int reqWidth, int reqHeight) {
        // Pretty much like decodeSampleBitmapFromResource
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = ARGB_8888;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Do it for real this time...
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, offset, length, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth,
            int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = ARGB_8888;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Do it for real this time...
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    /**
     * Round the corners of a {@link Bitmap}
     * 
     * @param source
     *            Raw bitmap
     * @param radius
     *            radius of corners (in pixels)
     * @return the original bitmap with its corners rounded by radius pixels
     */
    public static Bitmap roundCorners(final Bitmap source, final float radius) {
        final int width = source.getWidth();
        final int height = source.getHeight();

        final Bitmap output = Bitmap.createBitmap(width, height, ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(WHITE);
        canvas.drawRoundRect(rectF, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(SRC_IN));
        canvas.drawBitmap(source, rect, rect, paint);

        source.recycle();

        return output;
    }
}
