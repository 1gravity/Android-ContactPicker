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

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.onegravity.contactpicker.Helper;
import com.onegravity.contactpicker.contact.Contact;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
		String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
		Uri uri = photoUri != null ? Uri.parse(photoUri) : null;
		return new ContactImpl(id, displayName, firstName, lastName, uri);
	}

	private String mFirstName = "";
	private String mLastName = "";
    private Map<Integer, String> mEmail = new HashMap<>();
    private Map<Integer, String> mPhone = new HashMap<>();
    private Map<Integer, String> mAddress = new HashMap<>();
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
	public String getEmail(int type) {
		String email = mEmail.get(type);
        if (email == null && ! mEmail.isEmpty()) {
            email = mEmail.values().iterator().next();
        }
        return  email;
	}

	@Override
	public String getPhone(int type) {
        String phone = mPhone.get(type);
        if (phone == null && ! mPhone.isEmpty()) {
            phone = mPhone.values().iterator().next();
        }
        return  phone;
    }

	@Override
	public String getAddress(int type) {
        String address = mAddress.get(type);
        if (address == null && ! mAddress.isEmpty()) {
            address = mAddress.values().iterator().next();
        }
        return  address;
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

    void setEmail(int type, String value) {
		mEmail.put(type, value);
	}

    void setPhone(int type, String value) {
		mPhone.put(type, value);
	}

    void setAddress(int type, String value) {
		mAddress.put(type, value);
	}

    void addGroupId(long value) {
		mGroupIds.add(value);
	}

    @Override
	public String toString() {
		return super.toString() + ", " + mFirstName + " " + mLastName + ", " + mEmail;
	}

}
