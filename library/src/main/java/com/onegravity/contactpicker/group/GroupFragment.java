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

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private List<Group> mGroups = new ArrayList<>();

    private GroupAdapter mAdapter;

    // ****************************************** Lifecycle Methods *******************************************

    public static GroupFragment newInstance() {
        GroupFragment frag = new GroupFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
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
        //EventBus.getDefault().register(this);

        getLoaderManager().initLoader(GROUPS_LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //EventBus.getDefault().unregister(this);
    }

    // ****************************************** Loader Methods *******************************************

    // Groups
    private static final int GROUPS_LOADER_ID = 2;
    private static final Uri GROUPS_URI = ContactsContract.Groups.CONTENT_URI;
    private static final String[] GROUPS_PROJECTION = new String[] {
            ContactsContract.Groups._ID,
            ContactsContract.Groups.ACCOUNT_TYPE,
            ContactsContract.Groups.ACCOUNT_NAME,
            ContactsContract.Groups.TITLE};
    private static final String GROUPS_SELECTION =
            ContactsContract.Groups.DELETED + " = 0 AND " +
            ContactsContract.Groups.GROUP_VISIBLE + " = 1";

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
        mGroups.clear();
        mAdapter.setData(mGroups);
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
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID));
                    String title = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));
                    Group group = Group.fromCursor(cursor);
                    // // TODO: 5/11/2016 deal with duplicates...
                    mGroups.add(group);
                    String ACCOUNT_TYPE = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE));
                    String ACCOUNT_NAME = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME));
                    Log.e("1gravity", "group " + id + ": " + title);
                    Log.e("1gravity", "ACCOUNT_TYPE " + ACCOUNT_TYPE);
                    Log.e("1gravity", "ACCOUNT_NAME " + ACCOUNT_NAME);
                }
            }

            mAdapter.setData(mGroups);
        }
    }

    // ****************************************** Option Menu Methods *******************************************

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        return true;	// don't call super.onOptionsItemSelected because we got some StackOverflowErrors on Honeycomb
    }

}