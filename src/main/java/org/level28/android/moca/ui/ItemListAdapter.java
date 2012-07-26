// @formatter:off
/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// @formatter:on

package org.level28.android.moca.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Base list adapter for items of a specific type.
 * 
 * @param <I>
 *            item type
 * @param <V>
 *            view holder type for the specified item type
 * @author GitHub Inc.
 */
public abstract class ItemListAdapter<I, V extends ItemView> extends
        BaseAdapter {

    private final LayoutInflater inflater;

    private final int viewId;

    private Object[] elements;

    /**
     * Create an empty adapter.
     * 
     * @param viewId
     *            layout identifier for the item view
     * @param inflater
     *            inflater used to create the item views
     * @see #ItemListAdapter(int, LayoutInflater, I[])
     */
    public ItemListAdapter(final int viewId, final LayoutInflater inflater) {
        this(viewId, inflater, null);
    }

    /**
     * Create an adapter with predefined contents.
     * 
     * @param viewId
     *            layout identifier for the item view
     * @param inflater
     *            inflater used to create the item views
     * @param elements
     *            initial contents backing this adapter
     */
    public ItemListAdapter(final int viewId, final LayoutInflater inflater,
            final I[] elements) {
        this.viewId = viewId;
        this.inflater = inflater;
        if (elements != null) {
            this.elements = elements;
        } else {
            this.elements = new Object[0];
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * Get the array backing this adapter.
     */
    @SuppressWarnings("unchecked")
    protected I[] getItems() {
        return (I[]) elements;
    }

    @Override
    public int getCount() {
        return elements.length;
    }

    /**
     * Get the item at the specified position.
     */
    @SuppressWarnings("unchecked")
    public I getItem(int position) {
        return (I) elements[position];
    }

    @Override
    public long getItemId(int position) {
        return elements[position].hashCode();
    }

    /**
     * Bind this adapter to a new array of elements.
     * 
     * @param items
     *            the new array that should back this adapter
     */
    public ItemListAdapter<I, V> setItems(final Object[] items) {
        if (items != null) {
            elements = items;
        } else {
            elements = new Object[0];
        }
        notifyDataSetChanged();
        return this;
    }

    /**
     * Bind an element view with the item.
     * 
     * @param position
     *            current offset in the array backing this adapter
     * @param view
     *            the item view holder
     * @param item
     *            the item from which the view should be updated
     */
    protected abstract void update(int position, V view, I item);

    /**
     * Create a new view holder for a given item view.
     * 
     * @param view
     *            the item view created in
     *            {@link #getView(int, View, ViewGroup)}
     * @return a view holder caching references for elements inside the given
     *         view
     */
    protected abstract V createView(View view);

    @Override
    public View getView(final int position, View convertView,
            final ViewGroup parent) {
        @SuppressWarnings("unchecked")
        V view = convertView != null ? (V) convertView.getTag() : null;

        if (view == null) {
            convertView = inflater.inflate(viewId, null);
            view = createView(convertView);
            convertView.setTag(view);
        }
        update(position, view, getItem(position));
        return convertView;
    }
}
