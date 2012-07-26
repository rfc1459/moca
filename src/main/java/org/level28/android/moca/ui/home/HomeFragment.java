// @formatter:off
/*
 * HomeFragment.java - home screen fragment
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

package org.level28.android.moca.ui.home;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.level28.android.moca.AsyncLoader;
import org.level28.android.moca.BuildConfig;
import org.level28.android.moca.R;
import org.level28.android.moca.json.HomeDeserializer;
import org.level28.android.moca.json.JsonDeserializerException;
import org.level28.android.moca.model.HomeSection;
import org.level28.android.moca.ui.ItemListAdapter;
import org.level28.android.moca.ui.ItemListFragment;
import org.level28.android.moca.ui.ItemView;

import android.app.Activity;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.ListView;

/**
 * The wonderful home screen :-)
 * 
 * @author Matteo Panella
 */
public class HomeFragment extends ItemListFragment<HomeSection> {

    static final String LOG_TAG = "HomeFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText("UH-OH...");
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setDividerHeight(0);
    }

    @Override
    public Loader<List<HomeSection>> onCreateLoader(int id, final Bundle args) {
        return new AsyncLoader<List<HomeSection>>(getActivity()) {
            @Override
            public List<HomeSection> loadInBackground() {
                if (BuildConfig.DEBUG) {
                    Log.v(LOG_TAG, "loadInBackground+");
                }

                HomeDeserializer jsonLoader = new HomeDeserializer();
                List<HomeSection> contents;
                InputStream in = null;
                try {
                    in = getContext().getResources()
                            .openRawResource(R.raw.home);
                    contents = jsonLoader.fromInputStream(in);
                } catch (NotFoundException e) {
                    // Are you kidding me?
                    Log.wtf(LOG_TAG, "Raw JSON resource for Home not found", e);
                    return null;
                } catch (JsonDeserializerException e) {
                    Log.e(LOG_TAG, "Internal Jackson error", e);
                    return null;
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // Swallow the I/O exception
                        }
                    }
                }

                if (BuildConfig.DEBUG) {
                    Log.v(LOG_TAG, "loadInBackground-");
                }
                return contents;
            }
        };
    }

    // @formatter:off
    @Override
    protected ItemListAdapter<HomeSection, ? extends ItemView> createAdapter(
            List<HomeSection> items) {
        return new HomeAdapter(R.layout.home_list_item, getActivity()
                .getLayoutInflater(), items.toArray(new HomeSection[items.size()]));
    }
    // @formatter:on
}
