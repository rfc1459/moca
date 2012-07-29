// @formatter:off
/*
 * BannerFragment.java - abstract fragment holding a list of banners
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

import java.util.List;

import org.level28.android.moca.AsyncLoader;
import org.level28.android.moca.R;
import org.level28.android.moca.ui.ItemListAdapter;
import org.level28.android.moca.ui.ItemListFragment;
import org.level28.android.moca.ui.ItemView;
import org.level28.android.moca.ui.MainActivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Abstract fragment holding a list of banners.
 * 
 * @author Matteo Panella
 */
public abstract class BannerFragment extends ItemListFragment<Banner> {

    static final String LOG_TAG = "SponsorsFragment";

    protected final List<Banner> contents;

    public BannerFragment() {
        contents = getBanners();
    }

    /**
     * Return a list of banners to display.
     */
    protected abstract List<Banner> getBanners();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText("UH-OH...");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // FIXME: override with a less ugly layout
        return inflater.inflate(R.layout.item_list, null);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        if (activity.getResources().getBoolean(R.bool.dualPaned)) {
            // Don't show dividers in dual-pane mode, they're really ugly
            listView.setDividerHeight(0);
        }
    }

    @Override
    public Loader<List<Banner>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader<List<Banner>>(getActivity()) {
            @Override
            public List<Banner> loadInBackground() {
                return contents;
            }
        };
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        // This fragment does not use an ExceptionLoader
        return 0;
    }

    @Override
    protected ItemListAdapter<Banner, ? extends ItemView> createAdapter(
            List<Banner> items) {
        final MainActivity activity = (MainActivity) getActivity();
        return new BannerAdapter(R.layout.banner_list_item,
                activity.getLayoutInflater(), items.toArray(new Banner[items
                        .size()]), activity.bannerLoader());
    }

}
