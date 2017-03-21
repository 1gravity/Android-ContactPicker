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

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * All groups have been loaded.
 *
 * Publisher: ContactPickerActivity
 * Subscriber: GroupFragment
 */
public class GroupsLoaded {

    public static void post(List<? extends Group> groups) {
        GroupsLoaded event = new GroupsLoaded(groups);
        EventBus.getDefault().postSticky(event);
    }

    final private List<? extends Group> mGroups;

    private GroupsLoaded(List<? extends Group> groups) {
        mGroups = groups;
    }

    public List<? extends Group> getGroups() {
        return mGroups;
    }

}
