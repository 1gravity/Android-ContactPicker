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

import android.database.Cursor;
import android.provider.ContactsContract;

import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.ContactBase;

import java.util.HashSet;
import java.util.Set;

public class Group extends ContactBase {

	public static Group fromCursor(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Groups._ID));
		String title = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));
		return new Group(id, title);
	}

	private static Set<Contact> mContacts;

	public Group(long id, String displayName) {
		super(id, displayName);
		mContacts = new HashSet<>();
	}

	void addContact(Contact contact) {
		mContacts.add(contact);
	}

	public Set<Contact> getContacts() {
		return mContacts;
	}

}
