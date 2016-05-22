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

import com.onegravity.contactpicker.ContactElement;
import com.onegravity.contactpicker.ContactElementImpl;
import com.onegravity.contactpicker.OnContactsCheckedListener;
import com.onegravity.contactpicker.contact.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GroupImpl is the concrete Group implementation.
 * It can be instantiated and modified only within its own package to prevent modifications from
 * classes outside the package.
 */
class GroupImpl extends ContactElementImpl implements Group {

	static GroupImpl fromCursor(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Groups._ID));
		String title = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));
		return new GroupImpl(id, title);
	}

	private static Set<Contact> mContacts;

	private GroupImpl(long id, String displayName) {
		super(id, displayName);
		mContacts = new HashSet<>();
	}

    @Override
    protected void notifyOnContactsCheckedListener(OnContactsCheckedListener listener,
                                                   boolean wasChecked, boolean isChecked) {
        List<ContactElement> changedContacts = new ArrayList<>();
        for (Contact contact : mContacts) {
            if (contact.isChecked() != isChecked) {
                changedContacts.add(contact);
                // TODO: 5/17/16 we need to make sure the change isn't notified by each contact
                contact.setChecked(isChecked);
            }
        }

        listener.onContactsChecked(changedContacts, wasChecked, isChecked);
    }

	@Override
	public Set<Contact> getContacts() {
		return mContacts;
	}

    void addContact(Contact contact) {
        mContacts.add(contact);
    }

}
