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

import com.onegravity.contactpicker.OnContactCheckedListener;
import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.SelectionChanged;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactsLoaded;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // only groups with contacts will be shown
    private List<GroupImpl> mVisibleGroups = new ArrayList<>();

    // the complete list of contacts
    private List<GroupImpl> mGroups = new ArrayList<>();

    // groups by id to find them once the contacts are loaded
    private Map<Long, GroupImpl> mGroupsById = new HashMap<>();

    /*
     * The selected ids are put into the Bundle in onSaveInstanceState, restored in onCreate and
     * then applied to the contacts once they are loaded in onLoadFinished.
     */
    private static final String GROUP_IDS = "GROUP_IDS";
    private HashSet<Long> mSelectedIds = new HashSet<>();

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

        // some devices don't retain fragments
        if (savedInstanceState != null) {
            try {
                mSelectedIds = (HashSet<Long>) savedInstanceState.getSerializable(GROUP_IDS);
            }
            catch (ClassCastException ignore) {}
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mSelectedIds.clear();;
        for (Group group : mGroups) {
            if (group.isChecked()) {
                mSelectedIds.add( group.getId() );
            }
        }
        outState.putSerializable(GROUP_IDS, mSelectedIds);
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

        clearData();
        EventBus.getDefault().register(this);
        getLoaderManager().initLoader(GROUPS_LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
        clearData();
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

    @Override
    public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), GROUPS_URI, GROUPS_PROJECTION, GROUPS_SELECTION, null, GROUPS_SORT);
    }

    @Override
    public synchronized void onLoaderReset(Loader<Cursor> loader) {
        clearData();
        mAdapter.setData(mVisibleGroups);
    }

    @Override
    public synchronized void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        clearData();
        readGroups(cursor);
    }

    private void clearData() {
        mVisibleGroups.clear();
        mGroups.clear();
        mGroupsById.clear();
    }

    private void readGroups(Cursor cursor) {
        Log.e("1gravity", "***************************************************************");
        Log.e("1gravity", "* GROUPS                                                      *");
        Log.e("1gravity", "***************************************************************");

        if (cursor.moveToFirst()) {
            cursor.moveToPrevious();
            while (cursor.moveToNext()) {
                GroupImpl group = GroupImpl.fromCursor(cursor);

                mGroups.add(group);
                mGroupsById.put(group.getId(), group);

                boolean isChecked = mSelectedIds.contains(group.getId());
                group.setChecked(isChecked, true);

                Log.e("1gravity", "group " + group.getId() + ": " + group.getDisplayName());
                String SOURCE_ID = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID));
                Log.e("1gravity", "SOURCE_ID: " + SOURCE_ID);
            }
        }
        mSelectedIds.clear();

        GroupsLoaded.post(mGroups);

        mAdapter.setData(mVisibleGroups);

        if (mContacts != null && ! mContacts.isEmpty()) {
            processContacts(mContacts);
        }
    }

    // ****************************************** Process Contacts / Groups *******************************************

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactsLoaded event) {
        EventBus.getDefault().removeStickyEvent(event);
        mContacts = event.getContacts();
        if (! mGroups.isEmpty()) {
            processContacts( mContacts );
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SelectionChanged event) {
        if (! mGroups.isEmpty()) {
            boolean hasChanged = false;

            for (Group group : mGroups) {
                if (deselectGroup(group)) {
                    hasChanged = true;
                }
            }

            if (hasChanged) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private synchronized void processContacts(List<? extends Contact> contacts) {
        // map contacts to groups
        for (Contact contact : contacts) {
            for (Long groupId : contact.getGroupIds()) {
                GroupImpl group = mGroupsById.get(groupId);
                if (group != null) {
                    group.addContact(contact);
                    contact.addOnContactCheckedListener(mContactListener);
                }
            }
        }

        // determine visible groups (the ones with at least one contact)
        mVisibleGroups.clear();
        for (GroupImpl group : mGroups) {
            if (group.hasContacts()) {
                mVisibleGroups.add(group);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Listening to onContactChecked for contacts because we want to check/un-check groups based on
     * whether their contacts are all checked or unchecked.
     */
    private OnContactCheckedListener<Contact> mContactListener = new OnContactCheckedListener<Contact>() {
        @Override
        public void onContactChecked(Contact contact, boolean wasChecked, boolean isChecked) {
            boolean hasChanged = false;

            for (Long groupId : contact.getGroupIds()) {
                GroupImpl group = mGroupsById.get(groupId);
                if (deselectGroup(group)) {
                    hasChanged = true;
                }
            }

            if (hasChanged) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private boolean deselectGroup(Group group) {
        if (group != null) {
            // let's see if the contacts of the group are either all deselected
            boolean isSelected = false;
            for (Contact groupContact : group.getContacts()) {
                if (groupContact.isChecked()) {
                    isSelected = true;
                    break;
                }
            }
            if (! isSelected && group.isChecked()) {
                // all deselected
                group.setChecked(false, true);
                return true;
            }
        }

        return false;
    }

    // ****************************************** Option Menu *******************************************

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == R.id.menu_check_all) {
            checkAll();
            return true;
        }

        return false;
    }

    private void checkAll() {
        // determine if all groups are checked
        boolean allChecked = true;
        for (Group group : mGroups) {
            if (! group.isChecked()) {
                allChecked = false;
                break;
            }
        }

        // if all are checked then un-check the groups, otherwise check them all
        boolean isChecked = ! allChecked;
        for (Group group : mGroups) {
            if (group.isChecked() != isChecked) {
                group.setChecked(isChecked, false);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

}