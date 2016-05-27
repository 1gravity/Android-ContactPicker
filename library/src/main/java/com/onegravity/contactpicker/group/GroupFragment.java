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

    // the list of all visible groups
    private List<? extends Group> mGroups = new ArrayList<>();

    private GroupAdapter mAdapter;

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    public GroupFragment() {}

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAdapter = new GroupAdapter(null);
        return super.createView(inflater, R.layout.group_list, mAdapter, mGroups);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GroupsLoaded event) {
        EventBus.getDefault().removeStickyEvent(event);

        mGroups = event.getGroups();
        mAdapter.setData(mGroups);
        updateEmptyViewVisibility(mGroups);
    }

    @Override
    protected void checkAll() {
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
    protected void performFiltering(String[] queryStrings) {
        List<Group> filteredElements = new ArrayList<>();
        for (Group group : mGroups) {
            if (group.matchesQuery(queryStrings)) {
                filteredElements.add(group);
            }
        }

        mAdapter.setData(filteredElements);
    }

}