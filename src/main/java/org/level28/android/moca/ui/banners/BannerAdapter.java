// @formatter:off
/*
 * BannerAdapter.java - ListAdapter for banners
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

package org.level28.android.moca.ui.banners;

import org.level28.android.moca.bitmaps.SimpleBitmapLoader;
import org.level28.android.moca.ui.ItemListAdapter;

import android.view.LayoutInflater;
import android.view.View;

/**
 * List adapter for {@link Banner}s.
 * 
 * @author Matteo Panella
 */
public class BannerAdapter extends ItemListAdapter<Banner, BannerItemView> {

    private final SimpleBitmapLoader mBanners;

    /**
     * Create an empty adapter.
     */
    public BannerAdapter(int viewId, LayoutInflater inflater,
            final SimpleBitmapLoader bannerLoader) {
        this(viewId, inflater, null, bannerLoader);
    }

    /**
     * Create an adapter with given initial contents.
     */
    public BannerAdapter(int viewId, LayoutInflater inflater,
            Banner[] elements, final SimpleBitmapLoader bannerLoader) {
        super(viewId, inflater, elements);
        mBanners = bannerLoader;
    }

    @Override
    protected void update(int position, BannerItemView view, Banner item) {
        view.link.setText(item.url);
        mBanners.load(view.banner, item.resId);
    }

    @Override
    protected BannerItemView createView(View view) {
        return new BannerItemView(view);
    }
}
