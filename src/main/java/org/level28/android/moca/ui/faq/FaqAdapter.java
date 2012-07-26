// @formatter:off
/*
 * FaqAdapter.java - list adapter for FAQ entries
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

import java.util.HashSet;
import java.util.Set;

import org.level28.android.moca.model.FaqEntry;
import org.level28.android.moca.ui.ItemListAdapter;
import org.level28.android.moca.util.ViewUtils;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;

/**
 * List adapter for FAQ entries.
 * 
 * @author Matteo Panella
 */
public class FaqAdapter extends ItemListAdapter<FaqEntry, FaqItemView> {

    private final Set<Integer> entriesWithHeader = new HashSet<Integer>();

    public FaqAdapter(int viewId, LayoutInflater inflater) {
        this(viewId, inflater, null);
    }

    public FaqAdapter(int viewId, LayoutInflater inflater, FaqEntry[] elements) {
        super(viewId, inflater, elements);
    }

    /**
     * Clear the list of registered category headers.
     */
    FaqAdapter clearHeaders() {
        entriesWithHeader.clear();
        return this;
    }

    /**
     * Register a new category header bound to a specific entry id.
     */
    FaqAdapter registerHeader(FaqEntry entry) {
        entriesWithHeader.add(entry.id);
        return this;
    }

    @Override
    protected void update(int position, FaqItemView view, FaqEntry item) {
        // Check if we have to display a category header
        if (entriesWithHeader.contains(item.id)) {
            ViewUtils.setGone(view.header, false);
            view.header.setText(item.category);
        } else {
            ViewUtils.setGone(view.header, true);
        }

        view.question.setText(item.question);
        final Spanned renderedAnswer = Html.fromHtml(item.answer + "<br>");
        view.answer.setText(renderedAnswer);
    }

    @Override
    protected FaqItemView createView(View view) {
        return new FaqItemView(view);
    }
}
