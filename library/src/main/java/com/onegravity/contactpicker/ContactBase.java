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

import java.io.Serializable;

public class ContactBase implements Serializable {

	public interface OnCheckedChangeListener {
		void onCheckedChanged(ContactBase contact, boolean isChecked);
	}

	public static final String CONTACTS_DATA = "CONTACTS_DATA";

	private long mId;
	private String mDisplayName = "";
	private boolean mChecked = false;
	private OnCheckedChangeListener mListener;

	public ContactBase(long id, String displayName) {
		mId = id;
		mDisplayName = Helper.isNullOrEmpty(displayName) ? "---" : displayName;
	}

	void setOnCheckedChangedListener(OnCheckedChangeListener listener) {
		mListener = listener;
	}

	void removeOnCheckedChangedListener(OnCheckedChangeListener listener) {
		if (mListener == listener) {
			mListener = null;
		}
	}

	public long getId() {
		return mId;
	}

	public String getDisplayName() {
		return mDisplayName;
	}

	boolean isChecked() {
		return mChecked;
	}

	void setChecked(boolean checked) {
		if (checked != mChecked) {
			mChecked = checked;
			if (mListener != null) {
				mListener.onCheckedChanged(this, checked);
			}
		}
	}

	@Override
	public String toString() {
		return mId + ": " + mDisplayName;
	}

}
