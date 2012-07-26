// @formatter:off
/*
 * FragmentListAdapter.java - bridge between ListView and MocaFragmentAdapter
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

import org.level28.android.moca.R;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Bridge between the fragment {@link android.widget.ListView ListView} inside
 * the tablet layout and {@link MocaFragmentAdapter}.
 * 
 * @author Matteo Panella
 */
public class FragmentListAdapter extends BaseAdapter {

    private final MocaFragmentAdapter mAdapter;

    private final LayoutInflater mInflater;

    public FragmentListAdapter(Context context, final ViewPager pager) {
        this.mAdapter = (MocaFragmentAdapter) pager.getAdapter();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return mAdapter.getCount();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public CharSequence getItem(int position) {
        // This is a bit counter-intuitive: the list needs the fragment's title, not the fragment itself
        return mAdapter.getPageTitle(position);
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = mInflater.inflate(R.layout.simple_list_item_activated_1, parent, false);
        } else {
            v = convertView;
        }

        final TextView title = (TextView) v.findViewById(android.R.id.text1);
        title.setText(getItem(position));

        return v;
    }
}
