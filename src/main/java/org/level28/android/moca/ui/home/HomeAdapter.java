// @formatter:off
/*
 * HomeAdapter.java - home screen list adapter
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

import org.level28.android.moca.model.HomeSection;
import org.level28.android.moca.ui.ItemListAdapter;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;

/**
 * List adapter for the home screen.
 * 
 * @author Matteo Panella
 */
public class HomeAdapter extends ItemListAdapter<HomeSection, HomeItemView> {

    public HomeAdapter(int viewId, LayoutInflater inflater) {
        super(viewId, inflater);
    }

    public HomeAdapter(int viewId, LayoutInflater inflater,
            HomeSection[] elements) {
        super(viewId, inflater, elements);
    }

    @Override
    protected void update(final int position, final HomeItemView view,
            final HomeSection item) {
        view.header.setText(item.header);
        final Spanned renderedContents = Html.fromHtml(item.contents + "<br>");
        view.contents.setText(renderedContents);
    }

    @Override
    protected HomeItemView createView(View view) {
        return new HomeItemView(view);
    }
}
