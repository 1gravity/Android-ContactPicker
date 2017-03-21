/*
 * Copyright (C) 2015-2017 Emanuel Moecklin
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

package com.onegravity.contactpicker.picture;

import android.provider.ContactsContract;

/**
 * Some constans used in the ContactBadge and the ContactQueryHandler.
 */
class Constants {

    static final int TOKEN_EMAIL_LOOKUP = 0;
    static final int TOKEN_PHONE_LOOKUP = 1;
    static final int TOKEN_EMAIL_LOOKUP_AND_TRIGGER = 2;
    static final int TOKEN_PHONE_LOOKUP_AND_TRIGGER = 3;

    static final String EXTRA_URI_CONTENT = "uri_content";

    static final String[] EMAIL_LOOKUP_PROJECTION = new String[]{
            ContactsContract.RawContacts.CONTACT_ID,
            ContactsContract.Contacts.LOOKUP_KEY,
    };
    static final int EMAIL_ID_COLUMN_INDEX = 0;
    static final int EMAIL_LOOKUP_STRING_COLUMN_INDEX = 1;

    static final String[] PHONE_LOOKUP_PROJECTION = new String[]{
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.LOOKUP_KEY,
    };
    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final int PHONE_LOOKUP_STRING_COLUMN_INDEX = 1;

}
