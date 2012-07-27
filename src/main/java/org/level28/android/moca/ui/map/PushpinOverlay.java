// @formatter:off
/*
 * PushpinOverlay.java - pushpin overlay for MOCA location
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

package org.level28.android.moca.ui.map;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * Static pushpin overlay for MOCA location.
 *
 * @author Matteo Panella
 */
public final class PushpinOverlay extends ItemizedOverlay<OverlayItem> {

    @SuppressWarnings("unused")
    private final Context mContext;

    private final OverlayItem mPushpin;

    public PushpinOverlay(final Context context, final Drawable defaultMarker, final OverlayItem pushpin) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
        mPushpin = pushpin;
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return mPushpin;
    }

    @Override
    public int size() {
        return 1;
    }
}
