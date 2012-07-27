// @formatter:off
/*
 * MocaFragmentAdapter.java - adapter for ViewPager
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

package org.level28.android.moca.ui;

import java.util.Locale;

import org.level28.android.moca.R;
import org.level28.android.moca.ui.faq.FaqFragment;
import org.level28.android.moca.ui.home.HomeFragment;
import org.level28.android.moca.ui.twitter.TwitterFragment;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Adapter for the main {@link android.support.v4.view.ViewPager ViewPager}
 * 
 * @author Matteo Panella
 */
public class MocaFragmentAdapter extends FragmentPagerAdapter {

    private final Resources mResources;

    public MocaFragmentAdapter(final FragmentManager fm,
            final Resources resources) {
        super(fm);
        mResources = resources;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
        case 0:
            return new HomeFragment();
        case 1:
            return new TwitterFragment();
        case 2:
            return new FaqFragment();
        /*
        case 3:
            return new SponsorsFragment();
        case 4:
            return new PartnersFragment();
        */
        default:
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#getCount()
     */
    @Override
    public int getCount() {
        return 3;
    }

    // @formatter:off
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
        case 0:
            return mResources.getString(R.string.home).toUpperCase(Locale.US);
        case 1:
            return mResources.getString(R.string.twitter).toUpperCase(Locale.US);
        case 2:
            return mResources.getString(R.string.faq).toUpperCase(Locale.US);
        case 3:
            return mResources.getString(R.string.sponsors).toUpperCase(Locale.US);
        case 4:
            return mResources.getString(R.string.partners).toUpperCase(Locale.US);
        default:
            return null;
        }
    }
    // @formatter:on
}
