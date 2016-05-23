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

package com.onegravity.contactpicker.group;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactsLoaded;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // only groups with contacts will be shown
    private List<GroupImpl> mVisibleGroups = new ArrayList<>();

    // the complete list of contacts
    private List<GroupImpl> mGroups = new ArrayList<>();

    // groups by id to find them once the contacts are loaded
    private Map<Long, GroupImpl> mGroupsById = new HashMap<>();

    // store the contacts in case the groups haven't been loaded yet
    private List<? extends Contact> mContacts;

    private GroupAdapter mAdapter;

    // ****************************************** Lifecycle Methods *******************************************

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    public GroupFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootLayout = inflater.inflate(R.layout.group_list, null);
        RecyclerView recyclerView = (RecyclerView) rootLayout.findViewById(R.id.recycler_view);

        // use a LinearLayout for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // create adapter for the RecyclerView
        mAdapter = new GroupAdapter(null);
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

        getLoaderManager().initLoader(GROUPS_LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    // ****************************************** Loader Methods *******************************************

    // Groups
    private static final int GROUPS_LOADER_ID = 2;
    private static final Uri GROUPS_URI = ContactsContract.Groups.CONTENT_URI;
    private static final String[] GROUPS_PROJECTION = new String[] {
            ContactsContract.Groups._ID,
            ContactsContract.Groups.SOURCE_ID,
            ContactsContract.Groups.TITLE};
    private static final String GROUPS_SELECTION = ContactsContract.Groups.DELETED + " = 0";

    private static final String GROUPS_SORT = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

    /**
     * The loader id is -1  for the folder list (group cursor) and the the group cursor position
     * (0, 1, ...) for the sudokus (children).
     *
     * The folder id for querying the Sudoku puzzles is passed in the Bundle
     */
    @Override
    public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), GROUPS_URI, GROUPS_PROJECTION, GROUPS_SELECTION, null, GROUPS_SORT);
    }

    @Override
    public synchronized void onLoaderReset(Loader<Cursor> loader) {
        mVisibleGroups.clear();
        mGroups.clear();
        mGroupsById.clear();
        mAdapter.setData(mVisibleGroups);
    }

    @Override
    public synchronized void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mAdapter != null && cursor != null && ! cursor.isClosed()) {
            mGroups.clear();

            Log.e("1gravity", "***************************************************************");
            Log.e("1gravity", "* GROUPS                                                      *");
            Log.e("1gravity", "***************************************************************");

            if (cursor.moveToFirst()) {
                cursor.moveToPrevious();
                while (cursor.moveToNext()) {
                    GroupImpl group = GroupImpl.fromCursor(cursor);

                    // TODO: 5/11/2016 deal with duplicates...

                    mGroups.add(group);
                    mGroupsById.put(group.getId(), group);

                    Log.e("1gravity", "group " + group.getId() + ": " + group.getDisplayName());

                    String SOURCE_ID = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID));
                    Log.e("1gravity", "SOURCE_ID: " + SOURCE_ID);
                }
            }

            mAdapter.setData(mVisibleGroups);

            if (mContacts != null && ! mContacts.isEmpty()) {
                processContacts(mContacts);
            }
        }
    }

    // ****************************************** Misc Methods *******************************************

    private void processContacts(List<? extends Contact> contacts) {
        for (Contact contact : contacts) {
            for (Long groupId : contact.getGroupIds()) {
                GroupImpl group = mGroupsById.get(groupId);
                if (group != null) {
                    Log.e("1gravity", group.getDisplayName() + " --> "  + contact.getDisplayName());
                    group.addContact(contact);
                }
            }
        }

        mVisibleGroups.clear();
        for (GroupImpl group : mGroups) {
            if (group.hasContacts()) {
                mVisibleGroups.add(group);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactsLoaded event) {
        EventBus.getDefault().removeStickyEvent(event);
        if (mGroups.isEmpty()) {
            mContacts = event.getContacts();
        }
        else {
            processContacts(event.getContacts());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        return true;	// don't call super.onOptionsItemSelected because we got some StackOverflowErrors on Honeycomb
    }

}