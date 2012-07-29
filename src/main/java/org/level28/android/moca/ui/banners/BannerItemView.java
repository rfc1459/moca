// @formatter:off
/*
 * BannerItemView.java - view holder for banners
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

import org.level28.android.moca.R;
import org.level28.android.moca.ui.CacheableImageView;
import org.level28.android.moca.ui.ItemView;

import android.view.View;
import android.widget.TextView;

/**
 * View holder for sponsor/partner banners.
 *
 * @author Matteo Panella
 */
public class BannerItemView extends ItemView {

    /**
     * Banner image (pre-scaled, non-cacheable!).
     */
    public final CacheableImageView banner;

    /**
     * Link to sponsor/partner website.
     */
    public final TextView link;

    public BannerItemView(View view) {
        super(view);

        banner = (CacheableImageView) view.findViewById(R.id.bannerImage);
        link = textView(view, R.id.bannerLink);
    }
}
