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

package com.onegravity.contactpicker.core;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactFragment;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.group.GroupFragment;
import com.onegravity.contactpicker.picture.ContactPictureType;

public class PagerAdapter extends FragmentStatePagerAdapter {

    final private int mNumOfTabs;

    final private ContactSortOrder mSortOrder;
    final private ContactPictureType mBadgeType;
    final private ContactDescription mDescription;
    final private int mDescriptionType;

    public PagerAdapter(FragmentManager fm, int numOfTabs, ContactSortOrder sortOrder,
                        ContactPictureType badgeType, ContactDescription description, int descriptionType) {
        super(fm);

        mNumOfTabs = numOfTabs;
        mSortOrder = sortOrder;
        mBadgeType = badgeType;
        mDescription = description;
        mDescriptionType = descriptionType;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ContactFragment.newInstance(mSortOrder, mBadgeType, mDescription, mDescriptionType);
            case 1:
                return GroupFragment.newInstance();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

}
