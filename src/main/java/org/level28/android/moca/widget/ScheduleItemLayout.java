// @formatter:off
/*
 * ScheduleItemLayout.java - TODO: fill file description
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

import org.level28.android.moca.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug;
import android.widget.LinearLayout;

/**
 * Subclass of {@link LinearLayout} which supports extra states needed by the
 * schedule list.
 * 
 * @author Matteo Panella
 */
public class ScheduleItemLayout extends LinearLayout {

    private static final int[] STATE_CURRENT = { R.attr.state_current };

    private boolean mCurrent = false;

    /**
     * {@inheritDoc}
     */
    public ScheduleItemLayout(Context context) {
        this(context, null);
    }

    /**
     * {@inheritDoc}
     */
    public ScheduleItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // For some reason, LinearLayout got the three-argument constructor as late
    // as API Level 11...

    /**
     * Indicates if this view represents the current session.
     * 
     * @return {@code true} if this view represents the current session,
     *         {@code false} otherwise
     */
    @ViewDebug.ExportedProperty
    public boolean isCurrent() {
        return mCurrent;
    }

    /**
     * Changes the {@code current} status of this view.
     * 
     * @param current
     *            {@code true} if this view represents the current session,
     *            {@code false} otherwise
     */
    public void setCurrent(final boolean current) {
        if (current != mCurrent) {
            mCurrent = current;
            invalidate();
            refreshDrawableState();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (mCurrent) {
            mergeDrawableStates(drawableState, STATE_CURRENT);
        }

        return drawableState;
    }
}
