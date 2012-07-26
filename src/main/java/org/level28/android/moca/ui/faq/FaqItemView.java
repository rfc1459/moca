// @formatter:off
/*
 * FaqItemView.java - view holder for FAQ items
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

import org.level28.android.moca.R;
import org.level28.android.moca.ui.ItemView;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

/**
 * View holder for FAQ items.
 * 
 * @author Matteo Panella
 */
public class FaqItemView extends ItemView {

    /**
     * Category header (visible only on the first entry of a category)
     */
    public final TextView header;

    /**
     * Question view
     */
    public final TextView question;

    /**
     * Answer view (link-enabled)
     */
    public final TextView answer;

    public FaqItemView(View view) {
        super(view);

        header = textView(view, R.id.headerView);
        question = textView(view, R.id.faqQuestion);
        answer = textView(view, R.id.faqAnswer);
        answer.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
