// @formatter:off
/*
 * HomeItemView.java - view holder for homescreen items
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

import org.level28.android.moca.R;
import org.level28.android.moca.ui.ItemView;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

/**
 * View holder for homescreen items.
 * 
 * @author Matteo Panella
 */
public class HomeItemView extends ItemView {

    /**
     * HomeSection header text
     */
    public final TextView header;

    /**
     * HomeSection contents
     */
    public final TextView contents;

    HomeItemView(final View view) {
        super(view);

        header = textView(view, R.id.homeHeader);
        contents = textView(view, R.id.homeText);
        contents.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
