// @formatter:off
/*
 * SponsorsFragment.java - MOCA 2012 sponsors
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

import java.util.Arrays;
import java.util.List;

import org.level28.android.moca.R;


/**
 * Fragment holding banners for our sponsors.
 * 
 * @author Matteo Panella
 */
public final class SponsorsFragment extends BannerFragment {

    public SponsorsFragment() {
        loaderId = 666;
    }

    @Override
    protected List<Banner> getBanners() {
        // @formatter:off
        return Arrays.asList(new Banner[] {
                new Banner(R.drawable.sonicwall, "http://www.sonicwall.com"),
                new Banner(R.drawable.quantumleap, "http://www.quantumleap.it"),
                new Banner(R.drawable.micso, "http://www.micso.it"),
                new Banner(R.drawable.securenetwork, "http://www.securenetwork.it")
        });
        // @formatter:on
    }
}
