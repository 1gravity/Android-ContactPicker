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
import android.provider.ContactsContract;

import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.group.Group;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    private Map<Long, Contact> mContacts = new HashMap<>();

    private GroupImpl(long id, String displayName) {
        super(id, displayName);
    }

    @Override
    public Collection<Contact> getContacts() {
        return mContacts.values();
    }

    void addContact(Contact contact) {
        long contactId = contact.getId();
        if (!mContacts.keySet().contains(contactId)) {
            mContacts.put(contact.getId(), contact);
        }
    }

    boolean hasContacts() {
        return mContacts.size() > 0;
    }

}
