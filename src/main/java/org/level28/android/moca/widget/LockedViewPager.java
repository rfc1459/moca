// @formatter:off
/*
 * LockedViewPager.java - ViewPager subclass that does not respond to swipe events
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

package org.level28.android.moca.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Subclass of {@link ViewPager} that does not respond to swipe events.
 * 
 * @author Matteo Panella
 */
public class LockedViewPager extends ViewPager {

    public LockedViewPager(Context context) {
        this(context, null);
    }

    public LockedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     * (non-Javadoc)
     * Discard touch events directed to this ViewPager
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    /*
     * (non-Javadoc)
     * Do not intercept touch events directed to this ViewGroup
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }
}
