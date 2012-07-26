// @formatter:off
/*
 * FaqFragment.java - FAQ screen UI
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

package org.level28.android.moca.ui.faq;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.level28.android.moca.AsyncLoader;
import org.level28.android.moca.BuildConfig;
import org.level28.android.moca.R;
import org.level28.android.moca.json.FaqDeserializer;
import org.level28.android.moca.json.JsonDeserializerException;
import org.level28.android.moca.model.FaqEntry;
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
 * UI for FAQ screen.
 * 
 * @author Matteo Panella
 */
public class FaqFragment extends ItemListFragment<FaqEntry> {

    static final String LOG_TAG = "FaqFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText("UH-OH...");
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setDividerHeight(0);
        updateCategoryHeaders(items);
    }

    void updateCategoryHeaders(final List<FaqEntry> entries) {
        FaqAdapter adapter = (FaqAdapter) getListAdapter();
        if (adapter == null || entries == null) {
            return;
        }

        adapter.clearHeaders();

        String category = "";
        for (FaqEntry entry : entries) {
            if (!category.equals(entry.category)) {
                category = entry.category;
                adapter.registerHeader(entry);
            }
        }
    }

    @Override
    public Loader<List<FaqEntry>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader<List<FaqEntry>>(getActivity()) {

            @Override
            public List<FaqEntry> loadInBackground() {
                if (BuildConfig.DEBUG) {
                    Log.v(LOG_TAG, "loadInBackground+");
                }

                FaqDeserializer jsonLoader = new FaqDeserializer();
                List<FaqEntry> entries;
                InputStream in = null;
                try {
                    in = getContext().getResources().openRawResource(R.raw.faq);
                    entries = jsonLoader.fromInputStream(in);
                } catch (NotFoundException e) {
                    // WTF?
                    Log.wtf(LOG_TAG, "Raw JSON resource for FAQ not found", e);
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
                return entries;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<FaqEntry>> loader,
            List<FaqEntry> items) {
        // Orientation change, mon amour...
        updateCategoryHeaders(items);
        super.onLoadFinished(loader, items);
    }

    @Override
    protected ItemListAdapter<FaqEntry, ? extends ItemView> createAdapter(
            List<FaqEntry> items) {
        return new FaqAdapter(R.layout.faq_list_item, getActivity()
                .getLayoutInflater(), items.toArray(new FaqEntry[items.size()]));
    }
}
