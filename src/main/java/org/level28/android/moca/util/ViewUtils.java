// @formatter:off
/*
 * ViewUtils.java - utility class for views
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

package org.level28.android.moca.util;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;

/**
 * Utility class containing various support functions for views.
 */
public final class ViewUtils {
    private ViewUtils() {
        // Don't new me :-(
    }

    /**
     * Change view visibility.
     * 
     * @param view
     *            the {@link View} on which the method should act
     * @param gone
     *            when set to {@code true}, the view will be marked as
     *            {@link View#GONE GONE}, when set to {@code false} the view
     *            will be marked as {@link View#VISIBLE VISIBLE}
     */
    public static void setGone(final View view, final boolean gone) {
        if (view == null) {
            return;
        }

        final int current = view.getVisibility();
        if (gone && current != GONE) {
            view.setVisibility(GONE);
        } else if (!gone && current != VISIBLE) {
            view.setVisibility(VISIBLE);
        }
    }

    /**
     * Fade-in a view.
     * 
     * @param context
     *            the context from which the animation should be loaded
     * @param view
     *            the view that should be faded in
     * @param animate
     *            {@code true} if the animation should actually take place,
     *            {@code false} otherwise
     */
    public static void fadeIn(final Context context, final View view,
            final boolean animate) {
        if (view != null) {
            if (animate) {
                view.startAnimation(AnimationUtils.loadAnimation(context,
                        android.R.anim.fade_in));
            } else {
                view.clearAnimation();
            }
        }
    }
}
