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

package com.onegravity.contactpicker.contact;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onegravity.contactpicker.BaseFragment;
import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.implementation.ContactSelectionChanged;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends BaseFragment {

    // the list of all contacts
    private List<? extends Contact> mContacts = new ArrayList<>();

    private ContactAdapter mAdapter;

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    public ContactFragment() {}

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootLayout = inflater.inflate(R.layout.contact_list, null);
        RecyclerView recyclerView = (RecyclerView) rootLayout.findViewById(R.id.recycler_view);

        // use a LinearLayout for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // create adapter for the RecyclerView
        mAdapter = new ContactAdapter(rootLayout.getContext(), null);
        recyclerView.setAdapter(mAdapter);

        return rootLayout;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactsLoaded event) {
        EventBus.getDefault().removeStickyEvent(event);

        mContacts = event.getContacts();
        mAdapter.setData(mContacts);
    }

    @Override
    protected void checkAll() {
        // determine if all contacts are checked
        boolean allChecked = true;
        for (Contact contact : mContacts) {
            if (! contact.isChecked()) {
                allChecked = false;
                break;
            }
        }

        // if all are checked then un-check the contacts, otherwise check them all
        boolean isChecked = ! allChecked;
        for (Contact contact : mContacts) {
            if (contact.isChecked() != isChecked) {
                contact.setChecked(isChecked, true);
            }
        }

        ContactSelectionChanged.post();

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void performFiltering(String[] queryStrings) {
        List<Contact> filteredElements = new ArrayList<>();
        for (Contact contact : mContacts) {
            if (contact.matchesQuery(queryStrings)) {
                filteredElements.add(contact);
            }
        }

        mAdapter.setData(filteredElements);
    }

}
