/*
 * Copyright (C) 2015-2016 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onegravity.contactpicker;

import android.widget.Filter;

import com.onegravity.contactpicker.contact.ContactAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContactFilter extends Filter {

    private ContactAdapter mAdapter;

    /**
     * All contacts.
     */
    private ArrayList<ContactBase> mOriginalValues = null;

    public ContactFilter(ContactAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * Do the actual search.
     */
    @Override
    protected FilterResults performFiltering(CharSequence searchTerm) {
        FilterResults results = new FilterResults();

        // Copy the values from mFolders to mOriginalValues if this is the
        // first time this method is called.
        if (mOriginalValues == null) {
            /*int count = mAdapter.getCount();
            mOriginalValues = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                //mOriginalValues.add( mAdapter.getContact(i) );
            }*/
        }

        if (Helper.isNullOrEmpty(searchTerm)) {
            ArrayList<ContactBase> list = new ArrayList<>(mOriginalValues);
            results.values = list;
            results.count = list.size();
        }
        else {
            final String searchTermString = searchTerm.toString().toLowerCase(Locale.getDefault());
            final String[] words = searchTermString.split(" ");
            final int wordCount = words.length;

            final ArrayList<ContactBase> values = mOriginalValues;

            final ArrayList<ContactBase> newValues = new ArrayList<>();

            for (final ContactBase value : values) {
                final String valueText = value.toString().toLowerCase(Locale.getDefault());

                for (int k = 0; k < wordCount; k++) {
                    if (valueText.contains(words[k])) {
                        newValues.add(value);
                        break;
                    }
                }
            }

            results.values = newValues;
            results.count = newValues.size();
        }

        return results;
    }

    /**
     * Publish the results to the user-interface.
     * {@inheritDoc}
     */
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        // Don't notify for every change
        mAdapter.setNotifyOnChange(false);
        try {
            final List<ContactBase> contacts = (List<ContactBase>) results.values;
            //mAdapter.clear();
            if (contacts != null) {
                for (ContactBase contact : contacts) {
                    if (contact != null) {
              //        mAdapter.add(contact);
                    }
                }
            }

            // Send notification that the data set changed now
            mAdapter.notifyDataSetChanged();
        } finally {
            // restore notification status
            mAdapter.setNotifyOnChange(true);
        }
    }

    public void invalidate() {
        mOriginalValues = null;
    }

}
