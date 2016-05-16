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

package com.onegravity.contactpicker.contact;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.onegravity.contactpicker.ContactBase;
import com.onegravity.contactpicker.Helper;

public class Contact extends ContactBase {

	public static Contact fromCursor(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
		String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
		String[] names = displayName != null ? displayName.split("\\s+") : new String[]{"---", "---"};
		String firstName = names.length >= 1 ? names[0] : displayName;
		String lastName = names.length >= 2 ? names[1] : "";
		String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
		Uri uri = photoUri != null ? Uri.parse(photoUri) : null;
		return new Contact(id, displayName, firstName, lastName, uri);
	}

	private String mFirstName = "";
	private String mLastName = "";
	private String mEmail = "";
	private Uri mPhotoUri;

	public Contact(long id, String displayName, String firstname, String lastname, Uri photoUri) {
		super(id, displayName);
		mFirstName = Helper.isNullOrEmpty(firstname) ? "---" : firstname;
		mLastName = Helper.isNullOrEmpty(lastname) ? "---" : lastname;
		mPhotoUri = photoUri;
	}

	public String getFirstName() {
		return mFirstName;
	}

	public String getLastName() {
		return mLastName;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String value) {
		mEmail = value;
	}

	public String getKey() {
		return getDisplayName();
	}

	public Uri getPhotoUri() {
		return mPhotoUri;
	}

	@Override
	public String toString() {
		return super.toString() + ", " + mFirstName + " " + mLastName + ", " + mEmail;
	}

}
