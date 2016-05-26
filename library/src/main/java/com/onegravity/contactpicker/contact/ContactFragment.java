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
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.implementation.ContactSelectionChanged;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContactFragment extends Fragment implements SearchView.OnQueryTextListener {

    // the list of all contacts
    private List<? extends Contact> mContacts = new ArrayList<>();

    private ContactAdapter mAdapter;

    // ****************************************** Lifecycle Methods *******************************************

    /**
     * Fragment for opening the message list for a "real" account/folder
     */
    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    public ContactFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactsLoaded event) {
        EventBus.getDefault().removeStickyEvent(event);

        mContacts = event.getContacts();
        mAdapter.setData(mContacts);
    }

    // ****************************************** Option Menu *******************************************

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == R.id.action_check_all) {
            checkAll();
            return true;
        }

        return false;
    }

    private void checkAll() {
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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final String queryString = newText.toString().toLowerCase( Locale.getDefault() );
        final String[] queryStrings = queryString.split(" ");

        final List<Contact> filteredElements = filter(mContacts, queryStrings);
        mAdapter.setData(filteredElements);

        return true;
    }

    private List<Contact> filter(List<? extends Contact> contacts, String[] queryStrings) {
        List<Contact> filteredElements = new ArrayList<>();
        for (Contact contact : contacts) {
            if (contact.matchesQuery(queryStrings)) {
                filteredElements.add(contact);
            }
        }
        return filteredElements;
    }

}
