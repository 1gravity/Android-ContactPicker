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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GroupFragment extends Fragment implements SearchView.OnQueryTextListener {

    // the list of all visible groups
    private List<? extends Group> mGroups = new ArrayList<>();

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
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GroupsLoaded event) {
        EventBus.getDefault().removeStickyEvent(event);

        mGroups = event.getGroups();
        mAdapter.setData(mGroups);
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final String queryString = newText.toString().toLowerCase( Locale.getDefault() );
        final String[] queryStrings = queryString.split(" ");

        final List<Group> filteredElements = filter(mGroups, queryStrings);
        mAdapter.setData(filteredElements);

        return true;
    }

    private List<Group> filter(List<? extends Group> groups, String[] queryStrings) {
        List<Group> filteredElements = new ArrayList<>();
        for (Group group : groups) {
            if (group.matchesQuery(queryStrings)) {
                filteredElements.add(group);
            }
        }
        return filteredElements;
    }

}