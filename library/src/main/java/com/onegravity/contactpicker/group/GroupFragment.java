/*
 * Copyright (C) 2015-2017 Emanuel Moecklin
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onegravity.contactpicker.BaseFragment;
import com.onegravity.contactpicker.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends BaseFragment {

    /**
     * The list of all visible groups.
     * This is only used as a reference to the original data set while we actually use
     * mFilteredGroups.
     */
    private List<? extends Group> mGroups = new ArrayList<>();

    /**
     * The list of all visible and filtered groups.
     */
    private List<? extends Group> mFilteredGroups = new ArrayList<>();

    private GroupAdapter mAdapter;

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    public GroupFragment() {}

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAdapter = new GroupAdapter(null);
        return super.createView(inflater, R.layout.cp_group_list, mAdapter, mGroups);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GroupsLoaded event) {
        EventBus.getDefault().removeStickyEvent(event);

        mGroups = event.getGroups();
        mFilteredGroups = mGroups;
        mAdapter.setData(mFilteredGroups);

        updateEmptyViewVisibility(mGroups);
    }

    @Override
    protected void checkAll() {
        if (mFilteredGroups == null) return;

        // determine if all groups are checked
        boolean allChecked = true;
        for (Group group : mFilteredGroups) {
            if (! group.isChecked()) {
                allChecked = false;
                break;
            }
        }

        // if all are checked then un-check the groups, otherwise check them all
        boolean isChecked = ! allChecked;
        for (Group group : mFilteredGroups) {
            if (group.isChecked() != isChecked) {
                group.setChecked(isChecked, false);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void performFiltering(String[] queryStrings) {
        if (mGroups == null) return;

        if (queryStrings == null || queryStrings.length == 0) {
            mFilteredGroups = mGroups;
        }
        else {
            List<Group> filteredElements = new ArrayList<>();
            for (Group group : mGroups) {
                if (group.matchesQuery(queryStrings)) {
                    filteredElements.add(group);
                }
            }
            mFilteredGroups = filteredElements;
        }

        mAdapter.setData(mFilteredGroups);
    }

}
