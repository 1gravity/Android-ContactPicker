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

package com.onegravity.contactpicker;

/**
 * The concrete but abstract implementation of ContactElement.
 */
public abstract class ContactElementImpl implements ContactElement {

	final private long mId;
	final private String mDisplayName;

	transient private OnContactsCheckedListener mListener;
	transient private boolean mChecked = false;

	public ContactElementImpl(long id, String displayName) {
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

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		boolean wasChecked = mChecked;
		mChecked = checked;
		if (mListener != null && wasChecked != checked) {
            notifyOnContactsCheckedListener(mListener, wasChecked, checked);
		}
	}

    protected abstract void notifyOnContactsCheckedListener(OnContactsCheckedListener listener,
                                                            boolean wasChecked, boolean isChecked);

    @Override
    public void setOnContactCheckedListener(OnContactsCheckedListener listener) {
        mListener = listener;
    }

	@Override
	public String toString() {
		return mId + ": " + mDisplayName;
	}

}
