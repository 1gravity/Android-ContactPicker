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

package com.onegravity.contactpicker.implementation;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.onegravity.contactpicker.Helper;
import com.onegravity.contactpicker.contact.Contact;

import java.util.HashSet;
import java.util.Set;

/**
 * ContactImpl is the concrete Contact implementation.
 * It can be instantiated and modified only within its own package to prevent modifications from
 * classes outside the package.
 */
class ContactImpl extends ContactElementImpl implements Contact {

    static ContactImpl fromCursor(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
		String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
		String[] names = displayName != null ? displayName.split("\\s+") : new String[]{"---", "---"};
		String firstName = names.length >= 1 ? names[0] : displayName;
		String lastName = names.length >= 2 ? names[1] : "";
		String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
		Uri uri = photoUri != null ? Uri.parse(photoUri) : null;
		return new ContactImpl(id, displayName, firstName, lastName, uri);
	}

	private String mFirstName = "";
	private String mLastName = "";
	private String mEmail = "";
	private String mPhone = "";
	private String mAddress = "";
	transient private Uri mPhotoUri;
	private Set<Long> mGroupIds = new HashSet<>();

	private ContactImpl(long id, String displayName, String firstName, String lastName, Uri photoUri) {
		super(id, displayName);
		mFirstName = Helper.isNullOrEmpty(firstName) ? "---" : firstName;
		mLastName = Helper.isNullOrEmpty(lastName) ? "---" : lastName;
		mPhotoUri = photoUri;
	}

	@Override
	public String getFirstName() {
		return mFirstName;
	}

	@Override
	public String getLastName() {
		return mLastName;
	}

	@Override
	public String getEmail() {
		return mEmail;
	}

	@Override
	public String getPhone() {
		return mPhone;
	}

	@Override
	public String getAddress() {
		return mAddress;
	}

	@Override
	public String getKey() {
		return getDisplayName();
	}

	@Override
	public Uri getPhotoUri() {
		return mPhotoUri;
	}

	@Override
	public Set<Long> getGroupIds() {
		return mGroupIds;
	}

    void setFirstName(String value) {
		mFirstName = value;
	}

    void setLastName(String value) {
		mLastName = value;
	}

    void setEmail(String value) {
		mEmail = value;
	}

    void setPhone(String value) {
		mPhone = value;
	}

    void setAddress(String value) {
		mAddress = value;
	}

    void addGroupId(long value) {
		mGroupIds.add(value);
	}

	@Override
	public String toString() {
		return super.toString() + ", " + mFirstName + " " + mLastName + ", " + mEmail;
	}

}
