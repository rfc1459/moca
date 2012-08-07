// @formatter:off
/*
 * PartnersFragment.java - MOCA 2012 partners
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
 * Fragment holding banners for our partners.
 * 
 * @author Matteo Panella
 */
public class PartnersFragment extends BannerFragment {

    public PartnersFragment() {
        // Every reference to my role as operator on Azzurra IRC Network is
        // purely coincidental ;-P
        loaderId = 6667;
    }

    @Override
    protected List<Banner> getBanners() {
        // @formatter:off
        return Arrays.asList(new Banner[] {
                new Banner(R.drawable.slackware, "http://slackware.it"),
                new Banner(R.drawable.sikurezza, "http://sikurezza.org"),
                new Banner(R.drawable.backtrack, "http://www.backtrack-linux.org"),
                new Banner(R.drawable.ggdroma, "http://www.girlgeekdinnersroma.com"),
                new Banner(R.drawable.btitalia, "http://www.backtrack.it"),
                new Banner(R.drawable.dyne, "http://www.dyne.org"),
                new Banner(R.drawable.azzurra, "http://www.azzurra.org"),
                new Banner(R.drawable.mamma, "http://www.mamma.am"),
                new Banner(R.drawable.phtv, "http://www.phtv.it"),
        });
        // @formatter:on
    }
}
