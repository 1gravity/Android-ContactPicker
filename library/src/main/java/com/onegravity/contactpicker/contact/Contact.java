/*
 * Copyright (C) 2015-2023 Emanuel Moecklin
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

import android.net.Uri;

import com.onegravity.contactpicker.ContactElement;

import java.util.Map;
import java.util.Set;

/**
 * This interface describes a single contact.
 * It only provides read methods to make sure no class outside this package can modify it.
 * Write access is only possible through the ContactImpl class which has package access.
 */
public interface Contact extends ContactElement {

    String getFirstName();

    String getLastName();

    /**
     * ContactsContract.CommonDataKinds.Email.TYPE values:
     *
     * - TYPE_CUSTOM
     * - TYPE_HOME
     * - TYPE_WORK
     * - TYPE_OTHER
     * - TYPE_MOBILE
     *
     * @param type the type
     * @return a String with the Email.
     */
    String getEmail(int type);

    /**
     * @see com.onegravity.contactpicker.contact.Contact#getEmail(int)
     * @return the map type to email.
     */
    Map<Integer, String> getMapEmail();

    /**
     * ContactsContract.CommonDataKinds.Email.ContactsContract.CommonDataKinds.Phone.TYPE values:
     *
     * - TYPE_CUSTOM, TYPE_HOME, TYPE_MOBILE, TYPE_WORK, TYPE_FAX_WORK, TYPE_FAX_HOME, TYPE_PAGER
     * - TYPE_OTHER, TYPE_CALLBACK, TYPE_CAR, TYPE_COMPANY_MAIN, TYPE_ISDN, TYPE_MAIN
     * - TYPE_OTHER_FAX, TYPE_RADIO, TYPE_TELEX, TYPE_TTY_TDD, TYPE_WORK_MOBILE, TYPE_WORK_PAGER
     * - TYPE_ASSISTANT, TYPE_MMS
     *
     * @param type the type
     * @return a String with the phone.
     */
    String getPhone(int type);

    /**
     * @see com.onegravity.contactpicker.contact.Contact#getPhone(int)
     * @return the map type to phone.
     */
    Map<Integer, String> getMapPhone();

    /**
     * * ContactsContract.CommonDataKinds.StructuredPostal.TYPE values:
     *
     * - TYPE_CUSTOM
     * - TYPE_HOME
     * - TYPE_WORK
     * - TYPE_OTHER
     *
     * @param type the type
     * @return A String with the address
     */
    String getAddress(int type);

    /**
     * @see com.onegravity.contactpicker.contact.Contact#getAddress(int)
     * @return the map type to address.
     */
    Map<Integer, String> getMapAddress();

    /**
     * The contact letter is used in the ContactBadge (if no contact picture can be found).
     */
    char getContactLetter();

    /**
     * The contact letter according to the sort order is used in the SectionIndexer for the fast
     * scroll indicator.
     */
    char getContactLetter(ContactSortOrder sortOrder);

    /**
     * The contact color is used in the ContactBadge (if no contact picture can be found) as
     * background color.
     */
    int getContactColor();

    /**
     * Unique key across all contacts that won't change even if the column id changes.
     */
    String getLookupKey();

    Uri getPhotoUri();

    Set<Long> getGroupIds();
}
