// @formatter:off
/*
 * LicenseDialogFragment.java - dialog fragment used to display the license file
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Display the license file in a dialog.
 * 
 * @author Matteo Panella
 */
public class LicenseDialogFragment extends SherlockDialogFragment {
    private static final String LICENSE_URI = "file:///android_asset/license.html";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                getActivity());
        WebView contentView = new WebView(getActivity());

        dialogBuilder.setTitle(R.string.menu_license)
                .setView(contentView)
                .setNeutralButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        contentView.loadUrl(LICENSE_URI);

        return dialogBuilder.create();
    }
}
