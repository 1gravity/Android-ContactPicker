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

package com.onegravity.contactpicker.core;

import com.onegravity.contactpicker.ContactElement;
import com.onegravity.contactpicker.Helper;
import com.onegravity.contactpicker.OnContactCheckedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The concrete but abstract implementation of ContactElement.
 */
abstract class ContactElementImpl implements ContactElement {

    final private long mId;
    private String mDisplayName;

    transient private List<OnContactCheckedListener> mListeners = new ArrayList<>();
    transient private boolean mChecked = false;

    ContactElementImpl(long id, String displayName) {
        mId = id;
        mDisplayName = Helper.isNullOrEmpty(displayName) ? "---" : displayName;
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public String getDisplayName() {
        return mDisplayName != null ? mDisplayName : "";
    }

    protected void setDisplayName(String value) {
        mDisplayName = value;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked, boolean suppressListenerCall) {
        boolean wasChecked = mChecked;
        mChecked = checked;
        if (!mListeners.isEmpty() && wasChecked != checked && !suppressListenerCall) {
            for (OnContactCheckedListener listener : mListeners) {
                listener.onContactChecked(this, wasChecked, checked);
            }
        }
    }

    @Override
    public void addOnContactCheckedListener(OnContactCheckedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public boolean matchesQuery(String[] queryStrings) {
        String dispName = getDisplayName();
        if (Helper.isNullOrEmpty(dispName)) return false;

        dispName = dispName.toLowerCase(Locale.getDefault());
        for (String queryString : queryStrings) {
            if (!dispName.contains(queryString)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return mId + ": " + mDisplayName;
    }

}
