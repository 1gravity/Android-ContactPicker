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
import java.util.List;

public abstract class ContactBase implements Serializable {

	public interface OnContactsCheckedListener {
		void onContactChecked(ContactBase contact, boolean wasChecked, boolean isChecked);
		void onContactsChecked(List<ContactBase> contacts, boolean wasChecked, boolean isChecked);
	}

	final private long mId;
	final private String mDisplayName;

	private OnContactsCheckedListener mListener;
	private boolean mChecked = false;

	public ContactBase(long id, String displayName) {
		mId = id;
		mDisplayName = Helper.isNullOrEmpty(displayName) ? "---" : displayName;
	}

	public long getId() {
		return mId;
	}

	public String getDisplayName() {
		return mDisplayName != null ? mDisplayName : "";
	}

	public void setOnContactCheckedListener(OnContactsCheckedListener listener) {
		mListener = listener;
	}

	public boolean isChecked() {
		return mChecked;
	}

	/**
	 * Note: the Group class must override this method since usually more than one contact is
	 * checked/unchecked at a time.
     */
	public void setChecked(boolean checked) {
		boolean wasChecked = mChecked;
		mChecked = checked;
		if (mListener != null && wasChecked != checked) {
			mListener.onContactChecked(this, wasChecked, checked);
		}
	}

	/**
	 * Sub classes should override this method if the number of contacts this object represents is
	 * not 1 (Groups).
     */
	protected int getNrOfContacts() {
		return 1;
	}

	@Override
	public String toString() {
		return mId + ": " + mDisplayName;
	}

}
